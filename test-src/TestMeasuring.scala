/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59
 */

import org.scalatest.Suite

import roofline.plot._
import roofline.services._

import services._
import java.io._
import scala.io._



class TestMeasuring extends Suite{

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


  def test_Memory_Bandwith()
  {
    val filename = "mem"
    val temp : File = File.createTempFile("memtest","");
    temp.delete()
    temp.mkdir()


    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")
    cmdbat.println("#include <iostream> \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\n\n\nvoid flushCacheLine(void *p){\n  __asm__ __volatile__ (\"clflush %0\" :: \"m\" (*(char*)p));\n}")
    cmdbat.println("int main () {")

    cmdbat.println("std::cout << \"malloc test\"; ")
    cmdbat.println("  perfmon_init(3,false,false,false);\n ");
    cmdbat.println("std::cout << \"malloc test2\"; ")
    cmdbat.println("\n    double * buffer;\n    \n    const int page = 1024*4;\n    const int mem = 256*64; //12 MB")
    cmdbat.println(" buffer = (double*)  _mm_malloc( mem*page, page );\n  if (!buffer) {\n    std::cout << \"malloc failed\";\n")
    cmdbat.println("perfmon_end();")
    cmdbat.println("return -1;\n  }\n   std::cout << \"malloc worked\"; ")
    cmdbat.println()
    /*
    cmdbat.println("    for(long i = 0; i< mem*page/sizeof(double); i++)    \n      buffer[i] = i%2;    \n ")
    cmdbat.println("for(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));")
    cmdbat.println("double result = 0;    \nperfmon_start();\n    for(long i = 0; i< mem*page/sizeof(double); i=i+page)    \n      result += buffer[i];\nperfmon_stop();\n\t\t\tstd::cout << result;")
    */
    cmdbat.println("    for(long i = 0; i< mem*page/sizeof(double); i++)    \n      buffer[i] = i%2;    \n ")
    cmdbat.println("for(long i = 0; i< mem*page/sizeof(double); i++)\n      flushCacheLine(&(buffer[i]));")
    cmdbat.println("\nperfmon_start();\n    for(long i = 0; i< mem*page/sizeof(double); i=i+page)    \n      buffer[i] = 3.0;\nperfmon_stop();\n\t\t\t")
    cmdbat.println("double result = 0;    \n\n    for(long i = 0; i< mem*page/sizeof(double); i=i+page)    \n      result += buffer[i];\n\n\t\t\tstd::cout << result;")

    cmdbat.println("std::cout << \"\\n\" << (mem/sizeof(double)) << \"\\n\";")
    cmdbat.println("perfmon_end();")

    cmdbat.println("_mm_free (buffer);")
    cmdbat.println("    return 0;\n  }")
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

    val Counter0 = new Array[Long](nrcores)
    val Counter1 = new Array[Long](nrcores)
    val Counter2 = new Array[Long](nrcores)
    val Counter3 = new Array[Long](nrcores)
    val CycleCounter = new Array[Long](nrcores)
    val RefCycleCounter = new Array[Long](nrcores)
    val TSCCounter = new Array[Long](nrcores)

    println("\t\t\tevents[0].event_number = ARCH_LLC_REFERENCE_EVTNR;\n\t\t\tevents[0].umask_value =  ARCH_LLC_REFERENCE_UMASK;\n\n\t\t\tevents[1].event_number = ARCH_LLC_MISS_EVTNR;\n\t\t\tevents[1].umask_value = ARCH_LLC_MISS_UMASK;\n\n\t\t\tevents[2].event_number = UNC_L3_MISS_ANY_EVTNR;\n\t\t\tevents[2].umask_value = UNC_L3_MISS_ANY_UMASK;\n\n\t\t\tevents[3].event_number = MEM_LOAD_RETIRED_L2_HIT_EVTNR;\n\t\t\tevents[3].umask_value = MEM_LOAD_RETIRED_L2_HIT_UMASK;")
    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").toLong
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").toLong
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").toLong
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").toLong
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").toLong
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").toLong
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").toLong
    }

