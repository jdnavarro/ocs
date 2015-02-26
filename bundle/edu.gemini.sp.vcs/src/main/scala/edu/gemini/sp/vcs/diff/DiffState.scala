package edu.gemini.sp.vcs.diff

import edu.gemini.pot.sp.{ISPProgram, SPNodeKey}
import edu.gemini.pot.sp.version.VersionMap

/** Groups the information required to calculate a
  * [[edu.gemini.sp.vcs.diff.ProgramDiff]]. */
case class DiffState(vm: VersionMap, removed: Set[SPNodeKey])

object DiffState {
  def apply(p: ISPProgram): DiffState = DiffState(p.getVersions, removedKeys(p))
}
