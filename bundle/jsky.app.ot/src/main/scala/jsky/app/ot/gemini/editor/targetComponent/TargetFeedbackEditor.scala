package jsky.app.ot.gemini.editor.targetComponent

import java.awt.{Component, Insets}

import edu.gemini.pot.sp.ISPNode
import edu.gemini.shared.util.immutable.{Option => GOption}
import edu.gemini.spModel.obs.context.ObsContext
import edu.gemini.spModel.rich.shared.immutable._
import edu.gemini.spModel.target.SPTarget
import jsky.app.ot.OT
import jsky.app.ot.ags.BagsManager
import jsky.app.ot.gemini.editor.targetComponent.TargetFeedback.Row

import scala.swing.GridBagPanel.Fill
import scala.swing.{GridBagPanel, Swing}


class TargetFeedbackEditor extends TelescopePosEditor {
  private val tab: TargetFeedbackEditor.Table = new TargetFeedbackEditor.Table

  def getComponent: Component = tab.peer

  override def edit(ctxOpt: GOption[ObsContext], target: SPTarget, node: ISPNode): Unit = {
    println("--- Starting edit")
    val analysis = {
      val mt = OT.getMagnitudeTable
      ctxOpt.asScalaOpt.map(TargetGuidingFeedback.targetAnalysis(_, mt, target)).getOrElse(Nil)
    }

    //Swing.onEDT {
      // Construct the rows for the table. Optionally a BAGS row, and then a list of AGS analysis rows.
      val rows = {
        val bagsRow = for {
          n   <- Option(node)
          o   <- Option(n.getContextObservation)
          pk  <- Option(o.getProgram).map(_.getProgramKey)
          ok  <- Option(o.getNodeKey)
          s   <- BagsManager.stateLookup(pk, ok)
          row <- BagsFeedback.toRow(s, ctxOpt.asScalaOpt)
        } yield row

        // If the BAGS row is defined, then use it. If not, create the rows corresponding to the analysis.
        // NOTE that is target.isTooTarget, we don't want an analysis.
        bagsRow.fold(if (target.isTooTarget) Nil else analysis)(List(_))
      }

      if (rows.isEmpty)
        tab.clear()
      else
        tab.showRows(rows)
    //}
    println("+++ Ending edit")
  }
}

object TargetFeedbackEditor {
  class Table extends GridBagPanel {
    def clear(): Unit = showRows(Nil)

    def showRow(row: Row): Unit = showRows(List(row))

    def showRows(rows: List[Row]): Unit = {
      layout.clear()

      rows.zipWithIndex.foreach { case (row, rowIndex) =>
        layout(row) = new Constraints {
          gridy   = rowIndex
          weightx = 1.0
          fill    = Fill.Horizontal
          insets  = new Insets(0, 0, 1, 0)
        }
      }
      println("Revalidating...")
      revalidate()
    }
  }
}