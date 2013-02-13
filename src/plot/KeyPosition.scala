/** 
 * Georg Ofenbeck
 First created:
 * Date: 14/12/12
 * Time: 16:10 
 */

package perfplot
package plot

object KeyPosition extends Enumeration {
  val TopLeft, TopRight, BottomLeft, BottomRight, NoKey, Undefined = Value
}

object SameSizeConnection extends Enumeration{
  val None, ByPerformance, ByOperationalIntensity = Value
}