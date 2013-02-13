/** 
 * Georg Ofenbeck
 First created:
 * Date: 14/12/12
 * Time: 16:02 
 */
package perfplot
package plot

abstract class Plot2D extends Plot{
  var yRange = (Double.MinValue,Double.MaxValue)
  var xRange = (Double.MinValue,Double.MaxValue)

  var xLabel: String = _
  var xUnit: String = _
  var yLabel: String = _
  var yUnit : String = _
  var logX: Boolean = _
  var logY: Boolean = _


  var xMin: Double = _
  var xMax: Double = _
  var yMin: Double = _
  var yMax: Double = _

  var keyPosition = KeyPosition.TopLeft
}
