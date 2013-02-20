package perfplot
package services

import perfplot.Config._
import java.io._

/**
 * Georg Ofenbeck
 First created:
 * Date: 19/02/13
 * Time: 11:15 
 */
object CodeGeneration {

  def Counters2CCode(counters: Array[HWCounters.Counter]): (String,String) =
  {
    var offcore_0: String = "0"
    var offcore_1: String = "0"

    var counter_string = "long counters["+counters.size*2+"];\n"
    for (i <- 0 until counters.size)
    {
      counter_string = counter_string + "counters["+ i*2 +"] = " + counters(i).getEventNr + ";\n"
      counter_string = counter_string + "counters["+ (i*2 + 1) +"] = " + counters(i).getUmask + ";\n"
      if (counters(i).getEventNr == 183) //Offcore response
        if (offcore_0 == "0")
          offcore_0 = counters(i).Comment
        else
        if (offcore_1 == "0")
          offcore_1 = counters(i).Comment
        else
          assert(false, "Trying to program more then 2 offcore response events")
    }

    (counter_string,"measurement_init(counters,"+offcore_0+","+offcore_1+");")
  }



  def tuneNrRunsbyRunTime(sourcefile : PrintStream, kernel: String, printsomething: String) =
  {
    def p(x: String) = sourcefile.println(x)

    p("unsigned long runs = 1; //start of with a single run for sample")
    p("unsigned long multiplier;")
    p("measurement_start();")
    p("measurement_stop(runs);")
    p("measurement_emptyLists(true); //don't clear the vector of runs")
    p("do{")
    p("measurement_start();")
    p("for(unsigned long i = 0; i <= runs; i++)")
    p("{")
    p(kernel)
    p("}")
    p("measurement_stop(runs);")
    p(printsomething)
    p("multiplier = measurement_run_multiplier("+measurement_Threshold+");")
    p("runs = runs * multiplier;")
    //p("std::cout << runs << \" runs\";")
    //p("std::cout << multiplier<< \" multiplier\";")
    p("}while (multiplier > 2);")
    //p("std::cout << runs << \" runs\";")
  }



  def tuneNrRuns(sourcefile : PrintStream, kernel: String, printsomething: String) =
  {
    def p(x: String) = sourcefile.println(x)
    p("long runs = 1;")
    p("for(; runs <= (1 << 20); runs *= 2){")
    p("measurement_start();")
    p("for(int i = 0; i <= runs; i++)")
    p(kernel)
    p("measurement_stop(runs);")
    p(printsomething) //this is just to avoid deadcode eliminiation
    p("if(measurement_testDerivative(runs, " + Config.testDerivate_Threshold + "))")
    p("break;")
    p("measurement_emptyLists(true);} //don't clear the vector of runs")
    p("measurement_emptyLists();") //duplicated cause of the break
  }


  def create_array_of_buffers (sourcefile: PrintStream) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec =  "double"
    p("void * CreateBuffers(long size, long numberofshifts)")
    p("{")
    p( prec + " ** bench_buffer = (" + prec + "**) _mm_malloc(numberofshifts*sizeof(" + prec + "*),page);" )
    p( "if (!bench_buffer) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return ;} ")
    p("for(int i = 0; i < numberofshifts; i++){")
    p("bench_buffer[i] = (" + prec + "*) _mm_malloc(size,page);" )
    p( "if (!bench_buffer[i]) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return ;} ")
    p("}")
    p("return (void*)bench_buffer;")
    p("}")
  }

  def destroy_array_of_buffers( sourcefile: PrintStream) =
  {
    def p(x: String) = sourcefile.println(x)
    p("void DestroyBuffers(void ** bench_buffer, long numberofshifts) {")
    p("for(int i = 0; i < numberofshifts; i++)")
    p("_mm_free(bench_buffer[i]);")
    p("_mm_free(bench_buffer);")
    p("}")
  }





}
