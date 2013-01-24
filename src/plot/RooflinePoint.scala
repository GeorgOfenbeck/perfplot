package roofline
package plot
import roofline.quantities._

/**
 * Georg Ofenbeck
 First created:
 * Date: 16/01/13
 * Time: 14:08 
 */
case class RooflinePoint (problemSize: Long, measurments: List[(Performance,OperationalIntensity)], label: String = "")
{
  def getMedianPerformance =
  {
    val values = measurments map (x => x._1.value)
    println("perf: " + problemSize)
    println(values)
    val sum = values.sum
    sum/measurments.size
  }
  def getMedianIntensity =
  {
    val values = measurments map (x => x._2.value)
    println("Intense:" + problemSize)
    println(values)
    val sum = values.sum
    sum/measurments.size
  }
}

case class PerformancePoint (problemSize: Long, measurments: List[Performance], label: String = "")
{
  def getAVG = measurments.map(x=>x.value).sum/measurments.size
}
case class OperationPoint (problemSize: Long, measurments: List[OperationCount], label: String = "")
{
  def getOperations = measurments.map(x=>x.value).sum/measurments.size
}




case class OperationSeries (name: String, series: List[OperationPoint])
case class PerformanceSeries (name: String, series: List[PerformancePoint])
case class RooflineSeries (name: String, series: List[RooflinePoint])

