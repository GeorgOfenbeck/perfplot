
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


class FFTOverview extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_optimization
  val folder = new File (Config.result_folder + File.separator + "ffts" + File.separator)

  val counters = JakeTown

  def test_FFT_FFTW() =
  {
    val sizes =  (for (i<-2 until 14) yield (Math.pow(2,i).toLong)).toList
    val fftw_flags = " -lfftw3 -lm "

    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_FFTW(true,true,s,false).setFlopCounter(counters.flops_double), "fftw-warm", seq + fftw_flags)
    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_FFTW(true,false,s,false).setFlopCounter(counters.flops_double), "fftw-cold", seq + fftw_flags)

  }

  def test_FFT_MKL() =
  {
    val sizes =  (for (i<-2 until 14) yield (Math.pow(2,i).toLong)).toList


    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,true,s,false).setFlopCounter(counters.flops_double), "mkl-warm", seq)
    CommandService.run_kernel(folder,for (s <- sizes) yield CodeGeneration.fft_MKL(true,false,s,false).setFlopCounter(counters.flops_double), "mkl-cold", seq)

  }

}







