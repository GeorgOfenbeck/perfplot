
/**
 * Georg Ofenbeck
 First created:
 * Date: 15/01/13
 * Time: 10:12 
 */

import HWCounters.Counter
import org.scalatest.Suite

import perfplot.{CodeGeneration, CommandService, Config}





import java.io._
import scala.io._


class BLASOverview extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_optimization
  val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_mkl + Config.flag_optimization
  val folder = new File (Config.result_folder + File.separator + "overview" + File.separator)


  /*
  def test_dgemm_seq() =
  {
    val sizes =  (for (i<-0 until 10) yield (i*300+100).toLong ).toList
    
    
      
    
    
    
    
    )
  }
  */

  def test_daxpy_seq() =
  {
    val sizes =  (for (i<-0 until 10) yield (i.toLong*i*30000+10000).toLong ).toList

    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,true,s), "daxpy-warm", seq)
    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,false,s), "daxpy-cold", seq)
    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,true,s), "daxpy-parallel-warm", parallel)
    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.daxpy_MKL(true,false,s), "daxpy-parallel-cold", parallel)
  }

  /*
   def test_FFT_MKL_seq() =
   {
     val sizes_2power =  (for (i<-5 until 23) yield (Math.pow(2,i).toLong)).toList
     CodeGeneration.run_kernel(folder,CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-warm",counters,true,true, seq)
     CodeGeneration.run_kernel(folder,CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-cold",counters,true,false, seq)
     CodeGeneration.run_kernel(folder,CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-parallel-warm",counters,true,true, parallel)
     CodeGeneration.run_kernel(folder,CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-parallel-cold",counters,true,false, parallel)
   }
 
 
   def test_dgmev_seq() =
   {
     val sizes_2power =  (for (i<-0 until 10) yield (i*300+100).toLong ).toList
     CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-warm",counters,true,true, seq)
     CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-cold",counters,true,false, seq)
     CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-parallel-warm",counters,true,true, parallel)
     CodeGeneration.run_kernel(folder,CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-parallel-cold",counters,true,false, parallel)
 
   }
   */
}







