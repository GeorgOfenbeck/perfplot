package perfplot
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
case class Flops(value: Double) extends OperationCount
case class PseudoFlops(value: Double) extends OperationCount
case class Fladds(value: Double) extends OperationCount
case class Flmults(value: Double) extends OperationCount


trait ByteCount extends Quantity
case class TransferredBytes(value: Double) extends ByteCount


case class OperationalIntensity( ops: OperationCount, bytes: ByteCount)
{
  val value = ops.value / bytes.value
}

case class Performance( ops : OperationCount, time: Time) extends Quantity
{
  val value = ops.value/time.value
}

case class Throughput (bytes: ByteCount, time: Time) extends Quantity
{
  val value = bytes.value/time.value
}

