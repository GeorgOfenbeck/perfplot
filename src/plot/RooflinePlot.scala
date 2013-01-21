package roofline
package plot

import roofline.quantities._
/**
 * Georg Ofenbeck
 First created:
 * Date: 14/01/13
 * Time: 11:22 
 */
case class RooflinePlot(peakPerformances : List[(String,Performance)], peakBandwidths : List[(String,Throughput)], series: List[RooflineSeries] ) extends Plot2D{
  val autoscaleX = false
  val autoscaleY = false


  xMin = 0.001
  xMax = 100
  xRange = (xMin, xMax)

  yMin = 0.001
  yMax =  peakPerformances.head._2.value *1.3
  yRange = (yMin, yMax)

  logX = true
  logY = true
}
