
/**
 * Georg Ofenbeck
 First created:
 * Date: 15/01/13
 * Time: 10:12 
 */

import HWCounters.{JakeTown, Counter}
import org.scalatest.Suite

import perfplot.{CodeGeneration, CommandService, Config}





import java.io._
import scala.io._


class BLASOverview extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_optimization
  val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_mkl + Config.flag_optimization
  val folder = new File (Config.result_folder + File.separator + "overview" + File.separator)

  val counters = JakeTown

  def test_axpy() =
  {
    val sizes =  (for (i<-0 until 10) yield (i.toLong*i*30000+10000).toLong ).toList


      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,true,s).setFlopCounter(counters.flops_double), "daxpy-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,false,s).setFlopCounter(counters.flops_double), "daxpy-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,true,s).setFlopCounter(counters.flops_double), "daxpy-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,false,s).setFlopCounter(counters.flops_double), "daxpy-parallel-cold", parallel)



      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(false,true,s).setFlopCounter(counters.flops_single), "saxpy-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(false,false,s).setFlopCounter(counters.flops_single), "saxpy-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(false,true,s).setFlopCounter(counters.flops_single), "saxpy-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(false,false,s).setFlopCounter(counters.flops_single), "saxpy-parallel-cold", parallel)

  }

  def test_gmev() =
  {
    val sizes =  (for (i<-0 until 10) yield (i*300+100).toLong ).toList


      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(true,true,s).setFlopCounter(counters.flops_double), "dgemv-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(true,false,s).setFlopCounter(counters.flops_double), "dgemv-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(true,true,s).setFlopCounter(counters.flops_double), "dgemv-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(true,false,s).setFlopCounter(counters.flops_double), "dgemv-parallel-cold", parallel)



      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(false,true,s).setFlopCounter(counters.flops_single), "sgemv-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(false,false,s).setFlopCounter(counters.flops_single), "sgemv-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(false,true,s).setFlopCounter(counters.flops_single), "sgemv-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemv_MKL(false,false,s).setFlopCounter(counters.flops_single), "sgemv-parallel-cold", parallel)

  }

  def test_gmem() =
  {
    val sizes =  (for (i<-0 until 10) yield (i*300+100).toLong ).toList


      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(true,true,s).setFlopCounter(counters.flops_double), "dgemm-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(true,false,s).setFlopCounter(counters.flops_double), "dgemm-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(true,true,s).setFlopCounter(counters.flops_double), "dgemm-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(true,false,s).setFlopCounter(counters.flops_double), "dgemm-parallel-cold", parallel)



      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(false,true,s).setFlopCounter(counters.flops_single), "sgemm-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(false,false,s).setFlopCounter(counters.flops_single), "sgemm-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(false,true,s).setFlopCounter(counters.flops_single), "sgemm-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.dgemm_MKL(false,false,s).setFlopCounter(counters.flops_single), "sgemm-parallel-cold", parallel)

  }

  def test_FFT_MKL() =
  {
    val sizes =  (for (i<-5 until 23) yield (Math.pow(2,i).toLong)).toList


      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,true,s,true).setFlopCounter(counters.flops_double), "fft-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,false,s,true).setFlopCounter(counters.flops_double), "fft-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,true,s,true).setFlopCounter(counters.flops_double), "fft-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,false,s,true).setFlopCounter(counters.flops_double), "fft-parallel-cold", parallel)



      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(false,true,s,true).setFlopCounter(counters.flops_single),  "sfft-warm", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(false,false,s,true).setFlopCounter(counters.flops_single), "sfft-cold", seq)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(false,true,s,true).setFlopCounter(counters.flops_single),  "sfft-parallel-warm", parallel)
      CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(false,false,s,true).setFlopCounter(counters.flops_single), "sfft-parallel-cold", parallel)

  }

}







