package roofline
package services

/**
 * Georg Ofenbeck
 First created:
 * Date: 23/01/13
 * Time: 10:53 
 */

import java.io._
import scala.io._

object RooflineService {



  def peak_def_code(sourcefile: PrintStream, threaded: Boolean, vectorized: Boolean) =
  {
    val DLP = 5;
    val unrolling = 100;
    val nrthreads = 24;
    val repeats = 2;
    sourcefile.println("#include <iostream>\n #include \"immintrin.h\" ")
    sourcefile.println(Config.MeasuringCoreH)
    sourcefile.println("int main () {\n    ")
    sourcefile.println("perfmon_init(1,false,false,false);")
    sourcefile.println("  const long DLP = "+DLP+";\n  const long iterations = 200000000;double result = 0;\n  double result2 = 0;")
    sourcefile.println("for (long r =0; r < " + repeats + "; r++) {")
      sourcefile.println("perfmon_start();");
      if (threaded)
        sourcefile.println("for (long p = 0; p <  "+ nrthreads + "; p++){")
      sourcefile.println("double tmp[2];")
      sourcefile.println("__m128d a[DLP], b[DLP], c,d;")
      sourcefile.println("double t;\n\n    t = 1.1;\n\n    for (int i = 0; i < DLP; i++) {\n      tmp[0] = t;\n      t += 0.1;\n      tmp[1] = t;\n      t += 0.1;\n      a[i] = _mm_loadu_pd(tmp);\n      b[i] = _mm_loadu_pd(tmp);\n    }\n\n    tmp[0] = 1;\n    tmp[1] = 1;\n    c = _mm_loadu_pd(tmp);\n    d = _mm_loadu_pd(tmp);")

      sourcefile.println("for (long i = 0; i < iterations/ "+ (unrolling * DLP) + "; i++) {")

        for (i <- 0 until unrolling)
          for (j <- 0 until DLP)
          {
            if (vectorized)
            {
              sourcefile.println("\ta["+j+"] = _mm_add_pd(a["+j+"], c);")
              sourcefile.println("\tb["+j+"] = _mm_mul_pd(b["+j+"], d);")
            }
            else
            {
              sourcefile.println("\ta["+j+"] = _mm_add_sd(a["+j+"], c);")
              sourcefile.println("\tb["+j+"] = _mm_mul_sd(b["+j+"], d);")
            }
          }
      sourcefile.println("}")
      sourcefile.println("for (int i = 0; i < 10; i++) {\n  _mm_storeu_pd(tmp, a[i]);\t\t\t\t\t\n  result += tmp[0];\n  result += tmp[1];\n  _mm_storeu_pd(tmp, b[i]);\n  result2 += tmp[0];\n  result2 += tmp[1];}")
      if (threaded)
        sourcefile.println("}")
      sourcefile.println("perfmon_stop();")

      sourcefile.println("\n\t\t\t\t\n  std::cout << result;\n  std::cout << result2;")
    sourcefile.println("}")
    sourcefile.println("std::cout << \"iterations: \" <<  iterations ;")
    sourcefile.println("perfmon_end();")
    sourcefile.println("}")
  }

  def get_peak_performances() =
  {

  }


  def get_scalar_peak () =
  {
    val filename = "get_peak_scalar"
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
    peak_def_code(sourcefile,false,false)
    sourcefile.close()
    CommandService.compile(tempdir.getPath + File.separator +  filename, "")
    val resultdir = CommandService.measureCode(tempdir, filename)
    resultdir
  }

  def get_vectorized_peak (): File =
  {
    val filename = "get_peak_SSE"
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
    peak_def_code(sourcefile,false,true)
    sourcefile.close()
    CommandService.compile(tempdir.getPath + File.separator +  filename, "")
    val resultdir = CommandService.measureCode(tempdir, filename)
    resultdir
  }


  def get_multithreaded_peak() =
  {

  }

}
