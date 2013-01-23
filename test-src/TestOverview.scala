/*
/**
 * Georg Ofenbeck
 First created:
 * Date: 15/01/13
 * Time: 10:12 
 */

import org.scalatest.Suite

import roofline.plot._
import roofline.quantities._
import roofline.services._

import services._
import java.io._
import scala.io._


class TestOverview extends Suite{



  def test_overview () =
  {
    val myplot = new RooflinePlot(
      List(
        ("6 * AVX",Performance(Flops(48),Cycles(1))),
        ("AVX",Performance(Flops(8),Cycles(1))),
        ("Scalar",Performance(Flops(2),Cycles(1)))
      ),
      getPeakBandwith(), List(daxpy(),dgemv(),mmm(),fft())

    )
    myplot.outputName = "TestRooflinePlot"
    myplot.title = "Title"
    myplot.xLabel = "xLabel"
    myplot.yLabel = "yLabel"

    myplot.xUnit = "xUnit"
    myplot.yUnit = "yUnit"


    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(myplot)
  }



  def fft ()  : RooflineSeries =
  {
    val start = 512
    val repeats = 11
    val maxsize = math.pow(2,25)+1
    val increase = 100 //ignored - doubling

    val filename = "fft"
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")

    cmdbat.println("  \n\n#include \"mkl_dfti.h\" \n#include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\n")
    cmdbat.println("\nvoid flushCache()\n{\n const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n double* buffer = (double*) _mm_malloc( size, page );\n if (!buffer) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return ;\n    }\n double result = 0; \n for (long i = 0; i < size/sizeof(double); i= i+4)\n  buffer[i] = 1.2;\n for (long i = 0; i < size/sizeof(double); i= i+4)\n   result = buffer[i] + result;   \n std::cout << \"res\" << result << \"\\n\";\n _mm_free(buffer);     \n    \n}")
    cmdbat.println("\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\n\n\ndouble * a, *b, *c;\ndouble alpha;\n\n\n\nint main () {\n\n  ");
    cmdbat.println("perfmon_init(1,false,false,false);\n\tdouble  *complexData;\n\tDFTI_DESCRIPTOR_HANDLE mklDescriptor;\n  const int page = 1024*4;\n  int mem = 256*512; //128 MB\n  \n  \n  \n")
    //cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
    cmdbat.println("for (int vectorSize = "+ start + "; vectorSize <= "+ maxsize + "; vectorSize = vectorSize *2) ")
    //cmdbat.println("long vectorSize = 500;")
    cmdbat.println("\n  {  MKL_LONG status;\n\n\tstatus = DftiCreateDescriptor( &mklDescriptor, DFTI_DOUBLE,\n\t\t\tDFTI_REAL, 1, vectorSize);\n\tif (status != 0) {\n    return -1;\n\t}\n\n\tstatus = DftiCommitDescriptor(mklDescriptor);\n\tif (status != 0) {\n\t\treturn -1;\n\t}")
    cmdbat.println("double alpha = 1.1; double beta = 1.2;  \n    complexData = (double *)  _mm_malloc( 2*vectorSize*sizeof(double), page );\n    if (!complexData) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n    ");
    cmdbat.println("for (long j = 0; j < "+ repeats + "; j++)\n  {\n")
    //cmdbat.println("double result = 0;perfmon_start();    \n\n    for(long i = 0; i< vectorSize; i=i+1)    \n      result += x[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;")
    cmdbat.println("perfmon_start();\n     \n\t")
    cmdbat.println("status = DftiComputeForward(mklDescriptor, complexData);\n\tif (status != 0) {\n\t\treturn -1;\n\t}  \n     perfmon_stop();\n\n ")
    cmdbat.println(" }\n   _mm_free(complexData);       \n  }\n  perfmon_end();\n  \n  \n}")
    cmdbat.close()

    CompileService.compile(temp.getPath +File.separator + filename)
    System.out.println("working here:")
    System.out.println(temp.getPath + File.separator + filename)

    if (roofline.Config.isWin)
    {
      val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
      val bat = new PrintStream(temp.getPath + File.separator +  filename + ".bat")
      bat.println(batch_source)
      bat.println(filename + ".exe")
      bat.close()
      CompileService.execute(temp.getPath + File.separator + filename + ".bat")
    }
    else
    {
      CompileService.execute(temp.getPath + File.separator + filename + ".x", temp)
    }
    val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt


    val Counter0 = new Array[Array[Long]](nrcores)
    val Counter1 = new Array[Array[Long]](nrcores)
    val Counter2 = new Array[Array[Long]](nrcores)
    val Counter3 = new Array[Array[Long]](nrcores)
    val CycleCounter = new Array[Array[Long]](nrcores)
    val RefCycleCounter = new Array[Array[Long]](nrcores)
    val TSCCounter = new Array[Array[Long]](nrcores)





    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
    }


