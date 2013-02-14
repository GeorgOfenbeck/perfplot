

/**
 * Georg Ofenbeck
 First created:
 * Date: 24/01/13
 * Time: 10:27 
 */
import java.io.PrintStream
import org.scalatest.Suite
import perfplot._
import perfplot.services._
import java.io._
import scala.io._


class CheckFlops extends Suite{


  def check_code_head (sourcefile: PrintStream, vectorcode: ( (PrintStream,Int) => Unit)) =
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

    sourcefile.println("double tmp[2];")
    sourcefile.println("__m128d a[DLP], b[DLP], c,d;")
    sourcefile.println("double t;\n\n    t = 1.1;\n\n    for (int i = 0; i < DLP; i++) {\n      tmp[0] = t;\n      t += 0.1;\n      tmp[1] = t;\n      t += 0.1;\n      a[i] = _mm_loadu_pd(tmp);\n      b[i] = _mm_loadu_pd(tmp);\n    }\n\n    tmp[0] = 1;\n    tmp[1] = 1;\n    c = _mm_loadu_pd(tmp);\n    d = _mm_loadu_pd(tmp);")

    sourcefile.println("for (long i = 0; i < iterations/ "+ (unrolling * DLP) + "; i++) {")

    for (i <- 0 until unrolling)
      for (j <- 0 until DLP)
      {
          vectorcode(sourcefile,j)
      }
    sourcefile.println("}")
    sourcefile.println("for (int i = 0; i < 10; i++) {\n  _mm_storeu_pd(tmp, a[i]);\t\t\t\t\t\n  result += tmp[0];\n  result += tmp[1];\n  _mm_storeu_pd(tmp, b[i]);\n  result2 += tmp[0];\n  result2 += tmp[1];}")

    sourcefile.println("perfmon_stop();")

    sourcefile.println("\n\t\t\t\t\n  std::cout << result;\n  std::cout << result2;")
    sourcefile.println("}")
    sourcefile.println("std::cout << \"iterations: \" <<  iterations ;")
    sourcefile.println("perfmon_end();")
    sourcefile.println("}")
  }



  def check_shuffles()=
  {
    val filename = "checkshuffles"
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")

    val shufflecode : ( (PrintStream,Int) => Unit) = (sourcefile,j) =>
    {
      sourcefile.println("\ta[0] = _mm_shuffle_pd(a[0],b[0],_MM_SHUFFLE2(1,0));")
      sourcefile.println("\tb[0] = _mm_shuffle_pd(a[0],b[0],_MM_SHUFFLE2(0,1));")
      sourcefile.println("\ta["+j+"] = _mm_add_pd(a["+j+"], c);")
      sourcefile.println("\tb["+j+"] = _mm_mul_pd(b["+j+"], d);")
    }
    check_code_head(sourcefile,shufflecode)
    sourcefile.close()
    CommandService.compile(tempdir.getPath + File.separator +  filename, "")
    val resultdir = CommandService.measureCode(tempdir, filename)
    resultdir
  }


  def test_if_shuffles_count =
  {
    CommandService.Counters(check_shuffles()).prettyprint()
  }

}
