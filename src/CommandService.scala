package perfplot


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

    def getSCounter(i : Long)  =
    {
        i match {
            case 0 =>  SCounter0
            case 1 =>  SCounter1
            case 2 =>  SCounter2
            case 3 =>  SCounter3
        }
    }


    def getSCounter3():Array[Long] = SCounter3

    def get_scalar_double_flops = SCounter0
    def get_sse_double_flops = SCounter1.map(x => x*2)
    def get_avx_double_flops = SCounter2.map(x => x*4)
    def get_scalar_single_flops = SCounter3
    def get_sse_single_flops = SCounter4.map(x => x*4)
    def get_avx_single_flops = SCounter5.map(x => x*8)




    def getCounters(i : Int) = List(SCounter0(i),SCounter1(i),SCounter2(i),SCounter3(i))

    def getFlops(r : Range): List[Long] = (for (i <- r) yield getFlops(i)).toList


    def getnrruns(i: Int) = nrruns(i)


    def getbytes_read (i: Int) : Long = mcread(i)
    def getbytes_write (i: Int) : Long = mcwrite(i)

    def getbytes_transferred (i: Int) : Long =
    {
      mcread(i) + mcwrite(i)
    }

    def getFlops : List[Long] =
    {
      val adjusted_flops = for (i<- 0 until SCounter0.size) yield
      {
        val counters = List(SCounter0(i),SCounter1(i),SCounter2(i),SCounter3(i))
        val appliedmask = (counters,kernel.mask).zipped.map(_*_)
        //val flops = appliedmask.foldLeft(0)(_ + _)
        val flops = appliedmask(0) + appliedmask(1) + appliedmask(2) + appliedmask(3)
        flops
      }


    }

    def getTSC() : Long = {
      val (lower, upper) = avgTSCCounter.sortWith(_<_).splitAt(s.size / 2)
      if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
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
    //def getPerformance(r: Range): List[Performance] = (for (i <- r) yield getPerformance(i)).toList
    //def getPerformance(i: Int) = Performance(Flops(getFlops(i)),Cycles(TSCCounter(0)(i)))
    //def getPerformance(core: Int, exp: Int) = Performance(Flops(getFlops(core,exp)),Cycles(TSCCounter(core)(exp)))



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
        "%12s".format("x:") +
        "%12s".format("x")
        //"%12s".format("Perf")
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
          "%12d".format(Counter7(i)(j))
          //"%12f".format(getPerformance(i,j).value)
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
          //"%12f".format(getPerformance(j).value) +
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
      if (Config.debug) System.out.println("reading counters from " + path.getPath + File.separator)
      def getFile(name: String ) : String =
      {
        val filecheck = new File(name)
        if ( Config.debug ) System.out.println("waiting for: " + filecheck.getAbsolutePath)
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



      //everything read - delete it
      if (Config.delete_temp_files){
        val children = path.list()
        for (i <- 0 until children.length)
        {
          val x = new File(path, children(i))
          x.delete
        }
        // The directory is now empty so delete it
        path.delete()
      }

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


  /*def run_kernel (path: File,kernel: (PrintStream, List[Long],  Array[HWCounters.Counter],  Boolean,  Boolean ) => Unit,
                  sizes_in: List[Long],name: String, counters: Array[HWCounters.Counter],double_precision: Boolean = true, warmData: Boolean = false, flags: String) = */
  def run_kernel (path: File, kernels: List[CodeGeneration], name: String, flags: String)
  {

    val file = new File(path.getPath + File.separator +"flop_"+ name + ".txt")
    if(file.exists() && Config.use_cache)
      println(name + " read from cached file")
    else
    {
      val outputFile1 = new PrintStream(path.getPath + File.separator +"flop_"+ name + ".txt")
      val outputFile2 = new PrintStream(path.getPath + File.separator +"tsc_"+ name + ".txt")
      val outputFile3 = new PrintStream(path.getPath + File.separator +"size_"+ name + ".txt")
      val outputFile4 = new PrintStream(path.getPath + File.separator +"bytes_transferred_" + name + ".txt")
      val outputFile5 = new PrintStream(path.getPath + File.separator +"Counter3_" + name + ".txt")
      val outputFile6 = new PrintStream(path.getPath + File.separator +"bytes_read_" + name + ".txt")
      val outputFile7 = new PrintStream(path.getPath + File.separator +"bytes_write_" + name + ".txt")
      var first1 = true

      var useless_counter = 0
      for (kernel <- kernels)
      {
        val kernel_res = CommandService.fromScratch(name, kernel, flags)
        //kernel_res.prettyprint() //removed since this will just confuse people
        println("running " + kernel.id + ": " + useless_counter + " of " + kernels.size)
        useless_counter = useless_counter + 1

        var first = true
        for (i <- 0 until Config.repeats)
        {
          if (!first)
          {
            outputFile1.print(" ")
            outputFile2.print(" ")
            outputFile4.print(" ")
            outputFile5.print(" ")
            outputFile6.print(" ")
            outputFile7.print(" ")
          }
          first = false
          val counters = kernel_res.getCounters(i)
          val appliedmask = (counters,kernel.mask).zipped.map(_*_)
          //val flops = appliedmask.foldLeft(0)(_ + _)
          val flops = appliedmask(0) + appliedmask(1) + appliedmask(2) + appliedmask(3)
          outputFile1.print(flops)
          outputFile2.print(kernel_res.getTSC(i))
          outputFile4.print(kernel_res.getbytes_transferred(i))
          outputFile5.print(kernel_res.getSCounter3.apply(i))
          outputFile6.print(kernel_res.getbytes_read(i))
          outputFile7.print(kernel_res.getbytes_write(i))

        }
        outputFile1.print("\n")
        outputFile2.print("\n")
        outputFile4.print("\n")
        outputFile5.print("\n")
        outputFile6.print("\n")
        outputFile7.print("\n")
        if (first1)
        {
          outputFile3.print(kernel.size)
          first1 = false
        }
        else
          outputFile3.print(" " + kernel.size)
      }
      outputFile1.close()
      outputFile2.close()
      outputFile3.close()
      outputFile4.close()
      outputFile5.close()
      outputFile6.close()
      outputFile7.close()
    }


  }


  def fromScratch (filename: String, kernel: CodeGeneration, flags: String = "") : Counters =
  {
    val tempdir = CommandService.getTempDir(filename)
    val sourcefile = new PrintStream(tempdir.getPath + File.separator +  filename + ".cpp")
    kernel.print(sourcefile)
    sourcefile.close()
    if (kernel.inline == false && kernel.kernel_code != "") //we need to compile the kernel code in a seperate file
    {
      val fileNameBase = tempdir.getPath + File.separator +  filename
      val sourcefile = new PrintStream(fileNameBase + "_kernel.cpp")
      kernel.printcode(sourcefile)
      sourcefile.close()
    }
    CommandService.compile(tempdir.getPath + File.separator +  filename, flags)
    if (Config.debug) System.out.println("executing ...")
    val file = CommandService.measureCode(tempdir, filename)
    if (Config.debug) System.out.println("gather results ..." + file.getPath)
    val c = Counters.apply(file)
    if (Config.debug) System.out.println("return results ...")
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

    val kernelFile = new File(codeFile + "_kernel.cpp")
    val kernelFileName = if (kernelFile.exists()) kernelFile.getAbsolutePath else ""


    val compiler = if (Config.use_gcc) "g++ " else "icc "

    if (Config.use_gcc && Config.isWin) assert(false, "not supported")


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

      //cmdbat.println("\"" + compiler.getAbsolutePath +"\" " + codeFile +".cpp -o "  + codeFile +".exe " + flags + " /link " + Config.MeasuringCore.getAbsolutePath +  " /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")
      cmdbat.println("\"" + Config.win_icc.getAbsolutePath +"\" " + codeFile + "*.cpp -o "  + codeFile +".exe " + flags + " /link " + Config.MeasuringCore.getAbsolutePath +  " /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")
      cmdbat.println("echo \"finished1\"")
      cmdbat.close()
      execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell")
    }
    else if (Config.isMac)
      execute(compiler +  codeFile + ".cpp " + kernelFileName + " " + Config.MeasuringCore.getAbsolutePath + " " + flags + " -lpthread -lPcmMsr -o "+ codeFile + ".x")
    else
      execute(compiler + codeFile +".cpp " + kernelFileName + " " + Config.MeasuringCore.getAbsolutePath + flags + "  -lpthread -lrt -o "+ codeFile + ".x")
    //execute(compiler.getAbsolutePath + " -std=c99 -mkl -fasm-blocks " + codeFile +".cpp " + " pcm/MeasuringCore.lib -lpthread -lrt -o "+ codeFile + ".x")
  }



  def execute (command: String, wd: File = null)
  {
    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = if (wd != null)
      if(!Config.isWin)
        runtime.exec(Array("sh","-c",command),null,wd)
       else
        runtime.exec(command,null,wd)
    else
      if(!Config.isWin)
        runtime.exec(Array("sh","-c",command))
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
      if (Config.debug) System.err.println("execute: ok")
    }
    compileProcess.destroy()
  }

}

