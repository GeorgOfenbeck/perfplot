package roofline
package quantities

/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 13:16 
 */
abstract class Quantity { val value : Double}

trait Time extends Quantity
case class Cycles(value: Double) extends Time
case class Seconds(value: Double) extends Time

trait OperationCount extends Quantity
case class flops(value: Double) extends OperationCount
case class fladds(value: Double) extends OperationCount
case class flmults(value: Double) extends OperationCount

case class Performance( ops : OperationCount, time: Time) extends Quantity
{
  val value = ops.value/time.value
}