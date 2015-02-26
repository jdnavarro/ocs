package edu.gemini.sp.vcs.diff

import java.io.IOException
import java.util.logging.{Level, Logger}

import edu.gemini.pot.sp.SPNodeKey
import edu.gemini.spModel.core.{VersionException, Peer, SPProgramID}

import scalaz._
import Scalaz._

sealed trait VcsFailure

object VcsFailure {
  /** Two distinct programs share the same program id or key. */
  case class KeyClash(key0: SPNodeKey, id0: SPProgramID, key1: SPNodeKey, id1: SPProgramID) extends VcsFailure

  /** The program associated with the given id could not be found. */
  case class NotFound(id: SPProgramID) extends VcsFailure

  /** Indicates that the user tried to do something for which he doesn't have
    * permission. */
  case class Forbidden(why: String) extends VcsFailure

  /** Indicates that the program you're trying to commit is out of date with
    * respect to the server's version.  Commit only works when the incoming
    * program is strictly newer than the existing version. */
  case object NeedsUpdate extends VcsFailure

  /** Indicates that the program you're trying to commit has conflicts which
    * must be resolved before committing. */
  case object HasConflict extends VcsFailure

  /** Indicates that the local program cannot be merged with the remote
    * program.  For example, because it contains executed observations that
    * would be renumbered. */
  case class Unmergeable(msg: String) extends VcsFailure

  /** Indicates an unexpected problem while performing a vcs operation. */
  case class Unexpected(msg: String) extends VcsFailure

  /** Exception thrown while performing a vcs operation. */
  case class VcsException(ex: Throwable) extends VcsFailure


  def explain(f: VcsFailure, id: SPProgramID, op: String, peer: Option[Peer]): String = {

    val peerName = peer.map { p => s"${p.host}:${p.port}" } | "remote host"
    val msg = f match {
      case KeyClash(k0,i0,k1,i1) =>
        if (k0 == k1) s"Internal error.  Another program ($i1) with the same internal key as $i0 already exists in the database."
        else s"There is another program in the database with ID '$i0'.  Give your program a new ID and try again."

      case NotFound(i)           =>
        s"$i is not in the database."

      case Forbidden(why)        =>
        s"Denied permission to $op $id: $why"

      case NeedsUpdate           =>
        "You have to update your version of the program before you can commit changes."

      case HasConflict           =>
        "You have to resolve all conflicts in your program before you can commit changes."

      case Unmergeable(m)        =>
        s"Internal error. You program could not be merged: $m"

      case Unexpected(m)         =>
        s"Internal error. The changes in the database could not be merged with your version of the program: $m"

      case VcsException(ex: VersionException) =>
        ex.getLongMessage(peerName)

      case VcsException(io: IOException)      =>
        val m = Option(io.getMessage) | "unknown network issue"
        s"There was a problem communicating with the server: $m.  Try again later."

      case VcsException(ex)                   =>
        val m = Option(ex.getMessage) | "unknown error"
        s"Internal error. Something went wrong in the database server: $m"
    }

    val exOpt = f match {
      case VcsException(ex) => Some(ex)
      case _                => None
    }

    val log = Logger.getLogger(VcsFailure.getClass.getName)
    log.log(Level.WARNING, s"VcsFailure $op $peerName: $msg", exOpt.orNull)
    msg
  }
}
