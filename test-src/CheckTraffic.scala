/** 
 * Georg Ofenbeck
 First created:
 * Date: 04/03/13
 * Time: 14:53 
 */
import HWCounters.Counter
import org.scalatest.Suite

import perfplot.Config
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._



import java.io._
import scala.io._



class CheckTraffic extends Suite{







  def test()=
  {
    val folder = new File (Config.result_folder + File.separator + "test_traffic" + File.separator)

    val counters = Array(
      Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
      Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
      Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
      Counter("D3H","01H","MEM_LOAD_UOPS_LLC_MISS_ RETIRED.LOCAL_DRAM","Retired load uops which data sources were data missed LLC but serviced by local DRAM.","Supports PEBS")
    )

    val sizes_2power =  (for (i<-1 until 100) yield (i*1000).toLong).toList
    CodeGeneration.run_kernel(folder,mydaxpy,sizes_2power,"mydgemv",counters,true,false, Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_no_optimization )
  }


def mydaxpy(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
{
  def p(x: String) = sourcefile.println(x)
  val prec = if (double_precision) "double" else "float"
  p("#include <mkl.h>")
  p("#include <iostream>")
  p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
  p(Config.MeasuringCoreH)
  p("#define page 4096")
  p("#define THRESHOLD " + Config.testDerivate_Threshold)
  p("using namespace std;")
  val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
  CodeGeneration.create_array_of_buffers(sourcefile)
  CodeGeneration.destroy_array_of_buffers(sourcefile)


  p("void mydaxpy(double * x, double * y, double * z,long size) {")
  p("double result = 0;")
  p("for (int i = 0; i< size;i++)")
  p("result += x[i] + y[i] + z[i]; }")


  p("int main () { ")
  p(counterstring)
  p(initstring)
  for (size <- sizes)
  {
    p("{")
    p("double alpha = 1.1;")

    //allocate

    p("int n = " +size + ";")
    //Tune the number of runs
    p("std::cout << \"tuning\";")
    //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
    //CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"mydaxpy( x,y, z,"+size+ ");","" )
    p("long runs = 100*1024*1024/(3*8*"+size+");")
    p("std::cout << \" Runs: \" << runs << \"- "+size+ " --\"; ")
    //find out the number of shifts required
    //p("std::cout << runs << \"allocate\";")
    //allocate the buffers
    //p("std::cout << \"run\";")
    if (!warmData)
    {

      //allocate
      p("long numberofshifts =  2*measurement_getNumberOfShifts(" + (size+size+size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
      p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
      p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
      p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
      p("double ** z_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
      p("for(int i = 0; i < runs; i++){")
      p("mydaxpy(x_array[i%numberofshifts],y_array[i%numberofshifts], z_array[i%numberofshifts],"+size+ ");")
      p("}")
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("measurement_start();")
      p("for(int i = 0; i < runs; i++){")
      p("mydaxpy(x_array[i%numberofshifts],y_array[i%numberofshifts], z_array[i%numberofshifts],"+size+ ");")
      p("}")
      p( "measurement_stop(runs);")
      p( " }")
      p("DestroyBuffers( (void **) x_array, numberofshifts);")
      p("DestroyBuffers( (void **) y_array, numberofshifts);")
      p("DestroyBuffers( (void **) z_array, numberofshifts);")
    }
    else
    {
      //run it
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("measurement_start();")
      p("for(int i = 0; i < runs; i++){")
      p("cblas_daxpy("+size+", alpha, x, 1, y, 1);")
      p("}")
      p( "measurement_stop(runs);")
      p( " }")
      p("std::cout << \"deallocate\";")
      //deallocate the buffers
      p("_mm_free(x);")
      p("_mm_free(y);")
    }
    p("}")
  }
  p("measurement_end();")
  p("}")
}

}

