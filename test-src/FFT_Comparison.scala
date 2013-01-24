/** 
 * Georg Ofenbeck
 First created:
 * Date: 24/01/13
 * Time: 11:03 
 */
import java.io.PrintStream
import org.scalatest.Suite

import roofline._
import roofline.services._
import roofline.plot._
import java.io._
import scala.io._
import roofline.quantities._


class FFT_Comparison extends Suite{

  val repeats = 3


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
    sourcefile.println("perfmon_init(1,false,false,false);")
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


  def SpiralCode(sourcefile: PrintStream, sizes: List[Long]) =
  {
    //this assumes that the code is in the home directory under ~/fft_scalar
    sourcefile.println(Config.MeasuringCoreH)
    sourcefile.println("#include \"/home/ofgeorg/fft_scalar/spiral_fft.h\"\n    #include \"/home/ofgeorg/fft_scalar/spiral_private.h\"\n    #include \"/home/ofgeorg/fft_scalar/spiral_private.c\"\n    #include \"/home/ofgeorg/fft_scalar/spiral_fft_double.c\"")
    sourcefile.println("#include <iostream>")
    sourcefile.println("#include <cstdio>")
    sourcefile.println("#include <stdlib.h>")
    sourcefile.println("int main () {\n    ")
    sourcefile.println("perfmon_init(1,false,false,false);")
    sourcefile.println("double * pSrc; double * pDst;")
    sourcefile.println("spiral_status_t status;")
    sourcefile.println("std::string statusStr;")
    sourcefile.println("const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n ")
    for (s <- sizes)
    {
      sourcefile.println("pSrc = (double*) _mm_malloc( "+s+ " *2*sizeof(double), page );")
      sourcefile.println("if (!pSrc) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }")
      sourcefile.println("pDst = (double*) _mm_malloc( "+s+ " *2*sizeof(double), page );")
      sourcefile.println("if (!pDst) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }")
      sourcefile.println("for (long r =0; r < " + repeats + "; r++) {")
      sourcefile.println("perfmon_start();");
      for (x <- 0 until 100)
        sourcefile.println("status = spiral_fft_double("+ s + ", 1, pSrc, pDst);")
      sourcefile.println("perfmon_stop();")
      sourcefile.println("  switch (status) {\n    case SPIRAL_SIZE_NOT_SUPPORTED:\n    statusStr = \"SIZE_NOT_SUPPORTED\";\n    break;\n    case SPIRAL_INVALID_PARAM:\n      statusStr = \"SPIRAL_INVALID_PARAM\";\n    break;\n    case SPIRAL_OUT_OF_MEMORY:\n      statusStr = \"SPIRAL_OUT_OF_MEMORY\";\n    break;\n    case SPIRAL_OK:\n      statusStr = \"worked!\";\n    break;\n  }")
      sourcefile.println("std::cout << status << pDst[0];")
      sourcefile.println("}")
      sourcefile.println("_mm_free(pSrc);")
      sourcefile.println("_mm_free(pDst);")
    }
    sourcefile.println("perfmon_end();")
    sourcefile.println("}")
  }




  def test() =
  {
    val sizes = (for (i<-6 until 13) yield Math.pow(2,i).toLong).toList
      def fftw (sourcefile: PrintStream) = FFTWCode(sourcefile,sizes)
      def spiral (sourcefile: PrintStream) = SpiralCode (sourcefile,sizes)
    val fftw_res = CommandService.fromScratch("fftw", fftw, " -lfftw3 -lm " )
    val spiral_res = CommandService.fromScratch("spiral", spiral )
    
    val spiral_ops_series = spiral_res.toFlopSeries("spiral",sizes)
    val fftw_ops_series = fftw_res.toFlopSeries("fftw",sizes)

    val opsplot = new OpsPlot(List(fftw_ops_series,spiral_ops_series))
    opsplot.outputName = "TestOps"
    opsplot.title = "Op Count"
    opsplot.xLabel = "size"
    opsplot.yLabel = "Ops"
    opsplot.xUnit = "# Complex Doubles"
    opsplot.yUnit = "Flops"


    val spiral_perf_series = spiral_res.toPerformanceSeries("spiral",sizes)
    val fftw_perf_series = fftw_res.toPerformanceSeries("fftw",sizes)


    val perfplot = new PerformancePlot(List(fftw_perf_series,spiral_perf_series),
      List(
        ("Scalar",Performance(Flops(2),Cycles(1)))
      ))
    perfplot.outputName = "Performance"
    perfplot.title = "Performance Comparison"
    perfplot.xLabel = "size"
    perfplot.yLabel = "Performance"
    perfplot.xUnit = "# Complex Doubles"
    perfplot.yUnit = "flops/cycle"








    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(opsplot)
    ps.plot(perfplot)

    spiral_res.prettyprint()
  }

}
