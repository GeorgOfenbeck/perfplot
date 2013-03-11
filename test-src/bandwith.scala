/** 
 * Georg Ofenbeck
 First created:
 * Date: 07/03/13
 * Time: 21:00 
 */
import HWCounters.Counter
import org.scalatest.Suite

import perfplot.Config
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._




import java.io._
import scala.io._

class bandwidth extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_optimization // + Config.flag_novec
//   val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_optimization

  val folder = new File (Config.result_folder + File.separator + "bandwidth" + File.separator)

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
  )

    val sizes : List[Long] = List(16*8*1024*1024)

//   def test_read_loop() =
//   {
//     CodeGeneration.run_kernel(folder,CodeGeneration.read_loop,sizes,"readloop",counters,false,false, seq) // First bool value enables/disables parallelism
//   }

  def test_read_loop_par() =
  {
    CodeGeneration.run_kernel(folder,CodeGeneration.read_loop,sizes,"readloop-par",counters,true,false, seq + " -openmp ")
  }

//   def test_copy_loop() =
//   {
//     CodeGeneration.run_kernel(folder,CodeGeneration.copy_loop,sizes,"copy",counters,true,false, seq + Config.flag_mkl_seq)
//   }
// 
//   def test_copy_loop_par() =
//   {
//     CodeGeneration.run_kernel(folder,CodeGeneration.copy_loop,sizes,"copy-par",counters,true,false, seq + Config.flag_mkl)
//   }

}
