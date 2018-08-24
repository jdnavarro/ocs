package edu.gemini.dbTools.timingwindowcheck

import org.osgi.framework.BundleContext
import java.io.File
import java.security.Principal
import java.time.Instant
import java.util.logging.{Level, Logger}

import edu.gemini.skycalc.{Interval, TwilightBoundType, TwilightBoundedNight, Union}
import scalaz._
import Scalaz._
import edu.gemini.dbTools.mailer.ProgramAddresses
import edu.gemini.pot.spdb.IDBDatabaseService
import edu.gemini.spModel.core.Site

object TimingWindowCheckCron {

  case class ActionLogger(logger: Logger) {
    def log(level: Level, msg: String): Action[Unit] =
      Action.delay(logger.log(level, msg))
  }

  private def sendEmails(
    log: ActionLogger,
    now: Instant,
    odb: IDBDatabaseService,
    twcm: TimingWindowCheckMailer,
    ps:  TimingWindows
  ): Action[Unit] =

    ps.toList.traverseU {
      case (pid, l) =>
        l.toNel.fold(Action.unit) { tws =>
          EitherT(ProgramAddresses.fromProgramId(odb, pid).catchLeft).flatMap {
            case None =>
              log.log(Level.INFO, s"Could not get email addresses for $pid because it was not found in ODB")

            case Some(Failure(msg)) =>
              log.log(Level.INFO, s"Could not send mask check nag email because some addresses are not valid for $pid: $msg")

            case Some(Success(pa)) =>
              twcm.notifyPendingCheck(pid, pa, tws)
          }
        }
    }.void

  /** Cron job entry point.  See edu.gemini.spdb.cron.osgi.Activator. */
  def run(ctx: BundleContext)(tmpDir: File, logger: Logger, env: java.util.Map[String, String], user: java.util.Set[Principal]): Unit = {

    def getCheckUnion(now: Instant, site: Site): Union[Interval] = {

      val night = TwilightBoundedNight.forInstant(TwilightBoundType.NAUTICAL, now, site)

      var u = new Union[Interval]
      u.add(new Interval(night.getStartInstant, night.next.getStartInstant))
      if (now.isAfter(night.getEndInstant)) u else { u.remove(new Interval(now, night.getEndInstant)); u }
    }

    val action = for {
      env <- TimingWindowCheckEnv.fromBundleContext(ctx)
      now <- Action.delay(Instant.now)
      union = getCheckUnion(now, env.site)
      all <- TimingWindowFunctor.query(env.odb, user)
      ps = all.toList.flatMap {
        case (pid, otw) => {
          val ftws = otw.filter {
            case (_, tw) => union.contains(tw)
          }
          if (ftws.nonEmpty) List((pid, ftws)) else Nil
        }
      }.toMap
      _  <- sendEmails(ActionLogger(logger), now, env.odb, env.mailer, ps)
    } yield ()

    action.run.unsafePerformIO() match {
      case -\/(ex) => logger.log(Level.WARNING, "Error executing TimingWindowCheckCron", ex)
      case \/-(_)  => logger.info("TimingWindowCheckCron complete")
    }
  }

}