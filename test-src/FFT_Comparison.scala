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

  val repeats = 20


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
    if (Config.isWin)
      sourcefile.println("#include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_fft.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_private.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_private.c\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_fft_double.c\"")
    else
      sourcefile.println("#include \""+Config.home+"/fft_scalar/spiral_fft.h\"\n    #include \""+Config.home+"/fft_scalar/spiral_private.h\"\n    #include \""+Config.home+"/fft_scalar/spiral_private.c\"\n    #include \""+Config.home+"/fft_scalar/spiral_fft_double.c\"")
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


  def SpiralSCode(sourcefile: PrintStream, sizes: List[Long],includepath: String) =
  {
    //this assumes that the code is in the home directory under ~/fft_scalar
    sourcefile.println(Config.MeasuringCoreH)
    sourcefile.println("#include <iostream>")
    sourcefile.println("#include <cstdio>")
    sourcefile.println("#include <stdlib.h>")
    sourcefile.println("typedef struct {\n        double* input;\n        double* output;\n} spiral_t;")


    for (s <- sizes)
      //sourcefile.println("void fft"+s+"(spiral_t* );")
              //sourcefile.println("#include \"/home/ofgeorg/SpiralS_generated/fft"+s+".c\"")
        sourcefile.println(includepath + "fft"+s+".c\"")

    sourcefile.println("int main () {\n    ")
    sourcefile.println("perfmon_init(1,false,false,false);")
    sourcefile.println("double * pSrc; double * pDst;")
    sourcefile.println("spiral_t dummy;")
    sourcefile.println("const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n ")
    for (s <- sizes)
    {
      sourcefile.println("pSrc = (double*) _mm_malloc( "+s+ " *2*sizeof(double), page );")
      sourcefile.println("if (!pSrc) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }")
      sourcefile.println("pDst = (double*) _mm_malloc( "+s+ " *2*sizeof(double), page );")
      sourcefile.println("if (!pDst) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }")
      sourcefile.println("for (long r =0; r < " + repeats + "; r++) {")
      sourcefile.println("        dummy.input = pSrc;\n        dummy.output = pDst;")
      sourcefile.println("perfmon_start();");
      for (x <- 0 until 100)
        sourcefile.println("fft"+ s + "(&dummy);")
      sourcefile.println("perfmon_stop();")
      sourcefile.println("std::cout << pDst[0];")
      sourcefile.println("}")
      sourcefile.println("_mm_free(pSrc);")
      sourcefile.println("_mm_free(pDst);")
    }
    sourcefile.println("perfmon_end();")
    sourcefile.println("}")
  }



  def test() =
  {
    val sizes = (for (i<-2 until 13) yield Math.pow(2,i).toLong).toList

    val path2 =
      if(Config.isWin)
        "C:\\Users\\ofgeorg\\SpiralS_generated\\"
      else
        Config.home +  "/SpiralS_generated/"

    val path = "#include \"" + path2

    def fftw (sourcefile: PrintStream) = FFTWCode(sourcefile,sizes)
    def spiral (sourcefile: PrintStream) = SpiralCode (sourcefile,sizes)
    //def spiralS (sourcefile: PrintStream) = SpiralSCode (sourcefile,sizes)

    def spiralS_16 (sourcefile: PrintStream) = SpiralSCode (sourcefile,sizes,path+ "unroll17/")
    def spiralS_32 (sourcefile: PrintStream) = SpiralSCode (sourcefile,sizes,path+ "unroll33/")

    def spiralS_16_linear (sourcefile: PrintStream) = SpiralSCode (sourcefile,sizes,path+ "unroll17_linear/")
    def spiralS_32_linear (sourcefile: PrintStream) = SpiralSCode (sourcefile,sizes,path+ "unroll33_linear/")


    val spiralS_16_res = CommandService.fromScratch("spiral", spiralS_16, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  )
    val spiralS_16_ops_series = spiralS_16_res.toFlopSeries("spiralS_16",sizes)
    val spiralS_16_perf_series = spiralS_16_res.toPerformanceSeries("spiralS_16",sizes)
    val spiralS_16_pseudo_perf_series = spiralS_16_res.toPerformanceSeries_fft("spiralS_16",sizes)

    val spiralS_32_res = CommandService.fromScratch("spiral", spiralS_32, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  )
    val spiralS_32_ops_series = spiralS_32_res.toFlopSeries("spiralS_32",sizes)
    val spiralS_32_perf_series = spiralS_32_res.toPerformanceSeries("spiralS_32",sizes)
    val spiralS_32_pseudo_perf_series = spiralS_32_res.toPerformanceSeries_fft("spiralS_32",sizes)

    val spiralS_16_linear_res = CommandService.fromScratch("spiral", spiralS_16_linear, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  )
    val spiralS_16_linear_ops_series = spiralS_16_linear_res.toFlopSeries("spiralS_16_linear",sizes)
    val spiralS_16_linear_perf_series = spiralS_16_linear_res.toPerformanceSeries("spiralS_16_linear",sizes)
    val spiralS_16_linear_pseudo_perf_series = spiralS_16_linear_res.toPerformanceSeries_fft("spiralS_16_linear",sizes)

    val spiralS_32_linear_res = CommandService.fromScratch("spiral", spiralS_32_linear, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  )
    val spiralS_32_linear_ops_series = spiralS_32_linear_res.toFlopSeries("spiralS_32_linear",sizes)
    val spiralS_32_linear_perf_series = spiralS_32_linear_res.toPerformanceSeries("spiralS_32_linear",sizes)
    val spiralS_32_linear_pseudo_perf_series = spiralS_32_linear_res.toPerformanceSeries_fft("spiralS_32_linear",sizes)

    /*
    val spiralS_res = CommandService.fromScratch("spiral", spiralS, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  )
    val spiralS_ops_series = spiralS_res.toFlopSeries("spiralS",sizes)
    val spiralS_perf_series = spiralS_res.toPerformanceSeries("spiralS",sizes)
    val spiralS_pseudo_perf_series = spiralS_res.toPerformanceSeries_fft("spiralS",sizes)
    */
    val spiral_res = CommandService.fromScratch("spiral", spiral ,Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec)
    val spiral_ops_series = spiral_res.toFlopSeries("spiral",sizes)
    val spiral_perf_series = spiral_res.toPerformanceSeries("spiral",sizes)
    val spiral_pseudo_perf_series = spiral_res.toPerformanceSeries_fft("spiral",sizes)

    val lists = if (!Config.isWin)
    {
      val fftw_res = CommandService.fromScratch("fftw", fftw, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -lfftw3 -lm " )
      val fftw_ops_series = fftw_res.toFlopSeries("fftw",sizes)
      val fftw_perf_series = fftw_res.toPerformanceSeries("fftw",sizes)
      val fftw_pseudo_perf_series = fftw_res.toPerformanceSeries_fft("fftw",sizes)

      //(List(spiralS_ops_series,fftw_ops_series,spiral_ops_series),List(spiralS_perf_series,fftw_perf_series,spiral_perf_series),List(spiralS_pseudo_perf_series,fftw_pseudo_perf_series,spiral_pseudo_perf_series))
     (
      List(fftw_ops_series,spiralS_16_ops_series,spiralS_32_ops_series,spiralS_16_linear_ops_series,spiralS_32_linear_ops_series,spiral_ops_series),
      List(fftw_perf_series,spiralS_16_perf_series,spiralS_32_perf_series,spiralS_16_linear_perf_series,spiralS_32_linear_perf_series,spiral_perf_series),
      List(fftw_pseudo_perf_series,spiralS_16_pseudo_perf_series,spiralS_32_pseudo_perf_series,spiralS_16_linear_pseudo_perf_series,spiralS_32_linear_pseudo_perf_series,spiral_pseudo_perf_series)
     )
    }
    else
      (List(spiralS_16_ops_series,spiralS_32_ops_series,spiralS_16_linear_ops_series,spiralS_32_linear_ops_series,spiral_ops_series),        
       List(spiralS_16_perf_series,spiralS_32_perf_series,spiralS_16_linear_perf_series,spiralS_32_linear_perf_series,spiral_perf_series),
       List(spiralS_16_pseudo_perf_series,spiralS_32_pseudo_perf_series,spiralS_16_linear_pseudo_perf_series,spiralS_32_linear_pseudo_perf_series,spiral_pseudo_perf_series))





    val opsplot = new OpsPlot(lists._1)
    opsplot.outputName = "TestOps"
    opsplot.title = "Op Count"
    opsplot.xLabel = "size"
    opsplot.yLabel = "%Op Overhead to minimum"
    opsplot.xUnit = "# Complex Doubles"
    opsplot.yUnit = "% Overhead"


    val perfplot = new PerformancePlot(lists._2,
      List(
        ("Scalar",Performance(Flops(2),Cycles(1)))
      ))
    perfplot.outputName = "Performance"
    perfplot.title = "Performance Comparison"
    perfplot.xLabel = "size"
    perfplot.yLabel = "Performance"
    perfplot.xUnit = "# Complex Doubles"
    perfplot.yUnit = "flops/cycle"



    val pseudoperfplot = new PerformancePlot(lists._3,
      List(
        ("Scalar",Performance(Flops(2),Cycles(1)))
      ))
    pseudoperfplot.outputName = "Pseudo-Performance"
    pseudoperfplot.title = "Pseudo-Performance Comparison"
    pseudoperfplot.xLabel = "size"
    pseudoperfplot.yLabel = "Pseudo-Performance"
    pseudoperfplot.xUnit = "# Complex Doubles"
    pseudoperfplot.yUnit = "Pseudo-flops/cycle"
    

    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(opsplot)
    ps.plot(perfplot)
    ps.plot(pseudoperfplot)

    spiralS_32_linear_res.prettyprint()
  }

}
