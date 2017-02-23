package edu.gemini.model.p1.dtree.inst

import edu.gemini.model.p1.immutable._

/**
 *
 */

trait AltairNode {
  val allGuideStarTypes: Boolean
  val title       = "Adaptive Optics"
  val description = "Please select an adaptive optics option."

  // REL-3079: GN laser is not available for 2017B.
  def choices     = if (allGuideStarTypes) {
      //List(AltairNone, AltairLGS(pwfs1 = false, aowfs = true), AltairLGS(pwfs1 = true), AltairLGS(pwfs1 = false, oiwfs = true), AltairNGS(fieldLens = false), AltairNGS(fieldLens = true))
      List(AltairNone, AltairNGS(fieldLens = false), AltairNGS(fieldLens = true))
    } else {
      List(AltairNone, AltairNGS(fieldLens = false), AltairNGS(fieldLens = true))
    }
}