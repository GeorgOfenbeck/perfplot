package services

/**
 * Georg Ofenbeck
 First created:
 * Date: 19/12/12
 * Time: 14:21 
 */

import java.io._

import roofline.Config


object CommandService {


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
    val compiler = if ( Config.isWin) {
      if (Config.use_gcc)
        Config.win_gcc
      else
        Config.win_icc
    }
    /*
    else { //asuming its linux - we use hardcoded gcc/icc
      new File("/opt/intel/composer_xe_2013.0.079/bin/intel64/","icc")
    }*/
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
      cmdbat.println("echo \"finished\"")
      cmdbat.close()
      execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell")
    }
    else
      execute("icc " + codeFile +".cpp " + Config.MeasuringCore.getAbsolutePath + "  -lpthread -lrt -o "+ codeFile + ".x")
    //execute(compiler.getAbsolutePath + " -std=c99 -mkl -fasm-blocks " + codeFile +".cpp " + " pcm/MeasuringCore.lib -lpthread -lrt -o "+ codeFile + ".x")
  }



  def execute (command: String, wd: File = null)
  {
    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = if (wd)
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

