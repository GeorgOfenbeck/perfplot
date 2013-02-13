import org.scalatest.Suite
import perfplot.Config
import java.io._
import java.io.PrintStream
import perfplot.plot.{PerformancePlot, OpsPlot}
import perfplot.quantities.{Cycles, Flops, Performance}
import perfplot.services.{PlotService, CommandService}


class TestMKL extends Suite {

  val repeats = 20

  def FIRCode (sourcefile: PrintStream, sizes: List[Long], fsize: Int) =
  {

    def p(x: String) = sourcefile.println(x)


    p(Config.MeasuringCoreH)
    p("#include <stdio.h>")

    p("int main () {\n")
    p("  perfmon_init(1,false,false,false);")
    for (s <- sizes)
    {
      p("{")
      p("  x1 = (double*) malloc(sizeof(double) * sizeN);")
      p("  x3 = (double*) malloc(sizeof(double) * sizeN);")
      p("  x2 = (double*) malloc(sizeof(double) * sizeH);")

      p("  for (long r =0; r < " + repeats + "; r++) {")
      p("    perfmon_start();");

      for (x <- 0 until 100) {
        p("  for (int x4 = 0; x4 < " + (s - fsize).toString + "; x4++) {")
        p("    x3[x4] = 0.0;")
        p("    int x5 = x4 + " + (fsize - 1).toString + ";")
        p("    for (int x7 = 0; x7 < " + fsize.toString() + "; x7++) {")
        p("      double x12 = x3[x4];")
        p("      double x9 = x2[x7];")
        p("      int x8 = x5 - x7;")
        p("      double x10 = x1[x8];")
        p("      double x11 = x9 * x10;")
        p("      double x13 = x12 + x11;")
        p("      x3[x4] = x13;")
        p("   }")
        p("}")
      }
      p("   perfmon_stop();")

      p("  }")
      p("  free(x1); free(x2); free(x3);")
      p("}")

    }
    p("    perfmon_end();")
    p("}")
  }


  def MKLCode (sourcefile: PrintStream, sizes: List[Long], fsize: Int) =
  {

    def p(x: String) = sourcefile.println(x)


    p(Config.MeasuringCoreH)
    p("#include <stdio.h>")
    //p("#include <stdlib.h>")
    p("#include \"/opt/intel/mkl/include/mkl.h\"")
    p("void fir (int sizeN, int sizeH) {")
    p("    VSLConvTaskPtr task; int i, r, start = 0; double *x, *h, *y;")
    p("    x = (double*) malloc(sizeof(double) * sizeN);")
    p("    y = (double*) malloc(sizeof(double) * sizeN);")
    p("    h = (double*) malloc(sizeof(double) * sizeH);")

    p("    for (long r =0; r < " + repeats + "; r++) {")
    p("      vsldConvNewTask1D(&task,VSL_CONV_MODE_DIRECT, sizeN, sizeH, sizeN);")
    p("      vslConvSetStart(task, &start);")
    p("        perfmon_start();");
    for (x <- 0 until 100) {
      p("      vsldConvExec1D(task, x, 1, h, 1, y, 1);")
    }
    p("        perfmon_stop();")
    p("      vslConvDeleteTask(&task);")
    p("     }")
    p("    free(x); free(y); free(h);")
    p("}\n")

    p("int main () {\n")
    p("perfmon_init(1,false,false,false);")
    for (s <- sizes)
    {
      p("    fir(" + s + ", " + fsize + ");")
    }
    p("    perfmon_end();")
    p("}")
  }

  def test () {

    val sizes = (for (i<-11 until 20) yield Math.pow(2,i).toLong).toList

    def mkl128 (sourcefile: PrintStream) = MKLCode(sourcefile,sizes, 128)
    val mkl128_res = CommandService.fromScratch("mkl128", mkl128, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -mkl ")
    val mkl128_ops_series = mkl128_res.toFlopSeries("mkl128",sizes)
    val mkl128_perf_series = mkl128_res.toPerformanceSeries("mkl128",sizes)
    val mkl128_pseudo_perf_series = mkl128_res.toPerformanceSeries_fft("mkl128", sizes)

    def mkl256 (sourcefile: PrintStream) = MKLCode(sourcefile,sizes, 256)
    val mkl256_res = CommandService.fromScratch("mkl256", mkl256, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -mkl ")
    val mkl256_ops_series = mkl256_res.toFlopSeries("mkl256",sizes)
    val mkl256_perf_series = mkl256_res.toPerformanceSeries("mkl256",sizes)
    val mkl256_pseudo_perf_series = mkl256_res.toPerformanceSeries_fft("mkl256", sizes)

    def mkl512 (sourcefile: PrintStream) = MKLCode(sourcefile,sizes, 512)
    val mkl512_res = CommandService.fromScratch("mkl512", mkl512, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -mkl ")
    val mkl512_ops_series = mkl512_res.toFlopSeries("mkl512",sizes)
    val mkl512_perf_series = mkl512_res.toPerformanceSeries("mkl512",sizes)
    val mkl512_pseudo_perf_series = mkl512_res.toPerformanceSeries_fft("mkl512", sizes)

    def mkl1024 (sourcefile: PrintStream) = MKLCode(sourcefile,sizes, 1024)
    val mkl1024_res = CommandService.fromScratch("mkl1024", mkl1024, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -mkl ")
    val mkl1024_ops_series = mkl1024_res.toFlopSeries("mkl1024",sizes)
    val mkl1024_perf_series = mkl1024_res.toPerformanceSeries("mkl1024",sizes)
    val mkl1024_pseudo_perf_series = mkl1024_res.toPerformanceSeries_fft("mkl1024", sizes)

    def mkl2048 (sourcefile: PrintStream) = MKLCode(sourcefile,sizes, 2048)
    val mkl2048_res = CommandService.fromScratch("mkl2048", mkl2048, Config.flag_c99 + Config.flag_optimization + Config.flag_hw + Config.flag_novec  + " -mkl ")
    val mkl2048_ops_series = mkl2048_res.toFlopSeries("mkl2048",sizes)
    val mkl2048_perf_series = mkl2048_res.toPerformanceSeries("mkl2048",sizes)
    val mkl2048_pseudo_perf_series = mkl2048_res.toPerformanceSeries_fft("mkl2048", sizes)


    val lists = (
      List(mkl128_ops_series, mkl512_ops_series, mkl512_ops_series, mkl1024_ops_series, mkl2048_ops_series),
      List(mkl128_perf_series, mkl256_perf_series, mkl512_perf_series, mkl1024_perf_series, mkl2048_perf_series),
      List(mkl128_pseudo_perf_series, mkl256_pseudo_perf_series, mkl512_pseudo_perf_series, mkl1024_pseudo_perf_series, mkl2048_pseudo_perf_series)
      );

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

  }

}

