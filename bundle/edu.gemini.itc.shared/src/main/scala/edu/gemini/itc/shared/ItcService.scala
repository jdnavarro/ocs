package edu.gemini.itc.shared

import edu.gemini.spModel.core.Peer
import edu.gemini.util.trpc.client.TrpcClient

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Color

import scalaz._
import Scalaz._

/** The data structures here are an attempt to unify the results produced by the different instrument recipes.
  * Results are either a few simple numbers in case of imaging or a set of charts made up by data series with (x,y)
  * value pairs for spectroscopy.
  */
sealed trait ItcResult extends Serializable {
  def peakPixelFlux(ccd: Int = 0):  Int
  def warnings:                     List[ItcWarning]
}

// === IMAGING RESULTS

final case class ImgData(singleSNRatio: Double, totalSNRatio: Double, peakPixelFlux: Double)

final case class ItcImagingResult(ccds: List[ImgData], warnings: List[ItcWarning]) extends ItcResult {
  def ccd(i: Int) = ccds(i % ccds.length)
  def peakPixelFlux(ccdIx: Int = 0) = ccd(ccdIx).peakPixelFlux.toInt
}

// === SPECTROSCOPY RESULTS

// There are two different types of charts
sealed trait SpcChartType
case object SignalChart    extends SpcChartType { val instance = this } // signal and background over wavelength [nm]
case object S2NChart       extends SpcChartType { val instance = this } // single and final S2N over wavelength [nm]

// There are four different data sets
sealed trait SpcDataType
case object SignalData     extends SpcDataType { val instance = this }  // signal over wavelength [nm]
case object BackgroundData extends SpcDataType { val instance = this }  // background over wavelength [nm]
case object SingleS2NData  extends SpcDataType { val instance = this }  // single S2N over wavelength [nm]
case object FinalS2NData   extends SpcDataType { val instance = this }  // final S2N over wavelength [nm]

/** Series of (x,y) data points used to create charts and text data files. */
final case class SpcSeriesData(dataType: SpcDataType, title: String, data: Array[Array[Double]], color: Option[Color] = None) {
  def x(i: Int): Double      = xValues(i)
  def y(i: Int): Double      = yValues(i)
  def xValues: Array[Double] = data(0)
  def yValues: Array[Double] = data(1)
}

/** Charts are made up of a set of data series which are all plotted in the same XY-plot. */
final case class SpcChartData(chartType: SpcChartType, title: String, xAxisLabel: String, yAxisLabel: String, series: List[SpcSeriesData]) {
  // JFreeChart requires a unique name for each series
  require(series.map(_.title).distinct.size == series.size, "titles of series are not unique")

  /** Gets all data series for the given type. */
  def allSeries(t: SpcDataType): List[SpcSeriesData] = series.filter(_.dataType == t)

  /** Gets all data series for the given type as Java lists. */
  def allSeriesAsJava(t: SpcDataType): java.util.List[SpcSeriesData] = series.filter(_.dataType == t)
}

/** The result of a spectroscpy ITC calculation is a set of charts and text files.
  * Individual charts and data series can be referenced by their types and an index. For most instruments there
  * is only one chart and data series of each type, however for NIFS for example there will be several charts
  * of each type in case of multiple IFU elements. */
final case class ItcSpectroscopyResult(charts: List[SpcChartData], warnings: List[ItcWarning]) extends ItcResult {

  /** Gets chart data by type and index.
    * This method will fail if the result you're looking for does not exist.
    */
  def chart(t: SpcChartType, i: Int = 0): SpcChartData      = charts.filter(_.chartType == t)(i)

  /** Gets all data series by chart type and data type.
    * This method will fail if the result (chart/data) you're looking for does not exist.
    */
  def allSeries(ct: SpcChartType, dt: SpcDataType): List[SpcSeriesData] = chart(ct).allSeries(dt)

  def peakPixelFlux(ccd: Int = 0): Int = {
    // zip signal and background values, sum them and return max value (i.e. max(signal + background))
    def maxSum(s: SpcSeriesData, b: SpcSeriesData) =
      s.yValues.zip(b.yValues).map(p => p._1 + p._2).max.round

    // zip signal and background value arrays and return max of all maximums
    // e.g. GNIRS with cross dispersion will have several arrays for signal and background, one for each order
    def maxAllSum(s: List[SpcSeriesData], b: List[SpcSeriesData]) =
      s.zip(b).map(p => maxSum(p._1, p._2)).max

    val signal     = allSeries(SignalChart, SignalData)
    val background = allSeries(SignalChart, BackgroundData)
    maxAllSum(signal, background).toInt
  }

}

object ItcSpectroscopyResult {

  // java compatibility
  def apply(charts: java.util.List[SpcChartData], warnings: java.util.List[ItcWarning]) =
    new ItcSpectroscopyResult(charts.toList, warnings.toList)

}

object ItcResult {

  import edu.gemini.itc.shared.ItcService._

  /** Creates an ITC result in case of an error. */
  def forException(e: Throwable): Result = ItcError(e.getMessage).left

  /** Creates an ITC result with a single problem/error message. */
  def forMessage(msg: String): Result = ItcError(msg).left

  /** Creates an ITC result for a result. */
  def forResult(result: ItcResult): Result = result.right

}

/**
 * Service interface for ITC calculations.
 */
trait ItcService {

  import edu.gemini.itc.shared.ItcService._

  def calculate(p: ItcParameters): Result

}

sealed trait ItcMessage
final case class ItcError(msg: String) extends ItcMessage
final case class ItcWarning(msg: String) extends ItcMessage

case class ItcParameters(
              source: SourceDefinition,
              observation: ObservationDetails,
              conditions: ObservingConditions,
              telescope: TelescopeDetails,
              instrument: InstrumentDetails)

object ItcService {

  type Result = ItcError \/ ItcResult

  /** Performs an ITC call on the given host. */
  def calculate(peer: Peer, inputs: ItcParameters): Future[Result] =
    TrpcClient(peer).withoutKeys future { r =>
      r[ItcService].calculate(inputs)
    }

}