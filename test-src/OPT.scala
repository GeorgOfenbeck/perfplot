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

class OPT extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_optimization + Config.flag_novec

  val folder = new File (Config.result_folder + File.separator + "daxpy_overview" + File.separator)

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
  )

/*
  def test_daxpy_loop() =
  {
  //  val sizes: List[Long] = List(100,600,1100,1600,2100,2600,3100)
	//val sizes: List[Long] = List(64,128,256,512,1024)
    //val sizes: List[Long] = List(100,200,500,,1000, 1500)
    val sizes =  (for (i<-0 until 31) yield (i*100+100).toLong ).toList
    //val sizes: List[Long] = List(1500)
    CodeGeneration.run_kernel(folder,CodeGeneration.daxpy_loop,sizes,"daxpy-cold",counters,true,false, seq)
  }

  def test_copy_loop() =
  {
    val sizes =  (for (i<-0 until 1) yield (i*100+100).toLong ).toList
    CodeGeneration.run_kernel(folder,CodeGeneration.copy_loop,sizes,"copy-cold",counters,true,false, seq)
  }

  def test_scale_loop() =
  {
    val sizes =  (for (i<-0 until 1) yield (i*100+100).toLong ).toList
    CodeGeneration.run_kernel(folder,CodeGeneration.scale_loop,sizes,"scale-cold",counters,true,false, seq)
  }
*/

  def test_add_loop() =
  {
    val sizes =  (for (i<-0 until 3) yield (i*10+10).toLong ).toList
    CodeGeneration.run_kernel(folder,CodeGeneration.add_loop,sizes,"add-cold",counters,true,false, seq)
  }

}
