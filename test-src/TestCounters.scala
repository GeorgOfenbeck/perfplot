/** 
 * Georg Ofenbeck
 First created:
 * Date: 22/01/13
 * Time: 16:17 
 */


import org.scalatest.Suite

import roofline._
import roofline.plot._
import roofline.quantities._
import roofline.services._


import java.io._
import scala.io._

class TestCounters extends Suite{


  def print_def_code(sourcefile: PrintStream) =
  {
    sourcefile.println("#include <iostream>")
    sourcefile.println(Config.MeasuringCoreH)
    sourcefile.println("int main () {\n    ")
    sourcefile.println("perfmon_init(1,false,false,false);")
    sourcefile.println("flushTLB();")
    sourcefile.println("flushCache();")
    sourcefile.println("flushICache();")
    sourcefile.println("perfmon_start();")
    sourcefile.println("perfmon_stop();")
    sourcefile.println("perfmon_end();")
    sourcefile.println("}")
  }

  def test_Compilation () =
  {
    val filename = "testcompile"
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
    sourcefile.println("#include <omp.h>")
    print_def_code(sourcefile)
    sourcefile.close()
    CommandService.compile(tempdir.getPath + File.separator +  filename, "")
  }

 def test_execution () =
 {
   val filename = "testexecution"
   val tempdir = CommandService.getTempDir(filename)
   val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
   print_def_code(sourcefile)
   sourcefile.close()
   CommandService.compile(tempdir.getPath + File.separator +  filename, "")
   val path = CommandService.measureCode(tempdir, filename)
   CommandService.Counters(path)
 }

 def test_op_count () =
 {
   val path = RooflineService.get_vectorized_peak()
   val res = CommandService.Counters(path)
   println("Perf:")
   println(res.getPerformance(1))

 }



}
