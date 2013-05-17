package perfplot

import java.io.File

object Config {
  def isWin =
  {
    if ( System.getProperty("os.name").contains("Windows") )
      true
    else
      false
  }

  def isMac =
  {
    if ( System.getProperty("os.name").toLowerCase.contains("mac") )
      true
    else
      false
  }

  var use_cache = false;
  var use_gcc = false;
  var debug = false;
  var home = System.getProperty( "user.home" )
  var pwd = System.getProperty( "user.dir" )

  var gnuplot = if (isWin) "C:\\Program Files (x86)\\gnuplot\\bin\\gnuplot.exe" else "gnuplot"


  var win_gcc = new File("C:\\cygwin\\bin","x86_64-w64-mingw32-gcc.exe")
  //win_icc is working with a hack atm - look into CompileService!
  var win_icc = new File("C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\intel64","icl.exe")

  var measurement_Threshold : Long = (0.1 * scala.math.pow(10,9)).toLong; //4giga cycles - TODO: GO: replace by seconds
  var testDerivate_Threshold =  0.0005
  var repeats = 3


  var def_alignment = 64

  def MeasuringCore: File = if (isWin)
                         new File("C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\","MeasuringCore.lib")
                      else
                         new File(pwd + File.separator + "pcm" + File.separator + "MeasuringCore.lib")


  def default_flags = flag_c99 + flag_optimization + flag_hw

  def flag_c99 = if (isWin) " /Qstd=c99" else " -std=c99"
  def flag_mkl = if (isWin) " /Qmkl" else " -mkl"
  def flag_mkl_seq = if (isWin) " /Qmkl:sequential" else " -mkl:sequential"
  def flag_optimization = if (isWin) " /O3" else " -O3"
  def flag_no_optimization = if (isWin) " /Od" else " -O0"
  def flag_hw = if (isWin) " /QxHost" else " -xHost"
  def flag_novec = if (isWin) " /Qno-simd /Qno-vec" else " -no-simd -no-vec"
  def flag_report = if (isWin) " /Qvec-report3" else " -vec-report3"


  var MeasuringCoreH = "#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\n#ifdef __cplusplus\nextern \"C\" {\n#endif\n//int measurement_init(int type, bool flushData , bool flushICache , bool flushTLB );\nint measurement_init(long * custom_counters , unsigned long offcore_response0 , unsigned long offcore_response1 );\nvoid measurement_start();\nvoid measurement_stop(unsigned long runs);\nvoid measurement_end();\n// Start Dani\nunsigned long measurement_run_multiplier(unsigned long threshold);\n\n//void measurement_meanSingleRun();\n\nvoid measurement_emptyLists(bool clearRuns);\nvoid dumpMeans();\n\nunsigned long measurement_getNumberOfShifts(unsigned long size, unsigned long initialGuess);\n// End Dani\n\nvoid flushITLB();\nvoid flushDTLB();\nvoid flushICache();\nvoid flushDCache();\n\nunsigned long getLLCSize();\n\n\n#ifdef __cplusplus\n}\n#endif\n#endif\n"

  var delete_temp_files = true

  def result_folder: File = if (isWin)
      new File("C:\\Users\\ofgeorg\\IdeaProjects\\results\\")
        else
      new File(pwd + File.separator + "results" + File.separator )
}



