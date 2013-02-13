import java.io.PrintStream
import org.scalatest.Suite

import perfplot._
import perfplot.services._
import perfplot.plot._
import java.io._
import scala.io._
import perfplot.quantities._


class TestTLB extends Suite {

    val DTLB_LOAD_MISSES_WALK_EVTNR = "0x08"
    val DTLB_LOAD_MISSES_WALK_UMASK = "0x81"

    val DTLB_ACCESS_STLB_HIT_EVTNR = "0x5F"
    val DTLB_ACCESS_STLB_HIT_UMASK = "0x01"

    val DTLB_STORE_MISSES_WALK_EVTNR = "0x49"
    val DTLB_STORE_MISSES_WALK_UMASK = "0x01"

    val DTLB_STORE_MISSES_STLB_HIT_EVTNR = "0x49"
    val DTLB_STORE_MISSES_STLB_HIT_UMASK = "0x10"

    val repeats = 10

    def FFTWCode (sourcefile: PrintStream, sizes: List[Long]) =
    {
        sourcefile.println(Config.MeasuringCoreH)
        sourcefile.println("#include <fftw3.h>")
        sourcefile.println("#include <iostream>")
        sourcefile.println("#include <cstdio>")
        sourcefile.println("#include <stdlib.h>")
        sourcefile.println("fftw_plan fftwPlan;")
        sourcefile.println("fftw_complex *fftwData;")

        sourcefile.println("int main () {\n    ")
        sourcefile.println("perfmon_init(11, true, false, false);")
        for (s <- sizes)
        {
            sourcefile.println("fftwData =  (fftw_complex*) fftw_malloc("+s+ " * sizeof(fftw_complex));")
            sourcefile.println("fftwPlan = fftw_plan_dft_1d("+s+", fftwData, fftwData,FFTW_FORWARD, FFTW_MEASURE);")
            sourcefile.println("for (long r =0; r < " + repeats + "; r++) {")
            sourcefile.println("perfmon_start();");
            for (x <- 0 until 100)
                sourcefile.println("fftw_execute(fftwPlan);")
            sourcefile.println("perfmon_stop();")
            sourcefile.println("std::cout << fftwData[0][0] ;")
            sourcefile.println("}")
            sourcefile.println("fftw_destroy_plan(fftwPlan);")
            sourcefile.println("free(fftwData);")
        }
        sourcefile.println("fftw_cleanup();")
        sourcefile.println("perfmon_end();")

        sourcefile.println("}")
    }

