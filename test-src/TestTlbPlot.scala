/*
import org.scalatest.Suite

import perfplot._
import perfplot.quantities._
import perfplot.services._
import perfplot.plot._
import java.io._
import scala.io._


class TestTlbPlot extends Suite {

    // all the TLB counters
    val DTLB_LOAD_MISSES_WALK_EVTNR = "0x08"
    val DTLB_LOAD_MISSES_WALK_UMASK = "0x81"

    val DTLB_ACCESS_STLB_HIT_EVTNR = "0x5F"
    val DTLB_ACCESS_STLB_HIT_UMASK = "0x01"

    val DTLB_STORE_MISSES_WALK_EVTNR = "0x49"
    val DTLB_STORE_MISSES_WALK_UMASK = "0x01"

    val DTLB_STORE_MISSES_STLB_HIT_EVTNR = "0x49"
    val DTLB_STORE_MISSES_STLB_HIT_UMASK = "0x10"

    // all the flops counters
    val FP_COMP_OPS_X87_EVTNR = "0x10"
    val FP_COMP_OPS_X87_UMASK = "0x01"

    val FP_COMP_OPS_PACKED_DOUBLE_EVTNR = "0x10"
    val FP_COMP_OPS_PACKED_DOUBLE_UMASK = "0x10"

    val FP_COMP_OPS_SCALAR_DOUBLE_EVTNR = "0x10"
    val FP_COMP_OPS_SCALAR_DOUBLE_UMASK = "0x80"

    val FP_COMP_OPS_PACKED_SINGLE_EVTNR = "0x10"
    val FP_COMP_OPS_PACKED_SINGLE_UMASK = "0x40"

    val FP_COMP_OPS_SCALAR_SINGLE_EVTNR = "0x10"
    val FP_COMP_OPS_SCALAR_SINGLE_UMASK = "0x20"

    val SIMD_FP_256_PACKED_DOUBLE_EVTNR = "0x11"
    val SIMD_FP_256_PACKED_DOUBLE_UMASK = "0x01"

    val SIMD_FP_256_PACKED_SINGLE_EVTNR = "0x11"
    val SIMD_FP_256_PACKED_SINGLE__UMASK = "0x02"

    // memory traffic counters
    val OFFCORE_RESPONSE_ALL_DATA_LLC_MISS = "0x300400091"
    val OFFCORE_RESPONSE_ALL_RFO_LLC_MISS = "0x300400122"

    // time
    val UNHALTED_CORE_CYCLES_EVTNR = "0x3C"
    val UNHALTED_CORE_CYCLES_UMASK = "0x00"


    val plotService : PlotService = new PlotService
    val repeats = 10

    def TriadCode (sourcefile: PrintStream, counters : Array[String]) =
    {
        sourcefile.println("#include <iostream>")
        sourcefile.println("#include <cstdlib>")
        sourcefile.println("#include <cstddef>")

        sourcefile.println(Config.MeasuringCoreH)

        sourcefile.println("double dummy;")

        sourcefile.println("int main () {\n    ")

        sourcefile.println("long counters[8];")
        sourcefile.println("counters[0] = " + counters(0) + ";")
        sourcefile.println("counters[1] = " + counters(1) + ";")
        sourcefile.println("counters[2] = " + counters(2) + ";")
        sourcefile.println("counters[3] = " + counters(3) + ";")
        sourcefile.println("counters[4] = " + counters(4) + ";")
        sourcefile.println("counters[5] = " + counters(5) + ";")
        sourcefile.println("counters[6] = " + counters(6) + ";")
        sourcefile.println("counters[7] = " + counters(7) + ";")

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

        sourcefile.println("measurement_init(counters, 0, 0);")

        sourcefile.println("measurement_start();");

        sourcefile.println("for(int rep = 0; rep < " + repeats + "; ++rep)")
        sourcefile.println("for(int i = 0; i < size; ++i)")
        sourcefile.println("  a[i] = b[i] + 2.71 * c[i];")

        sourcefile.println("measurement_stop(1);")

        sourcefile.println("measurement_end();")

        sourcefile.println("dummy = a[2];" )
        sourcefile.println("std::cout << a[3] << std::endl; " )


        sourcefile.println("delete[] a;")
        sourcefile.println("delete[] b;")
        sourcefile.println("delete[] c;")

        sourcefile.println("return 0;" )
        sourcefile.println("}")
    }

    def test01 {

        val tlbEvents : Array[String] = Array(
            DTLB_LOAD_MISSES_WALK_EVTNR, DTLB_LOAD_MISSES_WALK_UMASK,
            DTLB_ACCESS_STLB_HIT_EVTNR, DTLB_ACCESS_STLB_HIT_UMASK,
            DTLB_STORE_MISSES_WALK_EVTNR, DTLB_STORE_MISSES_STLB_HIT_UMASK,
            DTLB_STORE_MISSES_STLB_HIT_EVTNR, DTLB_STORE_MISSES_STLB_HIT_UMASK)

        val fpEvents : Array[String] = Array(
            FP_COMP_OPS_X87_EVTNR, FP_COMP_OPS_X87_UMASK,
            FP_COMP_OPS_PACKED_DOUBLE_EVTNR, FP_COMP_OPS_PACKED_DOUBLE_UMASK,
            FP_COMP_OPS_SCALAR_DOUBLE_EVTNR, FP_COMP_OPS_SCALAR_DOUBLE_UMASK,
            SIMD_FP_256_PACKED_DOUBLE_EVTNR, SIMD_FP_256_PACKED_DOUBLE_UMASK)

        def tlbMeasure(file: PrintStream) = TriadCode(file, tlbEvents)

        def fpMeasure(file: PrintStream) = TriadCode(file, fpEvents)

        val tlbResult = CommandService.fromScratch("test", tlbMeasure,  Config.flag_c99 + Config.flag_optimization + Config.flag_hw  + " -mfpmath=sse")

        val totalTlb : Double = tlbResult.getSCounter0()(0) + tlbResult.getSCounter1()(0) + tlbResult.getSCounter2()(0) + tlbResult.getSCounter3()(0)

        val fpResult = CommandService.fromScratch("test", fpMeasure,  Config.flag_c99 + Config.flag_optimization + Config.flag_hw  + " -mfpmath=sse")

        val totalFp : Double = fpResult.getSCounter0()(0) + fpResult.getSCounter1()(0)*2 + fpResult.getSCounter2()(0) + fpResult.getSCounter3()(0) * 4

        val time : Double = tlbResult.getTSC()(0)

        //System.out.format("PageIntensity = %f \n Performance = %f \n", totalFp/totalTlb, totalFp/time)
        System.out.println("Opcount = " + totalFp.toString)
        System.out.println("Time = " + time.toString)
        System.out.println("TLB Misses = " + totalTlb.toString)
        System.out.println("PageIntensity = " + (totalFp/totalTlb).toString)
        System.out.println("Performance = " + (totalFp/time).toString)

        var perf : List[(String, Performance)] = List(
            ("Balanced Scalar", Performance(Flops(2.0), Cycles(1.0))),
            ("Balanced SSE", Performance(Flops(4.0), Cycles(1.0))),
            ("Balanced AVX", Performance(Flops(8.0), Cycles(1.0))),
            ("AVX * 4", Performance(Flops(32.0), Cycles(1.0))))
        var band : List[(String, Throughput)] = List(
            ("TLB L1", Throughput(TransferredBytes(1.0), Cycles(1.0))),
            ("TLB L2", Throughput(TransferredBytes(1.0), Cycles(15.0))))


        var pts : List[RooflinePoint] = List(RooflinePoint(1000000,
            List[(Performance, OperationalIntensity)] (
                (Performance(Flops(totalFp), Cycles(time)), OperationalIntensity(Flops(totalFp), TransferredBytes(totalTlb)))
                 )
            )
        )

        var series : List[RooflineSeries] = List(RooflineSeries("triad", pts))

        var plot : RooflinePlot = RooflinePlot(perf, band, series)
        plot.xLabel = "Triad TLB"

        plotService.plot(plot)
    }
}*/
