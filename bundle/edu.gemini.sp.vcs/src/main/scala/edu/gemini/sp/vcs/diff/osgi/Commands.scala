package edu.gemini.sp.vcs.diff.osgi

import edu.gemini.pot.sp.version._
import edu.gemini.pot.spdb.IDBDatabaseService
import edu.gemini.sp.vcs.diff.ProgramLocation.{Both, RemoteOnly, LocalOnly, Neither}
import edu.gemini.sp.vcs.diff._
import edu.gemini.sp.vcs.diff.VcsAction._
import edu.gemini.sp.vcs.reg.VcsRegistrar
import edu.gemini.spModel.core.{Peer, SPProgramID}
import edu.gemini.util.security.auth.keychain.KeyChain

import scalaz._
import Scalaz._

trait Commands {
  def vcs2(cmd: String, args: Array[String]): String
}

object Commands {
  def parseId(s: String): String \/ SPProgramID =
    \/.fromTryCatch {
      SPProgramID.toProgramID(s)
    }.leftMap(_ => s"Sorry, '$s' isn't a valid program id")

  def parseLoc(s: String): String \/ Peer =
    Option(Peer.tryParse(s)).toRightDisjunction(s"Sorry, expecting host:port(:site) not '$s'")

  def usage(cmd: String): String =
    s"Usage: vcs2 $cmd [[host:port]] program_id]"

  case class CmdArgs(loc: Peer, id: SPProgramID)

  def apply(odb: IDBDatabaseService, auth: KeyChain, vcs: Vcs, reg: VcsRegistrar): Commands = new Commands {
    var defaultLocation = new Peer("localhost", 8443, null)

    var lastProgId: Option[SPProgramID] = None

    def host(): String =
      s"default vcs host set to ${defaultLocation.host} ${defaultLocation.port}"

    def host(loc: Peer): String = {
      defaultLocation = loc
      host()
    }

    def locationFor(id: SPProgramID): Peer =
      reg.registration(id).getOrElse(defaultLocation)

    def parseArgs(cmd: String, args: Array[String]): String \/ CmdArgs =
      args.length match {
        case 2 =>
          for {
            loc <- parseLoc(args(0))
            id  <- parseId(args(1))
          } yield CmdArgs(loc, id)

        case 1 => parseId(args(0)).map { id =>
          CmdArgs(locationFor(id), id)
        }
        case 0 => lastProgId.toRightDisjunction(usage(cmd)).map { id =>
          CmdArgs(locationFor(id), id)
        }
        case _ => usage(cmd).left
      }

    def remember(args: CmdArgs) {
      lastProgId = Some(args.id)
      reg.register(args.id, args.loc)
    }

    type VcsOp = (SPProgramID, Peer) => TryVcs[String]

    def doVcsOp(cmd: String, args: Array[String])(op: VcsOp): String =
      (for {
        ca  <- parseArgs(cmd, args)
        res <- op(ca.id, ca.loc).leftMap { f =>
                 VcsFailure.explain(f, ca.id, cmd, Some(ca.loc))
               }
      } yield {
        remember(ca)
        res
      }).toEither.merge

    def timed[A](body: => A): (A, Long) = {
      val start = System.currentTimeMillis()
      (body, System.currentTimeMillis() - start)
    }

    def formatted(msg: String, time: Long): String =
      s"$msg\n$time ms"

    val pull: VcsOp = (id, peer) => {
      val (out, time) = timed { vcs.pull(id, peer).unsafeRun }
      out.map { updated =>
        formatted(if (updated) "Updated local program." else "Already up to date.", time)
      }
    }

    val push: VcsOp = (id, peer) => {
      val (out, time) = timed { vcs.push(id, peer).unsafeRun }
      out.map { updated =>
        formatted(if (updated) "Updated remote program." else "Already up to date.", time)
      }
    }

    val sync: VcsOp = (id, peer) => {
      val (out, time) = timed { vcs.sync(id, peer).unsafeRun }

      out.map { updates =>
        formatted(updates match {
          case Neither    => "Already up to date."
          case LocalOnly  => "Updated local program."
          case RemoteOnly => "Updated remote program."
          case Both       => "Synchronized."
        }, time)
      }
    }

    val vcsOps: Map[String, VcsOp] = Map(
      "pull" -> pull,
      "push" -> push,
      "sync" -> sync
    )

    type VcsCmdHandler = (String, Array[String]) => Option[String]

    val vcsCmdHandler: VcsCmdHandler = (cmd, args) =>
      vcsOps.get(cmd).map { op =>
        doVcsOp(cmd, args)(op)
      }

    val hostHandler: VcsCmdHandler = (cmd, args) =>
      if ("host".equals(cmd))
        args.length match {
          case 0 => Some(host())
          case 1 => Some(parseLoc(args(0)).map(host).toEither.merge)
          case _ => None
        }
      else None

    val handlers = List(vcsCmdHandler, hostHandler)

    def vcs2(cmd: String, args: Array[String]): String =
      try {
        exec(cmd, args, handlers) | "Usage: vcs2 host|sync"
      } catch {
        case e: Exception =>
          e.printStackTrace()
          "???"
      }

    def exec(cmd: String, args: Array[String], handlerList: List[VcsCmdHandler]): Option[String] =
      handlerList match {
        case h :: t => h(cmd, args) orElse exec(cmd, args, t)
        case _ => None
      }
  }
}
