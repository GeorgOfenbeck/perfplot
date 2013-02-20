package perfplot
package services

/**
 * Georg Ofenbeck
 First created:
 * Date: 19/12/12
 * Time: 14:21 
 */

import java.io._
import java.security.MessageDigest
import java.security.DigestInputStream
import perfplot.Config
import perfplot.plot._
import perfplot.quantities._


object CommandService {




  class Counters (nrcores: Int,
      Counter0 : Array[Array[Long]],
      Counter1 : Array[Array[Long]],
      Counter2 : Array[Array[Long]],
      Counter3 : Array[Array[Long]],
      Counter4 : Array[Array[Long]],
      Counter5 : Array[Array[Long]],
      Counter6 : Array[Array[Long]],
      Counter7 : Array[Array[Long]],
      CycleCounter : Array[Array[Long]],
      RefCycleCounter : Array[Array[Long]],
      TSCCounter: Array[Array[Long]],


      SCounter0: Array[Long],
      SCounter1: Array[Long],
      SCounter2: Array[Long],
      SCounter3: Array[Long],
      SCounter4: Array[Long],
      SCounter5: Array[Long],
      SCounter6: Array[Long],
      SCounter7: Array[Long],

      sCycleCounter: Array[Long],
      sRefCycleCounter: Array[Long],
      avgTSCCounter: Array[Long],
      mcread: Array[Long],
      mcwrite: Array[Long],
      nrruns : Array[Long]
    )
  {


    def getSCounter3():Array[Long] = SCounter3

    def toFlopSeries (name: String, sizes: List[Long], warmup : Int = 1) =
    {
      val repeats = TSCCounter(0).size/sizes.size
      val series = new Array[OperationPoint](sizes.size)

      for (i <- 0 until sizes.size)
      {
        series(i) = OperationPoint(sizes(i),
          getFlops( ( (i*repeats+warmup) until (i+1)*repeats) ).map(x => Flops(x))
        )
      }
      OperationSeries(name,series.toList)
    }

    def toPerformanceSeries (name: String, sizes: List[Long], warmup : Int = 1) =
    {
      val repeats = TSCCounter(0).size/sizes.size
      val series = new Array[PerformancePoint](sizes.size)
      for (i <- 0 until sizes.size)
      {
        series(i) = PerformancePoint(sizes(i),
          getPerformance( ( (i*repeats+warmup) until (i+1)*repeats) )
        )
      }
      PerformanceSeries(name,series.toList)
    }

    def toPerformanceSeries_fft (name: String, sizes: List[Long], warmup : Int = 1) =
    {
      val repeats = TSCCounter(0).size/sizes.size
      val series = new Array[PerformancePoint](sizes.size)
      for (i <- 0 until sizes.size)
      {
        val pseudo_ops: Double = 5 * sizes(i) * 100.0 * (Math.log(sizes(i)*1.0)/math.log(2)) //100 cause of repeats
        System.out.println("Pseudo ops: " + pseudo_ops)
        val t = getPerformance( ( (i*repeats+warmup) until (i+1)*repeats))
        val pseudo_perf = t.map( perf => Performance(PseudoFlops(pseudo_ops),Cycles(perf.time.value)))
        series(i) = PerformancePoint(sizes(i), pseudo_perf  )

      }
      PerformanceSeries(name,series.toList)
    }


    def get_scalar_double_flops = SCounter0
    def get_sse_double_flops = SCounter1.map(x => x*2)
    def get_avx_double_flops = SCounter2.map(x => x*4)
    def get_scalar_single_flops = SCounter3
    def get_sse_single_flops = SCounter4.map(x => x*4)
    def get_avx_single_flops = SCounter5.map(x => x*8)

    def getFlops(r : Range): List[Long] = (for (i <- r) yield getFlops(i)).toList


    def getnrruns(i: Int) = nrruns(i)


    def getbytes_read (i: Int) : Long = mcread(i)
    def getbytes_write (i: Int) : Long = mcwrite(i)

    def getbytes_transferred (i: Int) : Long =
    {
      mcread(i) + mcwrite(i)
    }



    def getTSC (i: Int) : Long =
    {
      avgTSCCounter(i)
    }

    def getFlops (i: Int) : Long =
    {
      (
        get_scalar_double_flops(i) + //+ get_scalar_single_flops(i) +
      get_sse_double_flops(i) + get_avx_double_flops(i)
        /*+ get_sse_single_flops(i) +
     + get_avx_single_flops(i) */
        )

    }

    def getFlops(core: Int, exp: Int) =
    {
      Counter0(core)(exp) +
      Counter1(core)(exp)*2 +
      Counter2(core)(exp)*4 +
      Counter3(core)(exp) +
      Counter4(core)(exp)*4 +
      Counter5(core)(exp)*8
    }
    def getPerformance(r: Range): List[Performance] = (for (i <- r) yield getPerformance(i)).toList
    def getPerformance(i: Int) = Performance(Flops(getFlops(i)),Cycles(TSCCounter(0)(i)))
    def getPerformance(core: Int, exp: Int) = Performance(Flops(getFlops(core,exp)),Cycles(TSCCounter(core)(exp)))



    def customprint() =
    {

      def write( cnt: Array[Long]) =
        cnt.slice(1,11).min

      def read( cnt: Array[Long]) =
        cnt.slice(11,21).min

      println("--------------------------------------------------------------------------------------------------------")
      println(
        "%6s".format("Corenr:") +
          "%12s".format("TSC") +
          "%12s".format("LLC_MISS") +
          "%12s".format("OFFC_R") +
          "%12s".format("OFFC_R") +
          "%12s".format("INST_R")
      )
      for (i <- 0 until nrcores)
        println(
          "%6d".format(i) +
            "%12d".format(TSCCounter(i).slice(1,11).min) +
            "%12d".format(Counter0(i).slice(1,11).min) +
            "%12d".format(Counter1(i).slice(1,11).min) +
            "%12d".format(Counter2(i).slice(1,11).min) +
            "%12d".format(Counter3(i).slice(1,11).min)            
        )

      println(Counter3(0).slice(1,11).size+"----------------"+Counter3(0).slice(11,21).size)
      for (i <- 0 until nrcores)
        println(
          "%6d".format(i) +
            "%12d".format(TSCCounter(i).slice(11,21).min) +
            "%12d".format(Counter0(i).slice(11,21).min) +
            "%12d".format(Counter1(i).slice(11,21).min) +
            "%12d".format(Counter2(i).slice(11,21).min) +
            "%12d".format(Counter3(i).slice(11,21).min)
        )
    }

    def prettyprint () =
    {
      for (j<- 0 until SCounter0.size)
      {
      println(
        "%6s".format("Corenr:") +
        "%12s".format("TSC") +
        "%12s".format("Scalar_D") +
        "%12s".format("SSE_D") +
        "%12s".format("AVX_D") +
        "%12s".format("Scalar_S") +
        "%12s".format("SSE_S") +
        "%12s".format("AVX_S") +
        //"%12s".format("x:") +
        "%12s".format("x") +
        "%12s".format("Perf")
      )
      for (i <- 0 until nrcores)
        println(
          "%6d".format(i) +
          "%12d".format(TSCCounter(i)(j)) +
          "%12d".format(Counter0(i)(j)) +
          "%12d".format(Counter1(i)(j)) +
          "%12d".format(Counter2(i)(j)) +
          "%12d".format(Counter3(i)(j)) +
          "%12d".format(Counter4(i)(j)) +
          "%12d".format(Counter5(i)(j)) +
          //"%12d".format(Counter6(i)(j)) +
          "%12d".format(Counter7(i)(j)) +
          "%12f".format(getPerformance(i,j).value)
        )
      println(nrruns(j) + "--------------------------------------------------------------------------------------------------------")
      println(
        "%6d".format(-1) +
          "%12d".format(avgTSCCounter(j)) +
          "%12d".format(SCounter0(j)) +
          "%12d".format(SCounter1(j)) +
          "%12d".format(SCounter2(j)) +
          "%12d".format(SCounter3(j)) +
          "%12d".format(SCounter4(j)) +
          "%12d".format(SCounter5(j)) +
          //"%12d".format(SCounter6(j)) +
          "%12d".format(SCounter7(j)) +
          "%12f".format(getPerformance(j).value) +
          "%12d".format(mcread(j)/1024) +
          "%12d".format(mcwrite(j)/1024)
      )
        println()
      }
    }
  }

