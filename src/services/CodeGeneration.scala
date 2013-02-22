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


  def memonly1 (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      p("long runs = 1;")
      p("int result = 0;")
      //allocate
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("int * A = (int *) _mm_malloc("+size+"*sizeof(int),page);")
      if (!warmData)
      {

        p("for(int i = 0; i < size; i++){")
        p("result += A[i];")
        p("}")

        p("result = 0;")
      }
      p("measurement_start();")
      p("for(int i = 0; i < size; i++){")
      p("result += A[i];")
      p("}")
      p("measurement_stop(runs);")
      p("_mm_free(A);")
      p("}")
      p("}")
  }
    p("measurement_end();")
    p("}")
  }

  def memonly2 (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      p("long runs = 1;")
      p("int result = 0;")
      //allocate
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("int * A = (int *) _mm_malloc("+size+"*sizeof(int),page);")
      if (!warmData)
      {
        p("for(long i = 0; i < size; i++){")
        p("A[i] = 9; ")
        p("}")
      }
      p("measurement_start();")
      p("for(long i = 0; i < size; i++){")
      p("A[i] = 10; ")
      p("}")
      p("measurement_stop(runs);")
      p("_mm_free(A);")
      p("}")
      p("}")
    }
    p("measurement_end();")
    p("}")
  }

  def memonly3 (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("long runs = 1;")
      p("unsigned long size = " +size + ";")
      p("int result = 0;")
      //allocate
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("int * A = (int *) _mm_malloc("+size+"*sizeof(int),page);")
      if (!warmData)
      {

        p("for(long i = 0; i < size; i++){")
        p("A[i] = A[ (size-1) - i]; ")
        p("}")

      }
      p("measurement_start();")
      p("for(long i = 0; i < size; i++){")
      p("A[i] = A[ (size-1) - i]; ")
      p("}")
      p("measurement_stop(runs);")
      p("_mm_free(A);")

      p("}")
      p("}")
    }
    p("measurement_end();")
    p("}")
  }

  def memonly4 (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      p("int result = 0;")
      p("long runs = 1;")
      //allocate
      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("int * A = (int *) _mm_malloc("+size+"*sizeof(int),page);")

      if (!warmData)
      {

        p("for(long i = 0; i < size; i=i+300){")
        p("A[i] = A[ (size-1) - i]; ")
        p("}")

      }

      p("measurement_start();")
      p("for(long i = 0; i < size; i=i+300){")
      p("A[i] = A[ (size-1) - i]; ")
      p("}")
      p("measurement_stop(runs);")
      p("_mm_free(A);")

      p("}")
      p("}")
    }
    p("measurement_end();")
    p("}")
  }


  def dgemm_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      //allocate
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * B = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * C = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("int n = " +size + ";")
      //Tune the number of runs
      p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1,A, size,B, size,1,C, size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
        //allocate
        p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** B_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** C_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1,A_array[i%numberofshifts], size,B_array[i%numberofshifts], size,1,C_array[i%numberofshifts], size);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) A_array, numberofshifts);")
        p("DestroyBuffers( (void **) B_array, numberofshifts);")
        p("DestroyBuffers( (void **) C_array, numberofshifts);")
      }
      else
      {
        //run it
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1,A, size,B, size,1,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }



  def daxpy_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")

      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("int n = " +size + ";")
      //Tune the number of runs
      p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"cblas_daxpy("+size+", alpha, x, 1, y, 1);","" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {

        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        p("long numberofshifts =  measurement_getNumberOfShifts(" + (size+size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_daxpy("+size+", alpha, x_array[i%numberofshifts], 1, y_array[i%numberofshifts], 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
      }
      else
      {
        //run it
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_daxpy("+size+", alpha, x, 1, y, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(x);")
        p("_mm_free(y);")
      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }



  def dgemv_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")

      //allocate
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("int n = " +size + ";")
      //Tune the number of runs
      p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(A);")
        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size+size+size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A_array[i%numberofshifts], "+size+", x_array[i%numberofshifts], 1, 0., y_array[i%numberofshifts], 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) A_array, numberofshifts);")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
      }
      else
      {
        //run it
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(A);")
        p("_mm_free(x);")
        p("_mm_free(y);")
      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }

  def fft_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <mkl.h>")
    p("#include <iostream>")
    p(Config.MeasuringCoreH)
    p("#define page 4096")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("int main () { ")
    p(counterstring)
    p(initstring)

    for (size <- sizes)
    {
      p("{")
      p("DFTI_DESCRIPTOR_HANDLE mklDescriptor;")
      p("MKL_LONG status;")
      val dfti_prec = if (double_precision) "DFTI_DOUBLE" else "DFTI_SINGLE"
      p("status = DftiCreateDescriptor( &mklDescriptor, " + dfti_prec+ ",DFTI_COMPLEX, 1,"+ size + ");")
      p("if (status != 0) {\n    return -1;\n\t}\n\n\tstatus = DftiCommitDescriptor(mklDescriptor);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}")


      //Tune the number of runs
      p(prec + " * x = (" + prec + "*) _mm_malloc(" + 2*size + "* sizeof(" + prec + "),page);" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"status = DftiComputeForward(mklDescriptor, x);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}", "std::cout << x[0];" )
      //find out the number of shifts required
      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



      if (!warmData)
      {
        p("_mm_free(x);")
        //allocate
        p("long numberofshifts =  measurement_getNumberOfShifts(" + (2*size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("double ** x_array = (double **) CreateBuffers("+2*size+"* sizeof(" + prec + "),numberofshifts);")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
      }
      else
      {
        //run it
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("status = DftiComputeForward(mklDescriptor, x);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(x);")

      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }

  def run_kernel (kernel: (PrintStream, List[Long],  Array[HWCounters.Counter],  Boolean,  Boolean ) => Unit,
                  sizes_in: List[Long],name: String, counters: Array[HWCounters.Counter],double_precision: Boolean = true, warmData: Boolean = false, flags: String) =
  {

    val file = new File("flop_"+ name + ".txt")
    if(!file.exists())
    {
      val outputFile1 = new PrintStream("flop_"+ name + ".txt")
      val outputFile2 = new PrintStream("tsc_"+ name + ".txt")
      val outputFile3 = new PrintStream("size_"+ name + ".txt")
      val outputFile4 = new PrintStream("bytes_transferred_" + name + ".txt")
      val outputFile5 = new PrintStream("Counter3_" + name + ".txt")
      val outputFile6 = new PrintStream("bytes_read_" + name + ".txt")
      val outputFile7 = new PrintStream("bytes_write_" + name + ".txt")
      var first1 = true
      for (s <- sizes_in)
      {
        //this way we do a single measurment setup for each size
        val sizes: List[Long] = List(s)
        def single_kernel(sourcefile: PrintStream) = kernel (sourcefile: PrintStream,sizes, counters, double_precision, warmData)
        val kernel_res = CommandService.fromScratch(name, single_kernel, flags)
        //kernel_res.prettyprint()
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
          outputFile3.print(s)
          first1 = false
        }
        else
          outputFile3.print(" " + s)
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
