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



  def test_daxpy() =
  {
    val filename = "daxpy"
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")

    cmdbat.println(" #include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}\n\n\ndouble * x, *y;\ndouble alpha;\n\n\n\nint main () {\n\n  perfmon_init(1,false,false,false);\n\n  const int page = 1024*4;\n  int mem = 256*512; //128 MB\n  \n  \n  \n")
    cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
    //cmdbat.println("long vectorSize = 500;")
    cmdbat.println("\n  {   \n    x = (double*)  _mm_malloc( vectorSize*sizeof(double), page );\n    if (!x) {\n      std::cout << \"malloc failed\";\n      //perfmon_end();\n      return -1;\n    }\n    y = (double*)  _mm_malloc( vectorSize*sizeof(double), page );\n    if (!x) {\n      std::cout << \"malloc failed\";\n      //perfmon_end();\n      return -1;\n    }\n  // initialize factors\n\talpha=1.1;\n\n\n\n  for (long j = 0; j < 11; j++)\n  {\n       \tfor (long i=0; i<vectorSize; i++){\n        x[i]=1.2;\n        y[i]=1.3;\n        flushCacheLine(&(x[i]));\n        flushCacheLine(&(y[i]));\n      }\n      perfmon_start();\n     cblas_daxpy( vectorSize, alpha, x, 1, y, 1);          \n     perfmon_stop();\n\n   }\n   _mm_free(x);\n   _mm_free(y);          \n  }\n  perfmon_end();\n  \n  \n}")
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
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong)
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong)
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong)
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong)
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong)
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




    val mcread = getFile(temp.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong )// /1024/1024 )
    val mcwrite = getFile(temp.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ) // /1024/1024)


    println (sCounter0.mkString(" "))
    println (sCounter1.mkString(" "))
    println (sCounter2.mkString(" "))
    println (sCounter3.mkString(" "))
    println("-------------")
    println("read")
    println (mcread.mkString(" "))
    println("write")
    println (mcwrite.mkString(" "))
    /*
    var vectorSize = 500
    var i = 0
    while (vectorSize <= 10000 * 1000)
    {
      for (j <- 0 until 11)
      {

      }
      vectorSize = vectorSize * 2
    }   */



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
