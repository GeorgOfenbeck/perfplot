/** 
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 12:58 
 */

package roofline
package plot



import roofline.quantities._

case class PerformancePoint (problemSize : Double, performance: List[Performance], label: String = "")
case class PerformanceSeries (label: String, points: scala.collection.immutable.Map[Double, PerformancePoint])



case class PerformancePlot(series : List[PerformanceSeries], peakPerformances : List[(String,Performance)]) extends Plot2D{
  xMin = 0.1
  xMax = 80

  yMin = 0.1
  yMax =  peakPerformances.head._2.value *1.1

  logX = true
  logY = true
}
