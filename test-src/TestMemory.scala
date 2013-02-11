/** 
 * Georg Ofenbeck
 First created:
 * Date: 05/02/13
 * Time: 15:27 
 */


import HWCounters.JakeTown
import org.scalatest.Suite

import roofline._
import roofline.plot._
import roofline.quantities._
import roofline.services._


import java.io._
import scala.io._


class TestMemory  extends Suite{



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
      sourcefile.println("perfmon_init(counters,"+counter.CommenttoLong+",0);")
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

    for (counter <- JakeTown.counters)
    {
      val memtest1 =  (sourcefile: PrintStream) => memory(sourcefile,128,1,1,counter)
      val res1 = CommandService.fromScratch("allcounters", memtest1,  " -O0 " )
      res1.prettyprint()
      //yield res1.getSCounter3()
    }

  }

}