    def TriadCode (sourcefile: PrintStream) =
    {
        sourcefile.println(Config.MeasuringCoreH)
        sourcefile.println("#include <iostream>")
        sourcefile.println("#include <cstdlib>")

        sourcefile.println("double dummy;")

        sourcefile.println("int main () {\n    ")

        sourcefile.println("long counters[8];")
        sourcefile.println("counters[0] = " + DTLB_LOAD_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[1] = " + DTLB_LOAD_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[2] = " + DTLB_ACCESS_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[3] = " + DTLB_ACCESS_STLB_HIT_UMASK + ";")
        sourcefile.println("counters[4] = " + DTLB_STORE_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[5] = " + DTLB_STORE_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[6] = " + DTLB_STORE_MISSES_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[7] = " + DTLB_STORE_MISSES_STLB_HIT_UMASK + ";")

        sourcefile.println("perfmon_init(counters, 0, 0);")

        sourcefile.println("int size = 1000000;")
        sourcefile.println("double *a, *b, *c;")
        sourcefile.println("a = new double[size];")
        sourcefile.println("b = new double[size];")
        sourcefile.println("c = new double[size];")

        sourcefile.println("srand48(0);")

        sourcefile.println("for(int i = 0; i < size; ++i) {")
        sourcefile.println("  a[i] = 0.0; ")
        sourcefile.println("  b[i] = drand48(); ")
        sourcefile.println("  c[i] = drand48(); }")

        sourcefile.println("perfmon_start();");

        sourcefile.println("for(int rep = 0; rep < " + repeats + "; ++rep)")
        sourcefile.println("for(int i = 0; i < size; ++i)")
        sourcefile.println("  a[i] = b[i] + 2.71 * c[i];")

        sourcefile.println("perfmon_stop();")

        sourcefile.println("perfmon_end();")

        sourcefile.println("dummy = a[2];" )

        sourcefile.println("delete[] a;")
        sourcefile.println("delete[] b;")
        sourcefile.println("delete[] c;")

        sourcefile.println("return 0;" )
        sourcefile.println("}")
    }

    def MMMCode (sourcefile: PrintStream) =
    {
        sourcefile.println(Config.MeasuringCoreH)
        sourcefile.println("#include <iostream>")
        sourcefile.println("#include <cstdlib>")

        sourcefile.println("double dummy;")
        sourcefile.println("long counters[8];")

        sourcefile.println("int main () {\n    ")

        sourcefile.println("counters[0] = " + DTLB_LOAD_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[1] = " + DTLB_LOAD_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[2] = " + DTLB_ACCESS_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[3] = " + DTLB_ACCESS_STLB_HIT_UMASK + ";")
        sourcefile.println("counters[4] = " + DTLB_STORE_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[5] = " + DTLB_STORE_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[6] = " + DTLB_STORE_MISSES_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[7] = " + DTLB_STORE_MISSES_STLB_HIT_UMASK + ";")

        sourcefile.println("perfmon_init(counters, 0, 0);")

        sourcefile.println("int size = 400;")
        sourcefile.println("double *a, *b, *c;")
        sourcefile.println("a = new double[size*size];")
        sourcefile.println("b = new double[size*size];")
        sourcefile.println("c = new double[size*size];")

        sourcefile.println("srand48(0);")

        sourcefile.println("for(int i = 0; i < size*size; ++i) {")
        sourcefile.println("  c[i] = 0.0; ")
        sourcefile.println("  a[i] = drand48(); ")
        sourcefile.println("  b[i] = drand48(); }")

        sourcefile.println("perfmon_start();");

        sourcefile.println("for(int rep = 0; rep < " + repeats + "; ++rep)")
        sourcefile.println("for(int i = 0; i < size; ++i) {")
        sourcefile.println("for(int j = 0; j < size; ++j) {")
        sourcefile.println("for(int k = 0; k < size; ++k) {")
        sourcefile.println("  c[i*size+j] += a[i*size+k] * b[k*size+j];")
        sourcefile.println(" } } }")

        sourcefile.println("perfmon_stop();")
        sourcefile.println("perfmon_end();")


        sourcefile.println("dummy = c[1];" )

        sourcefile.println("delete[] a;")
        sourcefile.println("delete[] b;")
        sourcefile.println("delete[] c;")

        sourcefile.println("return 0;" )
        sourcefile.println("}")
    }

    def TlbBoundCode (sourcefile: PrintStream) =
    {
        val rows = 650;
        val cols = 512;

        sourcefile.println(Config.MeasuringCoreH)
        sourcefile.println("#include <iostream>")
        sourcefile.println("#include <cstdlib>")
        sourcefile.println("#include <pmmintrin.h>")

        sourcefile.println("double dummy;")
        sourcefile.println("long counters[8];")

        sourcefile.println("int main () {\n    ")

        sourcefile.println("counters[0] = " + DTLB_LOAD_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[1] = " + DTLB_LOAD_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[2] = " + DTLB_ACCESS_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[3] = " + DTLB_ACCESS_STLB_HIT_UMASK + ";")
        sourcefile.println("counters[4] = " + DTLB_STORE_MISSES_WALK_EVTNR + ";")
        sourcefile.println("counters[5] = " + DTLB_STORE_MISSES_WALK_UMASK + ";")
        sourcefile.println("counters[6] = " + DTLB_STORE_MISSES_STLB_HIT_EVTNR + ";")
        sourcefile.println("counters[7] = " + DTLB_STORE_MISSES_STLB_HIT_UMASK + ";")

        sourcefile.println("int rows = " + rows + ";")
        sourcefile.println("int cols = " + cols + ";")

        sourcefile.println("perfmon_init(counters, 0, 0);")

        sourcefile.println("int size = 400;")
        sourcefile.println("double *a;")
        sourcefile.println("a = new double[rows*cols];")

        sourcefile.println("srand48(0);")

        sourcefile.println("for(int i = 0; i < size*size; ++i) {")
        sourcefile.println("  a[i] = drand48(); }")

        sourcefile.println("__m128d val, v1;")
        sourcefile.println("v1 = _mm_set1_pd(0.0);")

        sourcefile.println("perfmon_start();");

        sourcefile.println("for(int rep = 0; rep < " + repeats + "; ++rep)")
        sourcefile.println("for(int j = 0; j < cols; ++j) {")
        sourcefile.println("for(int i = 0; i < rows; ++i) {")
        sourcefile.println("  val = _mm_set_sd(a[i*cols+j]);")
        sourcefile.println("  v1 = _mm_add_sd(v1, val);")
        sourcefile.println(" } }")

        sourcefile.println("perfmon_stop();")
        sourcefile.println("perfmon_end();")


        sourcefile.println("_mm_store_sd(&dummy, v1);" )

        sourcefile.println("delete[] a;")

        sourcefile.println("return 0;" )
        sourcefile.println("}")
    }

    def test01 {
        //val sizes = (for (i<-2 until 13) yield Math.pow(2,i).toLong).toList
        val sizes: List[Long] = List(1024,4096)
        def test (sourcefile: PrintStream) = FFTWCode(sourcefile,sizes)

        val test_res = CommandService.fromScratch("test", TlbBoundCode, Config.flag_c99 + Config.flag_optimization + Config.flag_hw  + " -lfftw3 -lm -mfpmath=sse" )
        test_res.prettyprint()
    }
}