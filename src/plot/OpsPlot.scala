package perfplot
package plot

import perfplot.quantities._
/**
 * Georg Ofenbeck
 First created:
 * Date: 24/01/13
 * Time: 14:03 
 */
case class OpsPlot (series : List[OperationSeries]) extends Plot2D
{
  val autoscaleX = true
  val autoscaleY = true
  val min = series.map (single_series => single_series.series.map( point => point.problemSize).min).min -1
  xMin = if (min < 0) 0 else min
  xMax =series.map (single_series => single_series.series.map( point => point.problemSize).max).max * 1.1

  xRange = (xMin, xMax)

  yMin = 1

  //val maxis = series.map(singles => singles.series.map (point => point.getOperations).toList).transpose.map(x => x.max)
  //val minis = series.map(singles => singles.series.map (point => point.getOperations).toList).transpose.map(x => x.min)


  yMax = 2
  //series.map (single_series => single_series.series.map( point => point.measurments.map( op => op.value).max).max).max * 1.1
  yRange = (yMin, yMax)

  logX = true
  logY = false
}