    for (i <- 0 until nrcores)
    {
      println("-------------")
      println("Core " + i)
      println (Counter0(i))
      println (Counter1(i))
      println (Counter2(i))
      println (Counter3(i))
      println("-")
      println(CycleCounter(i))
      println(RefCycleCounter(i))
      println(TSCCounter(i))
    }

  }

  def Peak_Performance()
  {
    val filename = "tmp"
    val temp : File = File.createTempFile("perfplot","");
    temp.delete()
    temp.mkdir()



    val test_source = "#include <iostream> \n#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\nint perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );\nvoid perfmon_start();\nvoid perfmon_stop();\nvoid perfmon_end();\n\n#endif\n\n#include <immintrin.h>\nint main () {\n    perfmon_init(1,false,false,false);\n        "

    val cmdbat = new PrintStream(temp.getPath + File.separator +  filename + ".cpp")
    cmdbat.println(test_source)


    val DLP = 5;
    cmdbat.println("const long DLP = "+DLP+";\n\t\t\t\tconst long iterations = 10000000;")
    cmdbat.println("    double tmp[2];\n    __m128d a[DLP], b[DLP], c,d;\n    double t;\n\n    t = 1.1;\n\n    for (int i = 0; i < DLP; i++) {\n      tmp[0] = t;\n      t += 0.1;\n      tmp[1] = t;\n      t += 0.1;\n      a[i] = _mm_loadu_pd(tmp);\n      b[i] = _mm_loadu_pd(tmp);\n    }\n\n    tmp[0] = 1;\n    tmp[1] = 1;\n    c = _mm_loadu_pd(tmp);\n    d = _mm_loadu_pd(tmp);")
    cmdbat.println("perfmon_start();");

    cmdbat.println("for (long i = 0; i < 100000; i++) {")
    for (i <- 0 until 100)
      for (j <- 0 until DLP)
      {
        cmdbat.println("\ta["+j+"] = _mm_add_pd(a["+j+"], c);")
        cmdbat.println("\tb["+j+"] = _mm_mul_pd(b["+j+"], d);")
      }

    cmdbat.println("}")
    cmdbat.println("perfmon_stop();")

    cmdbat.println("  double result = 0;\n  double result2 = 0;\n  for (int i = 0; i < 10; i++) {\n  _mm_storeu_pd(tmp, a[i]);\t\t\t\t\t\n  result += tmp[0];\n  result += tmp[1];\n  _mm_storeu_pd(tmp, b[i]);\n  result2 += tmp[0];\n  result2 += tmp[1];\n\t\t\t\t}\n  std::cout << result;\n  std::cout << result2;")

    /*
    cmdbat.println("perfmon_start();");
    cmdbat.println("      __asm{\n        xorpd xmm0, xmm0\n        xorpd xmm1, xmm1\n        xorpd xmm2, xmm2\n        xorpd xmm3, xmm3\n        xorpd xmm4, xmm4\n        xorpd xmm5, xmm5\n        xorpd xmm6, xmm6\n        xorpd xmm7, xmm7\n        xorpd xmm8, xmm8\n        xorpd xmm9, xmm9\n        xorpd xmm10, xmm10\n        xorpd xmm11, xmm11\n        xorpd xmm12, xmm12\n        xorpd xmm13, xmm13\n        xorpd xmm14, xmm14\n        xorpd xmm15, xmm15\n      }")
    cmdbat.println("      for (int i = 0; i< 200000; i++)\n      __asm{" )
    //thats 8k avx instructions
    for (i <- 0 until 1000)
      cmdbat.println("        addsd     xmm0, xmm1\n        mulsd     xmm2, xmm3\n\n        addsd     xmm4, xmm5\n        mulsd     xmm6, xmm7\n\n        addsd     xmm8, xmm9\n        mulsd     xmm10, xmm11\n\n        addsd     xmm12, xmm13\n        mulsd     xmm14, xmm15")
      //cmdbat.println("        vaddpd    ymm0, ymm0, ymm1        \n        vmulpd    ymm2, ymm2, ymm3\n\n        vaddpd    ymm4, ymm4, ymm5\n        vmulpd    ymm6, ymm6, ymm7\n        \n        vaddpd    ymm8, ymm8, ymm9\n        vmulpd    ymm10, ymm10, ymm11      \n        \n        vaddpd    ymm12, ymm12, ymm13\n        vmulpd    ymm14, ymm14, ymm15        ")
    cmdbat.println("} ")
    cmdbat.println("perfmon_stop();")
    */

    cmdbat.println("\n\n\n\nperfmon_end();\n    return 0;\n  }")
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

    //at this point we've got the measurment results - check which core we are on

    //CompileService.execute("echo hello")

    /*
    val filecheck = new File(temp.getPath + File.separator + "CPUBrandString.txt")
    System.out.println("waiting for results")
    System.out.println(filecheck.getAbsolutePath)
    while (!filecheck.exists())
    {
      //System.out.print(".")
    }
    System.out.println("done")
    val source = scala.io.Source.fromFile(temp.getPath + File.separator + "CPUBrandString.txt")
    val lines = source.mkString
    source.close ()
    System.out.println(lines)
  */



    val nrcores = getFile(temp.getPath + File.separator + "NrCores.txt").toInt
    println(nrcores)


    val Counter0 = new Array[Long](nrcores)
    val Counter1 = new Array[Long](nrcores)
    val Counter2 = new Array[Long](nrcores)
    val Counter3 = new Array[Long](nrcores)
    val CycleCounter = new Array[Long](nrcores)
    val RefCycleCounter = new Array[Long](nrcores)
    val TSCCounter = new Array[Long](nrcores)


    for (i <- 0 until nrcores)
    {
      Counter0(i) = getFile(temp.getPath + File.separator + "Custom_ev0_core" + i + ".txt").toLong
      Counter1(i) = getFile(temp.getPath + File.separator + "Custom_ev1_core" + i + ".txt").toLong
      Counter2(i) = getFile(temp.getPath + File.separator + "Custom_ev2_core" + i + ".txt").toLong
      Counter3(i) = getFile(temp.getPath + File.separator + "Custom_ev3_core" + i + ".txt").toLong
      CycleCounter(i) = getFile(temp.getPath + File.separator + "Cycles_core_" + i + ".txt").toLong
      RefCycleCounter(i) = getFile(temp.getPath + File.separator + "RefCycles_core_" + i + ".txt").toLong
      TSCCounter(i) = getFile(temp.getPath + File.separator + "TSC_core_" + i + ".txt").toLong
    }

    for (i <- 0 until nrcores)
    {
      println("-------------")
      println("Core " + i)
      println (Counter0(i))
      println (Counter1(i))
      println (Counter2(i))
      println (Counter3(i))
      println(CycleCounter(i))
      println(RefCycleCounter(i))
      println(TSCCounter(i))
    }

    //for (i<- 0 until nrcores)


    


    val host = getFile(temp.getPath + File.separator + "CorerunningPerf.txt").toInt
    println("Core running perfplot")
    println(host)


    var ops : Double = 0

    for (i<- 0 until nrcores)
      ops = ops + Counter1(i)

    var cycles : Double = 0
    for (i<- 0 until nrcores)
      cycles = cycles + CycleCounter(i)

    for (i<- 0 until nrcores)
    {
      println("------------------------")
      println("Core "+i)
      val cycles :Double = CycleCounter(i)
      val refcycles :Double = RefCycleCounter(i)
      val tsccycles : Double = TSCCounter(i)
      val flops = Counter1(i)
      println("Flops/Cycle")
      println(flops/cycles)
      println("Flops/RefCycle")
      println(flops/refcycles)
      println("Flops/TSC")
      println(flops/tsccycles)
    }

    //val opscycle = ops/cycles
    //println(opscycle)
    //for (i <- 0 until 16)
    //println("xorpd xmm"+i+", xmm"+i)



  }




}
