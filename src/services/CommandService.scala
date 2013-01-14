/** 
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59 
 */


package roofline
package services

import java.io._


object CommandService {

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

  /*
  def runCommand(workingDirectory : File, command: String, arguments : Array[String], desiredExitValue : Int, showOutput : Boolean )
  {
    //setup the command line
    val cmdLine = new CommandLine(command)
    cmdLine.addArguments(arguments)

    //setup executor
    val executer = new DefaultExecuter()
  } */


}
