/** 
 * Georg Ofenbeck
 First created:
 * Date: 12/03/13
 * Time: 22:59 
 */

import HWCounters.Counter
import org.scalatest.Suite

import perfplot.Config
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._




import java.io._
import scala.io._

class Validation extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_optimization + Config.flag_novec
  val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_mkl + Config.flag_optimization

  val folder = new File (Config.result_folder + File.separator + "validate" + File.separator)

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
  )

  def test_dgemm_seq() =
  {
    val sizes =  (for (i<-1 until 7) yield (i*100).toLong ).toList

    CodeGeneration.run_kernel(folder,CodeGeneration.dgemm_MKL,sizes,"dgemm-cold",counters,true,false, seq)
    //CodeGeneration.run_kernel(folder,CodeGeneration.sixfold_loop,sizes,"6fold-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(folder,CodeGeneration.tripple_loop,sizes,"triple",counters,true,false, seq)
  }


  def test_daxpy_seq() =
  {
    val sizes_2power =  (for (i<-1 until 7) yield (i*1000000).toLong ).toList

    CodeGeneration.run_kernel(folder,CodeGeneration.daxpy_MKL,sizes_2power,"daxpy-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(folder,CodeGeneration.daxpy_loop,sizes_2power,"daxpy-naive",counters,true,false, seq)

  }

  def test_dgmev_seq() =
  {
    val sizes_2power =  (for (i<-1 until 7) yield (i*100).toLong ).toList

    CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_loop,sizes_2power,"dgemv-naive",counters,true,false, seq)


  }
}

