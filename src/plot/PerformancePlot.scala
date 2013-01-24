/** 
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 12:58 
 */

package roofline
package plot



import roofline.quantities._





case class PerformancePlot(series : List[PerformanceSeries], peakPerformances : List[(String,Performance)]) extends Plot2D{
  val min = series.map (single_series => single_series.series.map( point => point.problemSize).min).min -1
  xMin = if (min < 0) 0 else min
  xMax = series.map (single_series => single_series.series.map( point => point.problemSize).max).max * 1.1

  yMin = 0.00001
  yMax =  peakPerformances.head._2.value *1.1

  logX = true
  logY = false
}
