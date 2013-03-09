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
  val use_cache = false;
  val use_gcc = false;
  val debug = true;
  val home = System.getProperty( "user.home" )
  val pwd = System.getProperty( "user.dir" )

  val gnuplot = if (isWin) "C:\\Program Files (x86)\\gnuplot\\bin\\gnuplot.exe" else "gnuplot"


  val win_gcc = new File("C:\\cygwin\\bin","x86_64-w64-mingw32-gcc.exe")
  //win_icc is working with a hack atm - look into CompileService!
  val win_icc = new File("C:\\Program Files (x86)\\Intel\\Composer XE 2013\\bin\\intel64","icl.exe")

  val measurement_Threshold : Long = (0.1 * scala.math.pow(10,9)).toLong; //4giga cycles - TODO: GO: replace by seconds
  val testDerivate_Threshold =  0.0005
  val repeats = 3

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


  val MeasuringCoreH = "#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n\n\n#ifdef __cplusplus\nextern \"C\" {\n#endif\n//int measurement_init(int type, bool flushData , bool flushICache , bool flushTLB );\nint measurement_init(long * custom_counters , unsigned long offcore_response0 , unsigned long offcore_response1 );\nvoid measurement_start();\nvoid measurement_stop(unsigned long runs);\nvoid measurement_end();\n// Start Dani\n//bool measurement_customTest(size_t runs, size_t vlen);\nunsigned long measurement_run_multiplier(unsigned long threshold);\nbool measurement_testDerivative(size_t runs, double alpha_threshold, double avg_threshold, double time_threshold, double *d, size_t points);\n//void measurement_meanSingleRun();\n//bool measurement_testSD(size_t runs);\nvoid measurement_emptyLists(bool clearRuns);\nvoid dumpMeans();\n\nunsigned long measurement_getNumberOfShifts(unsigned long size, unsigned long initialGuess);\n// End Dani\n\nvoid flushITLB();\nvoid flushDTLB();\nvoid flushICache();\nvoid flushDCache();\n\n\n#ifdef __cplusplus\n}\n#endif\n#endif"

  val delete_temp_files = true

  def result_folder: File = if (isWin)
      new File("C:\\Users\\ofgeorg\\IdeaProjects\\results\\")
        else
      new File(pwd + File.separator + "results" + File.separator )
}