    val sCounter0 = new Array[Long](Counter0(0).size)
    val sCounter1 = new Array[Long](Counter1(0).size)
    val sCounter2 = new Array[Long](Counter2(0).size)
    val sCounter3 = new Array[Long](Counter3(0).size)

    for (i <- 0 until sCounter0.size) {
      sCounter0(i) = 0
      for (j <- 0 until nrcores)
        sCounter0(i) = sCounter0(i) + Counter0(j)(i)
    }
    for (i <- 0 until sCounter1.size){
      sCounter1(i) = 0
      for (j <- 0 until nrcores)
        sCounter1(i) = sCounter1(i) + Counter1(j)(i)
    }
    for (i <- 0 until sCounter2.size){
      sCounter2(i) = 0
      for (j <- 0 until nrcores)
        sCounter2(i) = sCounter2(i) + Counter2(j)(i)
    }
    for (i <- 0 until sCounter3.size){
      sCounter3(i) = 0
      for (j <- 0 until nrcores)
        sCounter3(i) = sCounter3(i) + Counter3(j)(i)
    }




    val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong  ).reverse // /1024/1024 )
  val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)


    println (sCounter0.mkString(" "))
    println (sCounter1.mkString(" "))
    println (sCounter2.mkString(" "))
    println (sCounter3.mkString(" "))
    println("-------------")
    println("read")
    println (mcread.mkString(" "))
    println("write")
    println (mcwrite.mkString(" "))

    var vectorSize = start
    var i = 0


    while (vectorSize <= maxsize)
    {
      i = i + 1
      vectorSize = vectorSize *2
    }
    val tseries = new Array[RooflinePoint](i)
    vectorSize = start
    i =0


    while (vectorSize <=  maxsize)
    {
      val tarr = new Array[(Performance,OperationalIntensity)](repeats-1) //-1 cause of warm up
      for (j <- 1 until repeats)
      {
        val cycles = TSCCounter(0)(i*repeats + j)
        val flops = sCounter0(i*repeats + j) + sCounter1(i*repeats + j) * 2 + sCounter2(i*repeats + j) * 4
        val performance = Performance(Flops(flops),Cycles(cycles))
        val bytes = (mcread(i*repeats + j) + mcwrite(i*repeats + j))
        //val intensity = flops.toDouble / bytes
        val intensity = OperationalIntensity(Flops(flops),TransferredBytes(bytes))
        println("----")
        println(performance.value + " .... " + bytes + " .... " + flops + " .... " + intensity.value )
        tarr(j-1) = (performance,intensity)
      }

      val tpoint = RooflinePoint(vectorSize,tarr.toList)

      tseries(i) = tpoint

      vectorSize = vectorSize *2
      i = i+1
    }


    tseries.toList

    val mes_ser = RooflineSeries("fft",tseries.toList)

    mes_ser
  }




  def dgemv ()  : RooflineSeries =
  {
    val start = 100
    val repeats = 11
    val maxsize = 4050
    val increase = 100

    val filename = "dgemv"
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")

    cmdbat.println(" \n#include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\n")
    cmdbat.println("\nvoid flushCache()\n{\n const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n double* buffer = (double*) _mm_malloc( size, page );\n if (!buffer) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return ;\n    }\n double result = 0; \n for (long i = 0; i < size/sizeof(double); i= i+4)\n  buffer[i] = 1.2;\n for (long i = 0; i < size/sizeof(double); i= i+4)\n   result = buffer[i] + result;   \n std::cout << \"res\" << result << \"\\n\";\n _mm_free(buffer);     \n    \n}")
    cmdbat.println("\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\n\n\ndouble * a, *b, *c;\ndouble alpha;\n\n\n\nint main () {\n\n  ");
    cmdbat.println("perfmon_init(1,false,false,false);\n\n  const int page = 1024*4;\n  int mem = 256*512; //128 MB\n  \n  \n  \n")
    //cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
    cmdbat.println("for (int vectorSize = "+ start + "; vectorSize <= "+ maxsize + "; vectorSize = vectorSize + " + increase + ")")
    //cmdbat.println("long vectorSize = 500;")
    cmdbat.println("\n  {double alpha = 1.1; double beta = 1.2;  \n    a = (double*)  _mm_malloc( vectorSize*vectorSize*sizeof(double), page );\n    if (!a) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n    b = (double*)  _mm_malloc( vectorSize*sizeof(double), page );\n    if (!b) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n  c = (double*)  _mm_malloc( vectorSize*vectorSize*sizeof(double), page );\n    if (!c) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n // initialize factors\n\talpha=1.1;\n\n\n\n  ");
    cmdbat.println("for (long j = 0; j < "+ repeats + "; j++)\n  {\n")
    //cmdbat.println("double result = 0;perfmon_start();    \n\n    for(long i = 0; i< vectorSize; i=i+1)    \n      result += x[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;")
    cmdbat.println("       \t\tstd::cout << j << \" j \\n\";\n for (long i=0; i<vectorSize*vectorSize; i+=1){\n        a[i]=1.2;\n        \n } for (long i=0; i<vectorSize; i+=1){\n        b[i]=1.2;\n        \n }    for (long i=0; i<vectorSize*vectorSize; i+=4){\n            flushCacheLine(&(a[i])); \n        \n }  for (long i=0; i<vectorSize; i+=4){\n            flushCacheLine(&(b[i])); \n        \n } //flushCache();\n      ;  \nperfmon_start();\n     \tint size = vectorSize;\n\t")
    cmdbat.println("cblas_dgemv(CblasRowMajor, CblasNoTrans, size, size, alpha, a, size, b, 1,\n\t\t\tbeta, c, 1);      \n     perfmon_stop();\n\n ")
    cmdbat.println(" }\n   _mm_free(a);\n   _mm_free(b);_mm_free(c);          \n  }\n  perfmon_end();\n  \n  \n}")
    cmdbat.close()

    CompileService.compile(temp.getPath +File.separator + filename)
    System.out.println("working here:")
    System.out.println(temp.getPath + File.separator + filename)

    if (roofline.Config.isWin)
    {
      val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
      val bat = new PrintStream(temp.getPath + File.separator +  filename + ".bat")
      bat.println(batch_source)
      bat.println(filename + ".exe")
      bat.close()
      CompileService.execute(temp.getPath + File.separator + filename + ".bat")
    }
    else
    {
      CompileService.execute(temp.getPath + File.separator + filename + ".x", temp)
    }
    val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt


    val Counter0 = new Array[Array[Long]](nrcores)
    val Counter1 = new Array[Array[Long]](nrcores)
    val Counter2 = new Array[Array[Long]](nrcores)
    val Counter3 = new Array[Array[Long]](nrcores)
    val CycleCounter = new Array[Array[Long]](nrcores)
    val RefCycleCounter = new Array[Array[Long]](nrcores)
    val TSCCounter = new Array[Array[Long]](nrcores)





    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
    }


    val sCounter0 = new Array[Long](Counter0(0).size)
    val sCounter1 = new Array[Long](Counter1(0).size)
    val sCounter2 = new Array[Long](Counter2(0).size)
    val sCounter3 = new Array[Long](Counter3(0).size)

    for (i <- 0 until sCounter0.size) {
      sCounter0(i) = 0
      for (j <- 0 until nrcores)
        sCounter0(i) = sCounter0(i) + Counter0(j)(i)
    }
    for (i <- 0 until sCounter1.size){
      sCounter1(i) = 0
      for (j <- 0 until nrcores)
        sCounter1(i) = sCounter1(i) + Counter1(j)(i)
    }
    for (i <- 0 until sCounter2.size){
      sCounter2(i) = 0
      for (j <- 0 until nrcores)
        sCounter2(i) = sCounter2(i) + Counter2(j)(i)
    }
    for (i <- 0 until sCounter3.size){
      sCounter3(i) = 0
      for (j <- 0 until nrcores)
        sCounter3(i) = sCounter3(i) + Counter3(j)(i)
    }




    val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong  ).reverse // /1024/1024 )
  val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)


    println (sCounter0.mkString(" "))
    println (sCounter1.mkString(" "))
    println (sCounter2.mkString(" "))
    println (sCounter3.mkString(" "))
    println("-------------")
    println("read")
    println (mcread.mkString(" "))
    println("write")
    println (mcwrite.mkString(" "))

    var vectorSize = start
    var i = 0


    while (vectorSize <= maxsize)
    {
      i = i + 1
      vectorSize = vectorSize + increase
    }
    val tseries = new Array[RooflinePoint](i)
    vectorSize = start
    i =0


    while (vectorSize <=  maxsize)
    {
      val tarr = new Array[(Performance,OperationalIntensity)](repeats-1) //-1 cause of warm up
      for (j <- 1 until repeats)
      {
        val cycles = TSCCounter(0)(i*repeats + j)
        val flops = sCounter0(i*repeats + j) + sCounter1(i*repeats + j) * 2 + sCounter2(i*repeats + j) * 4
        val performance = Performance(Flops(flops),Cycles(cycles))
        val bytes = (mcread(i*repeats + j) + mcwrite(i*repeats + j))
        //val intensity = flops.toDouble / bytes
        val intensity = OperationalIntensity(Flops(flops),TransferredBytes(bytes))
        println("----")
        println(performance.value + " .... " + bytes + " .... " + flops + " .... " + intensity.value )
        tarr(j-1) = (performance,intensity)
      }

      val tpoint = RooflinePoint(vectorSize,tarr.toList)

      tseries(i) = tpoint

      vectorSize = vectorSize + increase
      i = i+1
    }


    tseries.toList

    val mes_ser = RooflineSeries("dgemv",tseries.toList)

    mes_ser
  }
  
  
  
  
  

  def mmm ()  : RooflineSeries =
    {
      val start = 100
      val repeats = 11
      val maxsize = 4050
      val increase = 100

      val filename = "mmm"
      val temp : File = File.createTempFile(filename,"");
      temp.delete()
      temp.mkdir()
      val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")

      cmdbat.println(" \n#include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\n")
      cmdbat.println("\nvoid flushCache()\n{\n const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n double* buffer = (double*) _mm_malloc( size, page );\n if (!buffer) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return ;\n    }\n double result = 0; \n for (long i = 0; i < size/sizeof(double); i= i+4)\n  buffer[i] = 1.2;\n for (long i = 0; i < size/sizeof(double); i= i+4)\n   result = buffer[i] + result;   \n std::cout << \"res\" << result << \"\\n\";\n _mm_free(buffer);     \n    \n}")
      cmdbat.println("\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\n\n\ndouble * a, *b, *c;\ndouble alpha;\n\n\n\nint main () {\n\n  ");
      cmdbat.println("perfmon_init(1,false,false,false);\n\n  const int page = 1024*4;\n  int mem = 256*512; //128 MB\n  \n  \n  \n")
      //cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
      cmdbat.println("for (int vectorSize = "+ start + "; vectorSize <= "+ maxsize + "; vectorSize = vectorSize + " + increase + ")")
      //cmdbat.println("long vectorSize = 500;")
      cmdbat.println("\n  {int check = 65; int size = vectorSize; std::cout  << size << \" vec \\n\" ;std::cout  << check << \" vecfa \\n\" ;\n   \n    a = (double*)  _mm_malloc( vectorSize*vectorSize*sizeof(double), page );\n    if (!a) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n    b = (double*)  _mm_malloc( vectorSize*vectorSize*sizeof(double), page );\n    if (!b) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n  c = (double*)  _mm_malloc( vectorSize*vectorSize*sizeof(double), page );\n    if (!c) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n // initialize factors\n\talpha=1.1;\n\n\n\n  ");
      cmdbat.println("for (long j = 0; j < "+ repeats + "; j++)\n  {\n")
      //cmdbat.println("double result = 0;perfmon_start();    \n\n    for(long i = 0; i< vectorSize; i=i+1)    \n      result += x[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;")
      cmdbat.println("       \t\tstd::cout << j << \" j \\n\";\n for (long i=0; i<vectorSize*vectorSize; i+=4){\n        a[i]=1.2;\n        b[i]=1.3;\n         flushCacheLine(&(a[i]));  flushCacheLine(&(b[i])); \n      } //flushCache();\n      ;  \nperfmon_start();\n     \tint size = vectorSize;\n\tcblas_dgemm(\n\t\t\tCblasRowMajor,\n\t\t\tCblasNoTrans, CblasNoTrans,\n\t\t\tsize, size, size,\n\t\t\t1,\n\t\t\ta, size,\n\t\t\tb, size,\n\t\t\t1,\n\t\t\tc, size);       \n     perfmon_stop();\n\n ")
      cmdbat.println(" }\n   _mm_free(a);\n   _mm_free(b);_mm_free(c);          \n  }\n  perfmon_end();\n  \n  \n}")
      cmdbat.close()

      CompileService.compile(temp.getPath +File.separator + filename)
      System.out.println("working here:")
      System.out.println(temp.getPath + File.separator + filename)

      if (roofline.Config.isWin)
      {
        val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
        val bat = new PrintStream(temp.getPath + File.separator +  filename + ".bat")
        bat.println(batch_source)
        bat.println(filename + ".exe")
        bat.close()
        CompileService.execute(temp.getPath + File.separator + filename + ".bat")
      }
      else
      {
        CompileService.execute(temp.getPath + File.separator + filename + ".x", temp)
      }
      val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt


      val Counter0 = new Array[Array[Long]](nrcores)
      val Counter1 = new Array[Array[Long]](nrcores)
      val Counter2 = new Array[Array[Long]](nrcores)
      val Counter3 = new Array[Array[Long]](nrcores)
      val CycleCounter = new Array[Array[Long]](nrcores)
      val RefCycleCounter = new Array[Array[Long]](nrcores)
      val TSCCounter = new Array[Array[Long]](nrcores)





      for (i <- 0 until nrcores)
      {
        Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
        RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
        TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      }


      val sCounter0 = new Array[Long](Counter0(0).size)
      val sCounter1 = new Array[Long](Counter1(0).size)
      val sCounter2 = new Array[Long](Counter2(0).size)
      val sCounter3 = new Array[Long](Counter3(0).size)

      for (i <- 0 until sCounter0.size) {
        sCounter0(i) = 0
        for (j <- 0 until nrcores)
          sCounter0(i) = sCounter0(i) + Counter0(j)(i)
      }
      for (i <- 0 until sCounter1.size){
        sCounter1(i) = 0
        for (j <- 0 until nrcores)
          sCounter1(i) = sCounter1(i) + Counter1(j)(i)
      }
      for (i <- 0 until sCounter2.size){
        sCounter2(i) = 0
        for (j <- 0 until nrcores)
          sCounter2(i) = sCounter2(i) + Counter2(j)(i)
      }
      for (i <- 0 until sCounter3.size){
        sCounter3(i) = 0
        for (j <- 0 until nrcores)
          sCounter3(i) = sCounter3(i) + Counter3(j)(i)
      }




      val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong  ).reverse // /1024/1024 )
    val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)


      println (sCounter0.mkString(" "))
      println (sCounter1.mkString(" "))
      println (sCounter2.mkString(" "))
      println (sCounter3.mkString(" "))
      println("-------------")
      println("read")
      println (mcread.mkString(" "))
      println("write")
      println (mcwrite.mkString(" "))

      var vectorSize = start
      var i = 0


      while (vectorSize <= maxsize)
      {
        i = i + 1
        vectorSize = vectorSize + increase
      }
      val tseries = new Array[RooflinePoint](i)
      vectorSize = start
      i =0


      while (vectorSize <=  maxsize)
      {
        val tarr = new Array[(Performance,OperationalIntensity)](repeats-1) //-1 cause of warm up
        for (j <- 1 until repeats)
        {
          val cycles = TSCCounter(0)(i*repeats + j)
          val flops = sCounter0(i*repeats + j) + sCounter1(i*repeats + j) * 2 + sCounter2(i*repeats + j) * 4
          val performance = Performance(Flops(flops),Cycles(cycles))
          val bytes = (mcread(i*repeats + j) + mcwrite(i*repeats + j))
          //val intensity = flops.toDouble / bytes
          val intensity = OperationalIntensity(Flops(flops),TransferredBytes(bytes))
          println("----")
          println(performance.value + " .... " + bytes + " .... " + flops + " .... " + intensity.value )
          tarr(j-1) = (performance,intensity)
        }

        val tpoint = RooflinePoint(vectorSize,tarr.toList)

        tseries(i) = tpoint

        vectorSize = vectorSize + increase
        i = i+1
      }


      tseries.toList

      val mes_ser = RooflineSeries("mmm",tseries.toList)

      mes_ser
    }


      def daxpy() : RooflineSeries =
  {
    val repeats = 2
    val maxsize =   100 * 1000 * 1000
    val startsize = 5000

    val filename = "daxpy"
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")

    cmdbat.println(" \n#include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\n")
    cmdbat.println("\nvoid flushCache()\n{\n const int page = 1024*4;\n long size = 28 * 1024 * 1024;\n double* buffer = (double*) _mm_malloc( size, page );\n if (!buffer) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return ;\n    }\n double result = 0; \n for (long i = 0; i < size/sizeof(double); i= i+4)\n  buffer[i] = 1.2;\n for (long i = 0; i < size/sizeof(double); i= i+4)\n   result = buffer[i] + result;   \n std::cout << \"res\" << result << \"\\n\";\n _mm_free(buffer);     \n    \n}")
    cmdbat.println("\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\n\n\ndouble * x, *y;\ndouble alpha;\n\n\n\nint main () {\n\n  ");
    cmdbat.println("perfmon_init(1,false,false,false);\n\n  const int page = 1024*4;\n  int mem = 256*512; //128 MB\n  \n  \n  \n")
    //cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
    cmdbat.println("for (long vectorSize = " + startsize + " ; vectorSize <= "+ maxsize + "; vectorSize *= 2)")
    //cmdbat.println("long vectorSize = 500;")
    cmdbat.println("\n  {   \n    x = (double*)  _mm_malloc( vectorSize*sizeof(double), page );\n    if (!x) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n    y = (double*)  _mm_malloc( vectorSize*sizeof(double), page );\n    if (!x) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n  // initialize factors\n\talpha=1.1;\n\n\n\n  ");
    cmdbat.println("for (long j = 0; j < "+ repeats + "; j++)\n  {\n")
    //cmdbat.println("double result = 0;perfmon_start();    \n\n    for(long i = 0; i< vectorSize; i=i+1)    \n      result += x[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;")
    cmdbat.println("       \tfor (long i=0; i<vectorSize; i++){\n        x[i]=1.2;\n        y[i]=1.3;}\n for (long i=0; i<vectorSize; i=i+4) {        flushCacheLine(&(x[i]));  flushCacheLine(&(y[i]));\n      } //flushCache();\n Sleep(1);      ;perfmon_start();\n     cblas_daxpy( vectorSize, alpha, x, 1, y, 1);          \n     perfmon_stop();\n\n ")
    cmdbat.println(" }\n   _mm_free(x);\n   _mm_free(y);          \n  }\n  perfmon_end();\n  \n  \n}")
    cmdbat.close()

    CompileService.compile(temp.getPath +File.separator + filename)
    System.out.println("working here:")
    System.out.println(temp.getPath + File.separator + filename)

    if (roofline.Config.isWin)
    {
      val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
      val bat = new PrintStream(temp.getPath + File.separator +  filename + ".bat")
      bat.println(batch_source)
      bat.println(filename + ".exe")
      bat.close()
      CompileService.execute(temp.getPath + File.separator + filename + ".bat")
    }
    else
    {
      CompileService.execute(temp.getPath + File.separator + filename + ".x", temp)
    }
    val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt


    val Counter0 = new Array[Array[Long]](nrcores)
    val Counter1 = new Array[Array[Long]](nrcores)
    val Counter2 = new Array[Array[Long]](nrcores)
    val Counter3 = new Array[Array[Long]](nrcores)
    val CycleCounter = new Array[Array[Long]](nrcores)
    val RefCycleCounter = new Array[Array[Long]](nrcores)
    val TSCCounter = new Array[Array[Long]](nrcores)





    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
    }


    val sCounter0 = new Array[Long](Counter0(0).size)
    val sCounter1 = new Array[Long](Counter1(0).size)
    val sCounter2 = new Array[Long](Counter2(0).size)
    val sCounter3 = new Array[Long](Counter3(0).size)

    for (i <- 0 until sCounter0.size) {
      sCounter0(i) = 0
      for (j <- 0 until nrcores)
        sCounter0(i) = sCounter0(i) + Counter0(j)(i)
    }
    for (i <- 0 until sCounter1.size){
      sCounter1(i) = 0
      for (j <- 0 until nrcores)
        sCounter1(i) = sCounter1(i) + Counter1(j)(i)
    }
    for (i <- 0 until sCounter2.size){
      sCounter2(i) = 0
      for (j <- 0 until nrcores)
        sCounter2(i) = sCounter2(i) + Counter2(j)(i)
    }
    for (i <- 0 until sCounter3.size){
      sCounter3(i) = 0
      for (j <- 0 until nrcores)
        sCounter3(i) = sCounter3(i) + Counter3(j)(i)
    }




    val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong  ).reverse // /1024/1024 )
    val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)


    println (sCounter0.mkString(" "))
    println (sCounter1.mkString(" "))
    println (sCounter2.mkString(" "))
    println (sCounter3.mkString(" "))
    println("-------------")
    println("read")
    println (mcread.mkString(" "))
    println("write")
    println (mcwrite.mkString(" "))

    var vectorSize = startsize
    var i = 0


    while (vectorSize <= maxsize)
    {
      i = i + 1
      vectorSize = vectorSize * 2
    }
    val tseries = new Array[RooflinePoint](i)
    vectorSize = startsize
    i =0


    while (vectorSize <=  maxsize)
    {
      val tarr = new Array[(Performance,OperationalIntensity)](repeats-1) //-1 cause of warm up
      for (j <- 1 until repeats)
      {
        val cycles = TSCCounter(0)(i*repeats + j)
        val flops = sCounter0(i*repeats + j) + sCounter1(i*repeats + j) * 2 + sCounter2(i*repeats + j) * 4
        val performance = Performance(Flops(flops),Cycles(cycles))
        val bytes = (mcread(i*repeats + j) + mcwrite(i*repeats + j))
        //val intensity = flops.toDouble / bytes
        val intensity = OperationalIntensity(Flops(flops),TransferredBytes(bytes))
        println("----")
        println(performance.value + " .... " + bytes + " .... " + flops + " .... " + intensity.value )
        tarr(j-1) = (performance,intensity)
      }

      val tpoint = RooflinePoint(vectorSize,tarr.toList)

      tseries(i) = tpoint

      vectorSize = vectorSize * 2
      i = i+1
   }


    tseries.toList

    val mes_ser = RooflineSeries("daxpy",tseries.toList)

    mes_ser



  }




  def getFile(name: String ) : String =
  {
    val filecheck = new File(name)
    System.out.println("waiting for: " + filecheck.getAbsolutePath)
    while (!filecheck.exists())
    {
      //System.out.print(".")
    }
    val source = scala.io.Source.fromFile(name)
    val lines = source.mkString.trim
    source.close ()
    lines
  }


  def getPeakBandwith(): List[(String,Throughput)] =
  {
    val filename = "getBandwidth"
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")
    cmdbat.println("#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\nint main () {\nstd::cout << \"malloc test\"; \nperfmon_init(3,false,false,false);\n \nstd::cout << \"malloc test2\"; \n\n    double * buffer;\n    \n    const int page = 1024*4;\n    const int mem = 256*512; //128 MB\n buffer = (double*)  _mm_malloc( mem*page, page );\n  if (!buffer) {\n    std::cout << \"malloc failed\";\n\nperfmon_end();\nreturn -1;\n  }\n   std::cout << \"malloc worked\"; \n\nfor(long i = 0; i< mem*page/sizeof(double); i++)    \n      buffer[i] = i%2;    \n \nfor(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));\nperfmon_start(); perfmon_stop();\n\nperfmon_start();\n    for(long i = 0; i< mem*page/sizeof(double); i=i+1)    \n      buffer[i] = 1.0;\n\nperfmon_stop();\n\t\t\t\nperfmon_start();\nfor(long i = 0; i< mem*page/sizeof(double); i= i+1)\n      flushCacheLine(&(buffer[i]));\nperfmon_stop();\n\t\t\t\ndouble result = 0;perfmon_start();    \n\n    for(long i = 0; i< mem*page/sizeof(double); i=i+1)    \n      result += buffer[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;\nstd::cout << \"\\n\" << (mem/sizeof(double)) << \"\\n\";\nfor(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));\nlong size = mem*page/sizeof(double);\nperfmon_start();    \n\n    for(long i = 0; i< size; i=i+1)    \n      buffer[(size/2 + i)%(size)] = buffer[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;\nfor(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));\nperfmon_start();    \n\n    for(long i = 0; i< size; i=i+1)    \n      buffer[i] = buffer[i]-1; perfmon_stop();\n\n\t\t\tstd::cout << result;\nfor(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));\ndouble vec_res[4];\n __m256d a,b ; b= _mm256_set1_pd( 1.1 ); perfmon_start();    \n\n    for(long i = 0; i< mem*page/sizeof(double); i=i+4)    \n      {a = _mm256_load_pd (&(buffer[i])); /*b = _mm256_add_pd ( a, b);*/} perfmon_stop(); _mm256_storeu_pd (vec_res,a); \n\n\t\t\tstd::cout << vec_res[0];\n __m256d avx_res = _mm256_set1_pd( 1.1 ); perfmon_start();    \n\n    for(long i = 0; i< mem*page/sizeof(double); i=i+4)    \n      _mm256_stream_pd(&(buffer[i]),avx_res); perfmon_stop();\n\n\t\t\tstd::cout << result;\nperfmon_start(); perfmon_stop();\nperfmon_start(); perfmon_stop();\nperfmon_end();\n_mm_free (buffer);\n    return 0;\n  }")
    cmdbat.close()
    CompileService.compile(temp.getPath +File.separator + filename)
    System.out.println("working here:")
    System.out.println(temp.getPath + File.separator + filename)
    if (roofline.Config.isWin)
    {
      val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
      val bat = new PrintStream(temp.getPath + File.separator +  filename + ".bat")
      bat.println(batch_source)
      bat.println(filename + ".exe")
      bat.close()
      CompileService.execute(temp.getPath + File.separator + filename + ".bat")
    }
    else
    {
      CompileService.execute(temp.getPath + File.separator + filename + ".x", temp)
    }

    val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt
    println(nrcores)

    val Counter0 = new Array[Array[Long]](nrcores)
    val Counter1 = new Array[Array[Long]](nrcores)
    val Counter2 = new Array[Array[Long]](nrcores)
    val Counter3 = new Array[Array[Long]](nrcores)
    val CycleCounter = new Array[Array[Long]](nrcores)
    val RefCycleCounter = new Array[Array[Long]](nrcores)
    val TSCCounter = new Array[Array[Long]](nrcores)

    println("\t\t\tevents[0].event_number = ARCH_LLC_REFERENCE_EVTNR;\n\t\t\tevents[0].umask_value =  ARCH_LLC_REFERENCE_UMASK;\n\n\t\t\tevents[1].event_number = ARCH_LLC_MISS_EVTNR;\n\t\t\tevents[1].umask_value = ARCH_LLC_MISS_UMASK;\n\n\t\t\tevents[2].event_number = UNC_L3_MISS_ANY_EVTNR;\n\t\t\tevents[2].umask_value = UNC_L3_MISS_ANY_UMASK;\n\n\t\t\tevents[3].event_number = MEM_LOAD_RETIRED_L2_HIT_EVTNR;\n\t\t\tevents[3].umask_value = MEM_LOAD_RETIRED_L2_HIT_UMASK;")
    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong)
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong)
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong)
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong)
    }

    for (i <- 0 until nrcores)
    {
      println("-------------")
      println("Core " + i)
      println (Counter0(i).mkString(" "))
      println (Counter1(i).mkString(" "))
      println (Counter2(i).mkString(" "))
      println (Counter3(i).mkString(" "))
      println("-")
      println(CycleCounter(i).mkString(" "))
      println(RefCycleCounter(i).mkString(" "))
      println(TSCCounter(i).mkString(" "))
    }




    val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong )// /1024/1024 )
    val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ) // /1024/1024)


    import roofline.plot._
    import roofline.services._
    import roofline.quantities._


    println("-------------")
    println("read")
    println (mcread.mkString(" "))
    println("write")
    println (mcwrite.mkString(" "))


    val all = mcread.size-1
    var nr = 1
    val b_write = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )


    nr = 3
    val b_read = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )
    /*println("-----")
    println("Bytes: " + (mcread(nr) + mcwrite(nr)) )
    println("Cycles: " + TSCCounter(0)(nr))*/
    nr = 4
    val b_read_write = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )
    nr = 5
    val b_update = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )
    nr = 6
    val b_avx_load = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )
    println("-----")
    println("Bytes: " + (mcread(all-nr) + mcwrite(all-nr)) )
    println("Cycles: " + TSCCounter(0)(all-nr))
    nr = 7
    val b_avx_write = Throughput(
      TransferredBytes(mcread(all-nr) + mcwrite(all-nr)),
      Cycles(TSCCounter(0)(all-nr)) )
    println("-----")
    println("Bytes: " + (mcread(all-nr) + mcwrite(all-nr)) + "..." + all + " - " +  mcwrite(all-nr))
    println("Cycles: " + TSCCounter(0)(all-nr))

    println(b_write.value)
    println(b_read.value)
    println(b_read_write.value)
    println(b_update.value)
    println(b_avx_load.value)
    println(b_avx_write.value)


    return       List(
      ("Write", b_write),
      ("Read", b_read),
      //("Read/Write", b_read_write),
      ("Update", b_update)//,
      //("AVX Load", b_avx_load),
      //("AVX SStore", b_avx_write)
    )
  }


}
*/
