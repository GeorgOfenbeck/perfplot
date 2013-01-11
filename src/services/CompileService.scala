package services

/**
 * Georg Ofenbeck
 First created:
 * Date: 19/12/12
 * Time: 14:21 
 */

import java.io._
import java.nio.file._
import roofline.Config


object CompileService {


  def run(args: String) = {

  }

  def compile(codeFile: String) = {
    val compiler = if ( System.getProperty("os.name").contains("Windows") ) {
      if (Config.use_gcc)
        new File("C:\\cygwin\\bin","x86_64-w64-mingw32-gcc.exe")
      else
      {
        new File("C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\intel64","icl.exe")
      }
    }
    else {
      new File("/opt/intel/composer_xe_2013.0.079/bin/intel64/","icc")
    }

    if (!Config.use_gcc && System.getProperty("os.name").contains("Windows")  )
    {
      val cmdbat = new PrintStream("C:\\Users\\ofgeorg\\command.bat")
      cmdbat.println("echo \"compiling !\"")
      cmdbat.println("\"" + compiler.getAbsolutePath +"\" " + codeFile +".cpp -o "  + codeFile +".exe  /link C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\MeasuringCore.lib /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")

      cmdbat.println("echo \"finished\"")
      cmdbat.close()
      //execute("C:\\Users\\ofgeorg\\compile.bat")

      //Modified the compilervars.bat to be able to do this - need to fix this !
      execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell")
    }
    else
      execute(compiler.getAbsolutePath + " -fasm-blocks " + codeFile +".cpp " + " pcm/MeasuringCore.lib -lpthread -lrt -o "+ codeFile + ".x")
  }

  def execute (command: String, wd: File)
  {
    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = runtime.exec(command,null,wd)
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
      System.err.println("compilation: had errors")
    } else {
      if (true ) System.err.println("compilation: ok")
    }
  }

  def execute (command : String)
  {
    val runtime = java.lang.Runtime.getRuntime()
    val compileProcess = runtime.exec(command)
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
      System.err.println("compilation: had errors")
    } else {
      if (true ) System.err.println("compilation: ok")
    }
  }

}

/*


//CCompile handling compilation - based on version written by Alen Stojanov for SpiralS
trait CCompile {

  private def findExec(executableName: String): File = {
    val systemPath = System.getenv("PATH")
    val pathDirs = systemPath.split(File.pathSeparator)
    var fullyQualifiedExecutable:File = null
    pathDirs.foreach ( pathDir => {
      val file = new File(pathDir, executableName)
      if (file.isFile()) {
        fullyQualifiedExecutable = file
      }
    })
    fullyQualifiedExecutable
  }

  /*
  private def getTempDir () : (File, String ) =
  {
    Files.createTempDirectory()
  } */




   def compile(codeFile: File) = {

    if ( false /*Config.debug*/ ) System.err.println("BridJ compilation started ...");

    val compiler = if ( System.getProperty("os.name").contains("Windows") ) {
      //findExec("cc.exe")
      //findExec("x86_64-w64-mingw32-gcc.exe")
      //findExec("")
        new File("C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\intel64","icl.exe")
    }
      else {
      new File("/opt/intel/composer_xe_2013.0.079/bin/intel64/","icc")
      //findExec("icc")

    }

    if ( compiler eq null )
    {
      assert (false)
    }
      //throw new CCompilerException("GCC/ICC compiler was not found in the PATH directories")

    if ( false /*Config.debug*/ ) System.err.println("Using icc: " + compiler.getAbsolutePath());

    //val codeFileName = codeFile.getAbsolutePath()
    //val objectFileName = codeFileName.substring(0, codeFileName.length() - 2) + ".o"
    /*
    val libFileName = if ( System.getProperty("os.name").contains("Windows") ) {
      codeFileName.substring(0, codeFileName.length() - 2) + ".dll"
    } else if ( System.getProperty("os.name").contains("Mac OS") ) {
      codeFileName.substring(0, codeFileName.length() - 2) + ".dylib"
    } else {
      codeFileName.substring(0, codeFileName.length() - 2) + ".so"
    } */

    val runtime = java.lang.Runtime.getRuntime()


   /*
   val cmdbat = new PrintStream("C:\\Users\\ofgeorg\\command.bat")
   cmdbat.println("\"" + compiler.getAbsolutePath + "\" -o C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\check.exe C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\main.cpp /link C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\MeasuringCore.lib /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")
   cmdbat.close()
   */


    //execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell")
    //execute("C:\\Windows\\SysWOW64\\cmd.exe /E:ON /V:ON /c \"\"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\ipsxe-comp-vars.bat\" intel64 vs2012\"")



      //" /C " +compiler.getAbsolutePath + " -o C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\check.exe C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\main.cpp /link C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\MeasuringCore.lib /DYNAMICBASE \"kernel32.lib\" \"user32.lib\" \"gdi32.lib\" \"winspool.lib\" \"comdlg32.lib\" \"advapi32.lib\" \"shell32.lib\" \"ole32.lib\" \"oleaut32.lib\" \"uuid.lib\" \"odbc32.lib\" \"odbccp32.lib\" ")
    //execute(" \"C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\compilervars.bat\" intel64 vs2012shell" +

     //execute("C:\\Users\\ofgeorg\\IdeaProjects\\roofline2\\check\\compile.bat")


  }




} */