  object Counters{

    def apply(path: File):Counters =
    {
      System.out.println("reading counters from " + path.getPath + File.separator)
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

      val nrcores = getFile(path.getPath + File.separator + "NrCores.txt").toInt
      val Counter0 = new Array[Array[Long]](nrcores)
      val Counter1 = new Array[Array[Long]](nrcores)
      val Counter2 = new Array[Array[Long]](nrcores)
      val Counter3 = new Array[Array[Long]](nrcores)
      val Counter4 = new Array[Array[Long]](nrcores)
      val Counter5 = new Array[Array[Long]](nrcores)
      val Counter6 = new Array[Array[Long]](nrcores)
      val Counter7 = new Array[Array[Long]](nrcores)
      val CycleCounter = new Array[Array[Long]](nrcores)
      val RefCycleCounter = new Array[Array[Long]](nrcores)
      val TSCCounter = new Array[Array[Long]](nrcores)


      for (i <- 0 until nrcores)
      {
        Counter0(i) = getFile(path.getPath + File.separator + "Custom_ev0_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter1(i) = getFile(path.getPath + File.separator + "Custom_ev1_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter2(i) = getFile(path.getPath + File.separator + "Custom_ev2_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter3(i) = getFile(path.getPath + File.separator + "Custom_ev3_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter4(i) = getFile(path.getPath + File.separator + "Custom_ev4_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter5(i) = getFile(path.getPath + File.separator + "Custom_ev5_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter6(i) = getFile(path.getPath + File.separator + "Custom_ev6_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        Counter7(i) = getFile(path.getPath + File.separator + "Custom_ev7_core" + i + ".txt").split(" ").map( x => x.toLong).reverse
        CycleCounter(i) = getFile(path.getPath + File.separator + "Cycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
        RefCycleCounter(i) = getFile(path.getPath + File.separator + "RefCycles_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
        TSCCounter(i) = getFile(path.getPath + File.separator + "TSC_core_" + i + ".txt").split(" ").map( x => x.toLong).reverse
      }

      val sCounter0 = new Array[Long](Counter0(0).size)
      val sCounter1 = new Array[Long](Counter1(0).size)
      val sCounter2 = new Array[Long](Counter2(0).size)
      val sCounter3 = new Array[Long](Counter3(0).size)
      val sCounter4 = new Array[Long](Counter4(0).size)
      val sCounter5 = new Array[Long](Counter5(0).size)
      val sCounter6 = new Array[Long](Counter6(0).size)
      val sCounter7 = new Array[Long](Counter7(0).size)

      val sCycleCounter = new Array[Long](CycleCounter(0).size)
      val sRefCycleCounter = new Array[Long](RefCycleCounter(0).size)
      val avgTSCCounter = new Array[Long](TSCCounter(0).size)


      def sumCounters(sumc : Array[Long], orgc: Array[Array[Long]]) =
      {
        for (i <- 0 until sumc.size) {
          sumc(i) = 0
          for (j <- 0 until nrcores)
            sumc(i) = sumc(i) + orgc(j)(i)
        }
      }


      for (i <- 0 until avgTSCCounter.size) { //GO: TODO: should change this to max instead of average
        avgTSCCounter(i) = 0
        for (j <- 0 until nrcores)
          avgTSCCounter(i) = avgTSCCounter(i) + TSCCounter(j)(i)
        avgTSCCounter(i) = avgTSCCounter(i)/nrcores
      }


      sumCounters(sCounter0,Counter0)
      sumCounters(sCounter1,Counter1)
      sumCounters(sCounter2,Counter2)
      sumCounters(sCounter3,Counter3)
      sumCounters(sCounter4,Counter4)
      sumCounters(sCounter5,Counter5)
      sumCounters(sCounter6,Counter6)
      sumCounters(sCounter7,Counter7)

      sumCounters(sCycleCounter, CycleCounter)
      sumCounters(sRefCycleCounter,RefCycleCounter)

      val mcread = getFile(path.getPath + File.separator + "MC_read.txt").split(" ").map( x => x.toLong  ).reverse // /1024/1024 )
      val mcwrite = getFile(path.getPath + File.separator + "MC_write.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)
      val nrruns = getFile(path.getPath + File.separator + "nrruns.txt").split(" ").map( x => x.toLong ).reverse // /1024/1024)


      new Counters(nrcores,
        Counter0,
        Counter1,
        Counter2,
        Counter3,
        Counter4,
        Counter5,
        Counter6,
        Counter7,
        CycleCounter,
        RefCycleCounter,
        TSCCounter,
        sCounter0,
        sCounter1,
        sCounter2,
        sCounter3,
        sCounter4,
        sCounter5,
        sCounter6,
        sCounter7,
        sCycleCounter,
        sRefCycleCounter,
        avgTSCCounter,
        mcread,
        mcwrite,
        nrruns
      )
    }
  }


  def fromScratch (filename: String, codegenfunction: (PrintStream => Unit), flags: String = "") : Counters =
  {
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
    codegenfunction(sourcefile)
    sourcefile.close()
    CommandService.compile(tempdir.getPath + File.separator +  filename, flags)
    System.out.println("executing ...")
    val file = CommandService.measureCode(tempdir, filename)
    System.out.println("gather results ..." + file.getPath)
    val c = Counters.apply(file)
    System.out.println("return results ...")
    return c
  }



  //GO: This function executes the code and returns the directory that contains the result (directory depends on caching)
  def measureCode(path: File, filename : String): File =
  {
    if (Config.use_cache)
    {
      val hash = md5(new File(path.getPath + File.separator + filename + ".cpp")) //this suboptimal - but need to ignore timestamp in .exe
      //if (Config.isWin) md5(new File(path.getPath + File.separator + filename + ".exe")) else md5(new File(path.getPath + File.separator + filename + ".x"))
      val temp : File = File.createTempFile("palceholder","")
      temp.delete()
      val md5dir :File = new File(temp.getParent + File.separator + hash.toString)
      if (md5dir.isDirectory())
      {
        md5dir
      }
      else
      {
        md5dir.mkdir()
        if (Config.isWin)
        {
          //GO: The bat file is a workaround to get admin rights
          val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
          val bat = new PrintStream(path.getPath + File.separator +  filename + ".bat")
          bat.println(batch_source)
          bat.println(filename + ".exe")
          bat.close()
          execute(path.getPath + File.separator + filename + ".bat",md5dir)
        }
        else
        {
          execute(path.getPath + File.separator + filename + ".x", md5dir) //second argument is Working Dir
        }
        md5dir
      }
    }
    else
    {
      if (Config.isWin)
      {
        //GO: The bat file is a workaround to get admin rights
        val batch_source = "@echo off\n\n:: BatchGotAdmin\n:-------------------------------------\nREM  --> Check for permissions\n>nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\"\n\nREM --> If error flag set, we do not have admin.\nif '%errorlevel%' NEQ '0' (\n    echo Requesting administrative privileges...\n    goto UACPrompt\n) else ( goto gotAdmin )\n\n:UACPrompt\n    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\getadmin.vbs\"\n    echo UAC.ShellExecute \"%~s0\", \"\", \"\", \"runas\", 1 >> \"%temp%\\getadmin.vbs\"\n\n    \"%temp%\\getadmin.vbs\"\n    exit /B\n\n:gotAdmin\n    if exist \"%temp%\\getadmin.vbs\" ( del \"%temp%\\getadmin.vbs\" )\n    pushd \"%CD%\"\n    CD /D \"%~dp0\"\n:--------------------------------------\ncopy C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\WinRing* ."
        val bat = new PrintStream(path.getPath + File.separator +  filename + ".bat")
        bat.println(batch_source)
        bat.println(filename + ".exe")
        bat.close()
        execute(path.getPath + File.separator + filename + ".bat")
      }
      else
      {
        execute(path.getPath + File.separator + filename + ".x", path) //second argument is Working Dir
      }
      path
    }



  }





  def getTempDir (dirname: String): File =
  {
    val filename = dirname
    val temp : File = File.createTempFile(filename,"");
    temp.delete()
    temp.mkdir()
    temp
  }

  def runCommand(workingDirectory : String, command: String, arguments : Array[String] )
  {
    System.out.println("executing command")
    System.out.println(workingDirectory,command,arguments)
  }


  def md5(exe: File) = {
    val md = MessageDigest.getInstance("MD5");
    val is = new FileInputStream(exe.getAbsolutePath);
    println("MD5:")
    println(exe.getAbsolutePath)
    val ist =
    try {
       new DigestInputStream(is, md);
      // read stream to EOF as normal...
    }
    ist.close()
    val digest = md.digest()
    digest
  }


  def rungnuplot(arguments : String) =
  {

    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = runtime.exec(Config.gnuplot + " " + arguments)
    val stderr = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));

    var line: String = null //need to use the stderr buffer - otherwise it will hang on windows
    do {
      line = stderr.readLine()
      if (line != null && Config.debug )System.err.println(line) //TODO - Config
    } while(line != null)

    val exitVal = compileProcess.waitFor()

    if (exitVal != 0) {
      System.err.println("compilation: had errors")
    } else {
      if ( Config.debug ) System.err.println("compilation: ok") //TODO - Config
    }

    //if ( Config.debug ) System.err.println("Creating shared library: " + libFileName); //TODO - Config

  }

  def compile(codeFile: String, flags: String) = {
    //
    val compiler: File = if ( Config.isWin) {
      if (Config.use_gcc)
        Config.win_gcc
      else
        Config.win_icc
    }
    else
      Config.win_gcc //asuming its linux - we use hardcoded gcc/icc

    if (Config.use_gcc)
    {
      if (Config.isWin)
      {
        //TODO
      }
      else
      {
        //TODO
      }
    }
    else
    if( Config.isWin )
    {
      //GO: This is a dirty hack to get the ICC running on the command line on windows
      //one needs to set the environment variables for the icl to work - i did not figure out a smart
      //way to do so.

      //The hacks works like this - i added a call to "my" .bat file at the end of compilervars.bat
      //not pretty but works

      //if anyone figures out a "clean" way to do this let me know
      val cmdbat = new PrintStream("C:\\Users\\ofgeorg\\command.bat")
      cmdbat.println("echo \"compiling !\"")

      cmdbat.println("\"" + compiler.getAbsolutePath +"\" " + codeFile +".cpp -o "  + codeFile +".exe " + flags + " /link " + Config.MeasuringCore.getAbsolutePath +  " /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")
      cmdbat.println("echo \"finished1\"")
      cmdbat.close()
      execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell")
    }
    else
      execute("icc " + codeFile +".cpp " + Config.MeasuringCore.getAbsolutePath + flags + "  -lpthread -lrt -o "+ codeFile + ".x")
    //execute(compiler.getAbsolutePath + " -std=c99 -mkl -fasm-blocks " + codeFile +".cpp " + " pcm/MeasuringCore.lib -lpthread -lrt -o "+ codeFile + ".x")
  }



  def execute (command: String, wd: File = null)
  {
    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = if (wd != null)
      runtime.exec(command,null,wd)
    else
      runtime.exec(command)
    val stderr = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
    val stdout=new BufferedReader(new InputStreamReader(compileProcess.getInputStream()));
    var line: String = null //need to use the stderr buffer - otherwise it will hang on windows
    do {
      line = stdout.readLine()
      if (line != null && true )System.out.println(line)
    } while(line != null)
    do {
      line = stderr.readLine()
      if (line != null && true )System.err.println(line)
    } while(line != null)
    val exitVal = compileProcess.waitFor();
    if (exitVal != 0) {
      System.err.println("execution of: \"" + command + "\" had errors")
    } else {
      if (true ) System.err.println("execute: ok")
    }
  }

}

