/** 
 * Georg Ofenbeck
 First created:
 * Date: 05/02/13
 * Time: 15:27 
 */


import HWCounters.JakeTown
import org.scalatest.Suite

import perfplot._
import perfplot.plot._
import perfplot.quantities._
import perfplot.services._


import java.io._
import scala.io._


class TestMemory  extends Suite{





  def fft (cmdbat: PrintStream, size: Int,counter: HWCounters.Counter) =
  {
    cmdbat.println(Config.MeasuringCoreH)
    cmdbat.println("  \n\n#include \"mkl_dfti.h\" \n#include <mkl.h>\n#include <iostream>\n#include \"immintrin.h\" \n " )
    cmdbat.println("#include <immintrin.h>\n\n\n")

    cmdbat.println("double * a, *b, *c;\ndouble alpha;\n\n\n\nint main () {\n\n  ");


    cmdbat.println("long counters[8];")
    //unhalted core
    cmdbat.println("counters[0] = 0x3C;")
    cmdbat.println("counters[1] = 0x00;")

    //unhalted ref
    cmdbat.println("counters[2] = 0x3C;")
    cmdbat.println("counters[3] = 0x01;")

    //
    cmdbat.println("counters[4] = 0x2E;")
    cmdbat.println("counters[5] = 0x41;")

    cmdbat.println("counters[6] = " + counter.getEventNr + ";")
    cmdbat.println("counters[7] = " + counter.getUmask + ";")

    if (counter.getEventNr == 183) //Offcore response
      cmdbat.println("perfmon_init(counters,"+counter.getComment+",0);")
    else
      cmdbat.println("perfmon_init(counters,0,0);")

    cmdbat.println("\n\tdouble  *complexData;\n\tDFTI_DESCRIPTOR_HANDLE mklDescriptor;\n  const int page = 1024*4;\n  int mem = 256*"+size+" ; //128 MB\n  \n  \n  \n")
    //cmdbat.println("for (long vectorSize = 500; vectorSize <= 10000 * 1000; vectorSize *= 2)")
    cmdbat.println("int vectorSize = mem/2/sizeof(double); ")
    //cmdbat.println("long vectorSize = 500;")
    cmdbat.println("\n  {  MKL_LONG status;\n\n\tstatus = DftiCreateDescriptor( &mklDescriptor, DFTI_DOUBLE,\n\t\t\tDFTI_REAL, 1, vectorSize);\n\tif (status != 0) {\n    return -1;\n\t}\n\n\tstatus = DftiCommitDescriptor(mklDescriptor);\n\tif (status != 0) {\n\t\treturn -1;\n\t}")
    cmdbat.println("double alpha = 1.1; double beta = 1.2;  \n    complexData = (double *)  _mm_malloc( 2*vectorSize*sizeof(double), page );\n    if (!complexData) {\n      std::cout << \"malloc failed\";\n      perfmon_end();\n      return -1;\n    }\n    ");
    //cmdbat.println("double result = 0;perfmon_start();    \n\n    for(long i = 0; i< vectorSize; i=i+1)    \n      result += x[i]; perfmon_stop();\n\n\t\t\tstd::cout << result;")
    cmdbat.println("perfmon_start();\n     \n\t")
    cmdbat.println("status = DftiComputeForward(mklDescriptor, complexData);\n\tif (status != 0) {\n\t\treturn -1;\n\t}  \n     perfmon_stop();\n\n ")
    cmdbat.println(" }\n   _mm_free(complexData);       \n  \n  perfmon_end();\n  \n  \n}")
    cmdbat.close()
  }



