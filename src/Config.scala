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

  def MeasuringCore: File = if (isWin)
                         new File("C:\\Users\\ofgeorg\\IdeaProjects\\perfplot\\pcm\\","MeasuringCore.lib")
                      else
                         new File(pwd + File.separator + "pcm" + File.separator + "MeasuringCore.lib")


  def default_flags = flag_c99 + flag_optimization + flag_hw

  def flag_c99 = if (isWin) " /Qstd=c99" else " -std=c99"
  def flag_mkl = if (isWin) " /Qmkl" else " -mkl"
  def flag_mkl_seq = if (isWin) " /Qmkl:sequential" else " -mkl:sequential"
  def flag_optimization = if (isWin) " /O3" else " -O3"
  def flag_hw = if (isWin) " /Qmarch=corei7-avx /QxHost" else " -march=corei7-avx -xHost"
  def flag_novec = if (isWin) " /Qno-simd /Qno-vec" else " -no-simd -no-vec"


  val MeasuringCoreH = "#ifndef MEASURING_CORE_HEADER\n#define MEASURING_CORE_HEADER\n\n#include \"cpuid_info.h\"\n\n\n//int measurement_init(int type, bool flushData , bool flushICache , bool flushTLB );\nint measurement_init(long * custom_counters = NULL, long offcore_response0 = 0, long offcore_response1 = 0);\nvoid measurement_start();\nvoid measurement_stop(long runs=1);\nvoid measurement_end();\n// Start Dani\n//bool measurement_customTest(size_t runs, size_t vlen);\nbool measurement_testDerivative(size_t runs, double threshold, size_t points=1);\n//void measurement_meanSingleRun();\n//bool measurement_testSD(size_t runs);\nvoid measurement_emptyLists(bool clearRuns=true);\nvoid dumpMeans();\n\nunsigned long getNumberOfShifts(unsigned long size, unsigned long initialGuess);\n// End Dani\n\nvoid flushITLB();\nvoid flushDTLB();\nvoid flushICache();\nvoid flushDCache();\n\n// Vicky --- Functions for getting cache parameters with CPUID\n\n\n\ncpuid_cache_descriptor_t getTLBinfo(cpuid_leaf2_qualifier_t cacheType);\nunsigned long getLLCSize();\n\n#endif"


}

