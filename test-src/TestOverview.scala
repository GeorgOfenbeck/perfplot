
/**
 * Georg Ofenbeck
 First created:
 * Date: 15/01/13
 * Time: 10:12 
 */

import HWCounters.Counter
import org.scalatest.Suite

import perfplot.Config
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._




import java.io._
import scala.io._


class TestOverview extends Suite{

  val seq = Config.flag_c99 + Config.flag_hw + Config.flag_mkl_seq + Config.flag_no_optimization
  val parallel = Config.flag_c99 + Config.flag_hw + Config.flag_mkl + Config.flag_no_optimization

  val counters = Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
  )

  def test_dgemm_seq() =
  {
    val sizes =  (for (i<-1 until 8) yield (i*128).toLong ).toList
    CodeGeneration.run_kernel(CodeGeneration.dgemm_MKL,sizes,"dgemm-warm",counters,true,true, seq)
    CodeGeneration.run_kernel(CodeGeneration.dgemm_MKL,sizes,"dgemm-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(CodeGeneration.dgemm_MKL,sizes,"dgemm-parallel-warm",counters,true,true, parallel)
    CodeGeneration.run_kernel(CodeGeneration.dgemm_MKL,sizes,"dgemm-parallel-cold",counters,true,false, parallel)
  }


  def test_daxpy_seq() =
  {
    val sizes_2power =  (for (i<-5 until 20) yield Math.pow(2,i).toLong).toList
    CodeGeneration.run_kernel(CodeGeneration.daxpy_MKL,sizes_2power,"daxpy-warm",counters,true,true, seq)
    CodeGeneration.run_kernel(CodeGeneration.daxpy_MKL,sizes_2power,"daxpy-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(CodeGeneration.daxpy_MKL,sizes_2power,"daxpy-parallel-warm",counters,true,true, parallel)
    CodeGeneration.run_kernel(CodeGeneration.daxpy_MKL,sizes_2power,"daxpy-parallel-cold",counters,true,false, parallel)
  }


  def test_FFT_MKL_seq() =
  {
    val sizes_2power =  (for (i<-5 until 15) yield Math.pow(2,i).toLong).toList
    CodeGeneration.run_kernel(CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-warm",counters,true,true, seq)
    CodeGeneration.run_kernel(CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-parallel-warm",counters,true,true, parallel)
    CodeGeneration.run_kernel(CodeGeneration.fft_MKL,sizes_2power,"fft-MKL-parallel-cold",counters,true,false, parallel)
  }


  def test_dgmev_seq() =
  {
    val sizes_2power =  (for (i<-2 until 12) yield Math.pow(2,i).toLong).toList
    CodeGeneration.run_kernel(CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-warm",counters,true,true, seq)
    CodeGeneration.run_kernel(CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-cold",counters,true,false, seq)
    CodeGeneration.run_kernel(CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-parallel-warm",counters,true,true, parallel)
    CodeGeneration.run_kernel(CodeGeneration.dgemv_MKL,sizes_2power,"dgemv-parallel-cold",counters,true,false, parallel)

  }
}