    def memory(sourcefile: PrintStream, size: Int, read: Int, write: Int, counter: HWCounters.Counter) =
  {
    sourcefile.println(Config.MeasuringCoreH)
    sourcefile.println("#include <iostream>")
    sourcefile.println("#include <cstdio>")
    sourcefile.println("#include <stdlib.h>")
    sourcefile.println("  double * buffer;")
    sourcefile.println("int main () {\n    ")
    sourcefile.println("long counters[8];")

    //unhalted core
    sourcefile.println("counters[0] = 0x3C;")
    sourcefile.println("counters[1] = 0x00;")

    //unhalted ref
    sourcefile.println("counters[2] = 0x3C;")
    sourcefile.println("counters[3] = 0x01;")

    //
    sourcefile.println("counters[4] = 0x2E;")
    sourcefile.println("counters[5] = 0x41;")

    sourcefile.println("counters[6] = " + counter.getEventNr + ";")
    sourcefile.println("counters[7] = " + counter.getUmask + ";")

    if (counter.getEventNr == 183) //Offcore response
      sourcefile.println("perfmon_init(counters,"+counter.getComment+",0);")
    else
      sourcefile.println("perfmon_init(counters,0,0);")
    sourcefile.println("const int page = 1024*4;")
    sourcefile.println("const int mem = 256*"+size+ "; //128 MB")
    sourcefile.println("  buffer = (double*)  _mm_malloc( mem*page, page );\n  if (!buffer) {\n    std::cout << \"malloc failed\";\n    perfmon_end();\n    return -1;\n  }")
    sourcefile.println("double result = 0;")


    sourcefile.println("perfmon_start(); perfmon_stop();  ")
    //sourcefile.println("flushCache();")


    sourcefile.println("long size = mem*page/sizeof(double);")
    //Write traffic
    sourcefile.println("perfmon_start();   ")
    for (i<- 0 until write)
    {
      sourcefile.println("for(long i = 0; i< size; i=i+1)    \n      buffer[i] = i%2; ")
      //sourcefile.println("flushCache();")
    }
    sourcefile.println("perfmon_stop();   ")



    //Read only traffic
    sourcefile.println("perfmon_start();   ")
    for (i <- 0 until read)
    {
      sourcefile.println("for(long i = 0; i< size; i=i+1)    \n      result += buffer[i]; ")
    }
    sourcefile.println("perfmon_stop();   ")

    //Read / Write
    sourcefile.println("perfmon_start();   ")

    for (i <- 0 until read)
    {
      sourcefile.println("for(long i = 0; i< size-10; i=i+1)    \n      buffer[size-i-1] = buffer[i]; ")
    }
    sourcefile.println("perfmon_stop();   ")


    //Multiple Reads from Cache
    sourcefile.println("perfmon_start();   ")

    for (i <- 0 until read)
    {
      sourcefile.println("for(long i = 0; i< size; i=i+1) {")
      sourcefile.println("for(long j = i; j < size && j < 50; j= j+ 1) result += buffer[j]; }")
    }
    sourcefile.println("perfmon_stop();   ")


    //Multiple Writes
    sourcefile.println("perfmon_start();   ")

    for (i <- 0 until read)
    {
      sourcefile.println("for(long i = 0; i< size-10; i=i+1)    \n{ ")
      sourcefile.println("for (long j = i; j< size && j < 50; j = j+1) buffer[size-i-1-j] = buffer[i+j]; }")
    }
    sourcefile.println("perfmon_stop();   ")


    sourcefile.println("\tstd::cout << result;\n  std::cout << \"\\n\" << (page*mem/sizeof(double)) << \"\\n\";")
    sourcefile.println("perfmon_end();")
    sourcefile.println("  _mm_free (buffer);")





    sourcefile.println("}")
  }





  def test() =
  {
    val outfile = new PrintStream("data.txt")
    val outfile1 = new PrintStream("datar.txt")
    val outfile2 = new PrintStream("dataw.txt")
    val outfile3 = new PrintStream("datac.txt")


    for (counter <- JakeTown.counters.reverse)
    {
      for (size <- List(32,64,128,196))
      {
        {
          val ffttest1 =  (sourcefile: PrintStream) => fft(sourcefile,size,counter)
          val res1 = CommandService.fromScratch("allcounters", ffttest1,  " -O0 -mkl" )
          val x = res1.getSCounter3()
          val r = res1.getRead()
          val w = res1.getWrite()
          for (v <- x)
            outfile.println(v)
          for (v <- r)
            outfile1.println(v)
          for (v <- w)
          {
            outfile2.println(v)
            outfile3.println(counter.name)
          }
        }


        {
        val memtest1 =  (sourcefile: PrintStream) => memory(sourcefile,size,10,10,counter)
        val res1 = CommandService.fromScratch("allcounters", memtest1,  " -O0 " )
        val x = res1.getSCounter3()
        val r = res1.getRead()
        val w = res1.getWrite()
        for (v <- x)
          outfile.println(v)
        for (v <- r)
          outfile1.println(v)
        for (v <- w)
          {
            outfile2.println(v)
            outfile3.println(counter.name)
          }

        }

      }
      //yield res1.getSCounter3()
    }



    outfile.close()
    outfile1.close();
    outfile2.close();

  }

}
