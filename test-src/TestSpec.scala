/**
 * Created with IntelliJ IDEA.
 * User: ofgeorg
 * Date: 3/6/13
 * Time: 3:33 PM
 * To change this template use File | Settings | File Templates.
 */



import HWCounters.Counter
import org.scalatest.Suite

import perfplot.Config
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._

import perfplot.services.CommandService._


import java.io._
import scala.io._


class TestSpec extends Suite{
  val flags = " "

  val folder = new File (Config.result_folder + File.separator + "spec" + File.separator)

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("B7H","01H","Custom_all","",     "0x3F80400FFF")
  )

/*
  def test_spec_433 =
  {

    run_spec("433",folder, new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/433.milc/run.sh") )
  }*/

  def test_spec_444 =
  {
    run_spec("444",folder,new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/444.namd/run.sh"))
  }

  def test_spec_450 =
  {
    run_spec("450",folder,new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/450.soplex/run.sh"))
  }

  def test_spec_470 =
  {
    run_spec("470",folder,new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/470.lbm/run.sh"))

  }

  def test_spec_482 =
  {
    run_spec("482",folder,new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/482.sphinx3/run.sh"))

  }

  def test_spec_999 =
  {
    run_spec("999",folder,new File("/Users/Victoria/ETH-VICTORIA/BENCHMARKS/SPEC2006/999.specrand/run.sh"))
  }


  def prep_spec (exefile: File): Counters =
  {

    //val tempdir = CommandService.getTempDir(filename)

    //CommandService.compile(tempdir.getPath + File.separator +  filename, flags)

    System.out.println("executing ...")
    CommandService.execute(exefile.toString, exefile.getParentFile)
    System.out.println("gather results ..." + exefile.getPath)
    val c = Counters.apply(exefile.getParentFile)
    System.out.println("return results ...")
    c
  }




  def run_spec (name: String, path: File, exe: File,  small : Boolean = true) =
  {

    val s = if (small) 0 else 1
    val file = new File(path.getPath + File.separator +"flop_"+ name + ".txt")
    if(!file.exists())
    {
      val outputFile1 = new PrintStream(path.getPath + File.separator +"flop_"+ name + ".txt")
      val outputFile2 = new PrintStream(path.getPath + File.separator +"tsc_"+ name + ".txt")
      val outputFile3 = new PrintStream(path.getPath + File.separator +"size_"+ name + ".txt")
      val outputFile4 = new PrintStream(path.getPath + File.separator +"bytes_transferred_" + name + ".txt")
      val outputFile5 = new PrintStream(path.getPath + File.separator +"Counter3_" + name + ".txt")
      val outputFile6 = new PrintStream(path.getPath + File.separator +"bytes_read_" + name + ".txt")
      val outputFile7 = new PrintStream(path.getPath + File.separator +"bytes_write_" + name + ".txt")
      var first1 = true


      val tmpfile = new PrintStream(".~/.LARGE_INPUT")
      tmpfile.print("0")
      tmpfile.close()

      for (i <- 0 until 2)
      {
        //this way we do a single measurment setup for each size

        val  kernel_res = prep_spec(exe)
        //val kernel_res = CommandService.fromScratch(name, single_kernel, flags)
        kernel_res.prettyprint()

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
          outputFile1.print(kernel_res.getFlops(i))
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
          outputFile3.print(i)
          first1 = false
        }
        else
          outputFile3.print(" " + i)

        val tmpfile2 = new PrintStream(".~/.LARGE_INPUT")
        tmpfile2.print("1")
        tmpfile2.close()
      }
      //dgemv_res.prettyprint()
      outputFile1.close()
      outputFile2.close()
      outputFile3.close()
      outputFile4.close()
      outputFile5.close()
      outputFile6.close()
      outputFile7.close()
    }
    else
      println(name + " read from cached file")
  }



}
