/** 
 * Georg Ofenbeck
 First created:
 * Date: 14/12/12
 * Time: 16:19 
 */

package roofline
package plot

class SimplePlot extends Plot2D{
  xMin = 0.0
  xMax = 100.0
  yMin = 0.0
  yMax = 100.0
  var values : List[Double] = List()

  def apply(v : Double) {
    values =  v :: values
  }
}

