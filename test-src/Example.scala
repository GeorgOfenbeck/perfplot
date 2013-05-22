/** 
 * Georg Ofenbeck
 First created:
 * Date: 15/05/13
 * Time: 16:00
 */
import HWCounters.{Westmere, JakeTown, Counter}
import org.scalatest.Suite
import perfplot.{CodeGeneration, CommandService, Config}
import java.io._
import scala.io._

class Example extends Suite{

  val flags = " -O2 -fno-tree-vectorize " //!these are for GCC!
  //val flags = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + " /O2 " + Config.flag_novec //this i use for icc
  //this flags are passed all the way down to the compiler.
  //The final call looks like this (inside a temporary directory).
  //execute("icc " + codeFile +".cpp " + kernelFileName + " " + Config.MeasuringCore.getAbsolutePath + flags + "  -lpthread -lrt -o "+ codeFile + ".x")

  //so if you'd like you code to be compiled to be added in this step either link against it or just compile it together


  Config.use_gcc = true
  //for this example we set it to gcc - still recommend icc - but just to make sure

  Config.use_cache = false
  //this would usually check that if the folder contains already xxx_flops.txt - and do nothing if it does
  //(to avoid running an experiment over an over that is already done - in case another fails)


  //This folder is not created - so make sure it exists in case you change this path!!!!
  val folder = new File (Config.result_folder + File.separator + "example" + File.separator)

  val counters = JakeTown //This works for Sandybridge and Ivy Bridge
    //for Nehalem and Westmere use
  //val counters = Westmere



  val sizes : List[Int] = List(16,32,64,128,256,512,1024,2048)
  val Accumulators : List[Int] = List(1,2,3,4,5,6,7,8)
  def test_Accumulators =
  {
    for (acc <- Accumulators)
    {
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.Example(true,true,acc,s*1024,false).setFlopCounter(counters.flops_double), "Accumulators-double-warm"+acc, flags)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.Example(true,false,acc,s*1024,false).setFlopCounter(counters.flops_double), "Accumulators-double-cold"+acc, flags)

      //Note that by default we measure double prec. -> to switch to single you need to write code like below!
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.Example(false,true,acc,s*1024,false).setFlopCounter(counters.flops_single), "Accumulators-single-warm"+acc, flags)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.Example(false,false,acc,s*1024,false).setFlopCounter(counters.flops_single), "Accumulators-single-cold"+acc, flags)
    }


    println("-------------------------------------------------------------------")
    println("your results are now collected in " + folder.toString)
  }

}
