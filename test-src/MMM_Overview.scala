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

class MMM_Overview extends Suite{


  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_optimization
  val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_mkl + Config.flag_optimization

  val folder = new File (Config.result_folder + File.separator + "mmm_overview" + File.separator)

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
  )

  val sizes: List[Long] = List(100,600,1100,1600,2100,2600,3100)
  /*
  def test_tripple_loop() =
  {

    //val sizes: List[Long] = List(1500)
    CodeGeneration.run_kernel(folder,CodeGeneration.tripple_loop,sizes,"tripple-cold",counters,true,false, seq)
  } */

  def test_6_fold( ) =
  {
    CodeGeneration.run_kernel(folder,CodeGeneration.sixfold_loop,sizes,"6fold-cold",counters,true,false, seq)
  }


  def test_Atlas() =
  {
    CodeGeneration.run_kernel(folder,CodeGeneration.dgemm_Atlas,sizes,"Atlas-cold",counters,true,false, seq + " /home/ofgeorg/Atlas/lib/libcblas.a /home/ofgeorg/Atlas/lib/liblapack.a /home/ofgeorg/Atlas/lib/libatlas.a -I/home/ofgeorg/Atlas/include -I/home/ofgeorg/Atlas/include/atlas")
    CodeGeneration.run_kernel(folder,CodeGeneration.dgemm_Atlas,sizes,"Atlas-parallel-cold",counters,true,false, seq + " /home/ofgeorg/Atlas/lib/libptcblas.a /home/ofgeorg/Atlas/lib/liblapack.a /home/ofgeorg/Atlas/lib/libatlas.a -I/home/ofgeorg/Atlas/include -I/home/ofgeorg/Atlas/include/atlas")
  }


  def test_dgemm_seq() =
  {

    CodeGeneration.run_kernel(folder,CodeGeneration.dgemm_MKL,sizes,"dgemm-cold",counters,true,false, seq + Config.flag_mkl_seq)
    CodeGeneration.run_kernel(folder,CodeGeneration.dgemm_MKL,sizes,"dgemm-parallel-cold",counters,true,false, parallel)
  }




}
