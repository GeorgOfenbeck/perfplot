package perfplot



import perfplot.Config._
import java.io._
import HWCounters.Counter
/**
 * Georg Ofenbeck
 First created:
 * Date: 19/02/13
 * Time: 11:15 
 */

case class CodeGeneration{

  var id: String = ""//used for cache lookup
  var includes : String = ""//any additional includes needed

  var initcode : String = ""//any code needed to set up the kernel

  var init_function : String = "" //the function used to initalize the data
  var init_call: String = ""//the call to this function

  //this has to be a single buffer for warm cache measurments, or a array of buffers for cold
  var alignment: Int = Config.def_alignment//alignment used in the creation of the buffers
  var size: Int = 0//size * datatype = size of one buffer
  var total_size: Int = 0//size of all buffers per iteration
  var datatype: String = ""


  var create_buffer_function: String = ""
  var destroy_buffer_function: String = ""
  var create_buffer_call: String = ""
  var destroy_buffer_call: String = ""


  var kernel_header: String = ""
  var kernel_call: String = ""
  var kernel_code: String = ""

  var inline : Boolean = false //if the code should be inlined (for small sizes), or if it should be compiled seperate
  var avoid_DCE: String = ""//optional code that makes sure that deadcode elimination doesnt happen (not relevant for funciton call)

  var repeats : Int = Config.repeats
  var threshold : Long = Config.measurement_Threshold

  var (counters,mask) = HWCounters.JakeTown.flops_double


  var determineSize_call: String = ""
  var nrRuns: String = ""//should include warm up in case of warm measurement


  //in case we dont inline and dont have a library call
  def printcode(sourcefile: PrintStream) = sourcefile.println(kernel_code)

  def print(sourcefile: PrintStream) =
  {
    def p(x: String) = sourcefile.println(x)
    val (counterstring, initstring ) = Counters2CCode()

    //the code printing
    p(Config.MeasuringCoreH) //Header required for pcm
    p(includes)// headers required for kernel

    p("#define ALIGNMENT " + alignment)

    p(create_buffer_function)
    p(destroy_buffer_function)
    p(init_function)
    p(kernel_header)

    p("int main (){")
    p("unsigned long runs = 1; //start of with a single run for sample")
    p(counterstring) //creates array of counters to be used
    p(initstring)    //initalizes the measuring core
    p(initcode)      //whatever is needed to initalize the kernel


    p(determineSize_call)
    p(create_buffer_call)
    p(init_call)

    p(nrRuns)
    p("for (int r = 0; r < "+ repeats + "; r++){")
    p("measurement_start();")
    p("for(int i = 0; i < runs; i++){")
    //p("cblas_daxpy("+size+", alpha, x, 1, y, 1);")
    if (inline)
      p(kernel_code)
    else
      p(kernel_call)
    p("}")
    p( "measurement_stop(runs);")
    p(avoid_DCE)

    p( " }")
    p(destroy_buffer_call)
    p("measurement_end();")
    p("}")
  }


  def ini11 () : String =
  {
    "void _ini1(" + datatype + " * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (" + datatype + ")1.1;\n}"
  }

  def tuneNrRunsbySize() :String =
  {
    "unsigned long LLCSize =  getLLCSize(); \n" +
    "unsigned associativity = 8; //this is hardcoded as its the same on all platforms considered \n" +
    "unsigned long size_per_run = " + total_size + "* sizeof("+datatype+"); \n" +
    "runs = (LLCSize * associativity)/size_per_run;\n" +
    "if (runs < 2) runs = 2; //to make sure stuff is not cache resident\n" + 
    "long numberofshifts = runs;"
  }




  def tuneNrRunsbyRunTime() : String =
  {
    "unsigned long multiplier= 1; \n" +
    "measurement_start(); //this is done to make sure that the warmup of the toolchain is not accounted\n" +
    "measurement_stop(runs);\n" +
    "measurement_emptyLists(true); //don't clear the vector of runs\n" +
    "do{\n" +
    "runs = runs * multiplier;\n" +
    "measurement_start();\n" +
    "for(unsigned long i = 0; i <= runs; i++)\n" +
    "{" + kernel_call + "}" +
    "measurement_stop(runs);\n" +
    avoid_DCE +
    "multiplier = measurement_run_multiplier("+threshold+");" +
    "}while (multiplier > 2);\n" +
    "measurement_emptyLists(true); //don't clear the vector of runs\n"
  }


  def create_array_of_buffers () : String =
  {
    "void * CreateBuffers(long size, long numberofshifts)\n" +
    "{" +
    datatype + " ** bench_buffer = (" + datatype + "**) _mm_malloc(numberofshifts*sizeof(" + datatype + "*),ALIGNMENT);\n" +
    "if (!bench_buffer) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return 0;} " +
    "for(int i = 0; i < numberofshifts; i++){" +
    "bench_buffer[i] = (" + datatype + "*) _mm_malloc(size,ALIGNMENT);" +
    "if (!bench_buffer[i]) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return 0;} " +
    "}" +
    "return (void*)bench_buffer;" +
    "}"
  }

  def destroy_array_of_buffers () =
  {
    "void DestroyBuffers(void ** bench_buffer, long numberofshifts) {" +
    "for(int i = 0; i < numberofshifts; i++)" +
    "_mm_free(bench_buffer[i]);" +
    "_mm_free(bench_buffer);" +
    "}"
  }

  def Counters2CCode(): (String,String) =
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

  def setFlopCounter( tu: (Array[HWCounters.Counter],List[Int]) )  =
  {
   counters = tu._1
   mask = tu._2

   this
  }

}



object CodeGeneration {



  def daxpy_MKL(double_precision: Boolean,warm: Boolean, size: Long ): CodeGeneration =
  {
    val daxpy = new CodeGeneration
    daxpy.id = if (double_precision)
       "daxpy_MKL_" + size
    else
      "saxpy_MKL_" + size
    daxpy.size = size.toInt
    daxpy.total_size = (2 * size).toInt
    daxpy.includes = "#include <mkl.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"
    daxpy.initcode = "double alpha = 1.1;"

    if (double_precision)
      daxpy.datatype = "double"
    else
      daxpy.datatype = "float"

    daxpy.create_buffer_call = if (warm)
    {
        daxpy.datatype +" * x = (" + daxpy.datatype + " *) _mm_malloc("+size+"*sizeof(" + daxpy.datatype + "),ALIGNMENT);" +
        daxpy.datatype +" * y = (" + daxpy.datatype + " *) _mm_malloc("+size+"*sizeof(" + daxpy.datatype + "),ALIGNMENT);"
    }
    else
    {
      daxpy.datatype + " ** x_array = (" + daxpy.datatype + " **) CreateBuffers("+size+"* sizeof(" + daxpy.datatype + "),numberofshifts);" +
      daxpy.datatype + " ** y_array = (" + daxpy.datatype + " **) CreateBuffers("+size+"* sizeof(" + daxpy.datatype + "),numberofshifts);"
    }

    daxpy.create_buffer_function = daxpy.create_array_of_buffers()
    daxpy.destroy_buffer_function = daxpy.destroy_array_of_buffers()
    daxpy.init_function = daxpy.ini11()

    daxpy.init_call = if (warm)
    {
      "_ini1(x,"+size+" ,1);_ini1(y,"+size+" ,1);"
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
      "_ini1(x_array[i],"+size+" ,1);" +
      "_ini1(y_array[i],"+size+" ,1);" +
      "}"
    }


    daxpy.kernel_call = if (warm)
    {
      if (double_precision)
        "cblas_daxpy("+size+", alpha, x, 1, y, 1);"
      else
        "cblas_saxpy("+size+", alpha, x, 1, y, 1);"
    }
    else
    {
      if (double_precision)
        "cblas_daxpy("+size+", alpha, x_array[i%numberofshifts], 1, y_array[i%numberofshifts], 1);"
      else
        "cblas_saxpy("+size+", alpha, x_array[i%numberofshifts], 1, y_array[i%numberofshifts], 1);"
    }

    daxpy.destroy_buffer_call = if (warm)
    {
      "_mm_free(x);_mm_free(y);"
    }
    else
      "DestroyBuffers( (void **) x_array, numberofshifts);DestroyBuffers( (void **) y_array, numberofshifts);"

    daxpy.determineSize_call = daxpy.tuneNrRunsbySize()
    daxpy.nrRuns = daxpy.tuneNrRunsbyRunTime()
    daxpy

  }


  def dgemv_MKL(double_precision: Boolean,warm: Boolean, size: Long ): CodeGeneration =
  {
    val dgemv = new CodeGeneration
    dgemv.id = if(double_precision) "dgemv_MKL_" + size else "sgemv_MKL_" + size
    dgemv.size = size.toInt
    dgemv.total_size = (2 * size + size*size).toInt
    dgemv.includes = "#include <mkl.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"


    if (double_precision)
      dgemv.datatype = "double"
    else
      dgemv.datatype = "float"

    dgemv.initcode = dgemv.datatype + " alpha = 1.1;" + dgemv.datatype + " beta = 1.1;"

    dgemv.create_buffer_call = if (warm)
    {
      dgemv.datatype +" * x = (" + dgemv.datatype + " *) _mm_malloc("+size+"*sizeof(" + dgemv.datatype + "),ALIGNMENT);" +
      dgemv.datatype +" * y = (" + dgemv.datatype + " *) _mm_malloc("+size+"*sizeof(" + dgemv.datatype + "),ALIGNMENT);" +
      dgemv.datatype +" * A = (" + dgemv.datatype + " *) _mm_malloc("+size*size+"*sizeof(" + dgemv.datatype + "),ALIGNMENT);"
    }
    else
    {
      dgemv.datatype + " ** x_array = (" + dgemv.datatype + " **) CreateBuffers("+size+"* sizeof(" + dgemv.datatype + "),numberofshifts);" +
      dgemv.datatype + " ** y_array = (" + dgemv.datatype + " **) CreateBuffers("+size+"* sizeof(" + dgemv.datatype + "),numberofshifts);" +
      dgemv.datatype + " ** A_array = (" + dgemv.datatype + " **) CreateBuffers("+size*size+"* sizeof(" + dgemv.datatype + "),numberofshifts);"
    }

    dgemv.create_buffer_function = dgemv.create_array_of_buffers()
    dgemv.destroy_buffer_function = dgemv.destroy_array_of_buffers()
    dgemv.init_function = dgemv.ini11()

    dgemv.init_call = if (warm)
    {
      "_ini1(x,"+size+" ,1);_ini1(y,"+size+" ,1);_ini1(A,"+size*size+" ,1);"
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
        "_ini1(x_array[i],"+size+" ,1);" +
        "_ini1(y_array[i],"+size+" ,1);" +
        "_ini1(A_array[i],"+size*size+" ,1);" +
        "}"
    }

    dgemv.kernel_call = if (warm)
    {
      if (double_precision)
        "cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, beta, y, 1);"
      else
        "cblas_sgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, beta, y, 1);"
    }
    else
    {
      if (double_precision)
        "cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A_array[i%numberofshifts], "+size+", x_array[i%numberofshifts], 1, beta, y_array[i%numberofshifts], 1);"
      else
        "cblas_sgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A_array[i%numberofshifts], "+size+", x_array[i%numberofshifts], 1, beta, y_array[i%numberofshifts], 1);"
    }

    dgemv.destroy_buffer_call = if (warm)
    {
      "_mm_free(x);_mm_free(y);_mm_free(A);"
    }
    else
      "DestroyBuffers( (void **) x_array, numberofshifts);DestroyBuffers( (void **) y_array, numberofshifts);DestroyBuffers( (void **) A_array, numberofshifts);"

    dgemv.determineSize_call = dgemv.tuneNrRunsbySize()
    dgemv.nrRuns = dgemv.tuneNrRunsbyRunTime()

    dgemv
  }


  def dgemm_MKL(double_precision: Boolean,warm: Boolean, size: Long ): CodeGeneration =
  {
    val dgemm = new CodeGeneration
    dgemm.id = if(double_precision) "dgemm_MKL_" + size else "sgemm_MKL_" + size
    dgemm.size = size.toInt
    dgemm.total_size = (3* size*size).toInt
    dgemm.includes = "#include <mkl.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"


    if (double_precision)
      dgemm.datatype = "double"
    else
      dgemm.datatype = "float"

    dgemm.initcode = dgemm.datatype + " alpha = 1.1;" + dgemm.datatype + " beta = 1.1; int size="+size+";"

    dgemm.create_buffer_call = if (warm)
    {
      dgemm.datatype +" * A = (" + dgemm.datatype + " *) _mm_malloc("+size*size+"*sizeof(" + dgemm.datatype + "),ALIGNMENT);" +
      dgemm.datatype +" * B = (" + dgemm.datatype + " *) _mm_malloc("+size*size+"*sizeof(" + dgemm.datatype + "),ALIGNMENT);" +
      dgemm.datatype +" * C = (" + dgemm.datatype + " *) _mm_malloc("+size*size+"*sizeof(" + dgemm.datatype + "),ALIGNMENT);"
    }
    else
    {
      dgemm.datatype + " ** A_array = (" + dgemm.datatype + " **) CreateBuffers("+size*size+"* sizeof(" + dgemm.datatype + "),numberofshifts);" +
      dgemm.datatype + " ** B_array = (" + dgemm.datatype + " **) CreateBuffers("+size*size+"* sizeof(" + dgemm.datatype + "),numberofshifts);" +
      dgemm.datatype + " ** C_array = (" + dgemm.datatype + " **) CreateBuffers("+size*size+"* sizeof(" + dgemm.datatype + "),numberofshifts);"
    }

    dgemm.create_buffer_function = dgemm.create_array_of_buffers()
    dgemm.destroy_buffer_function = dgemm.destroy_array_of_buffers()
    dgemm.init_function = dgemm.ini11()

    dgemm.init_call = if (warm)
    {
      "_ini1(A,"+size*size+" ,1);_ini1(B,"+size*size+" ,1);_ini1(C,"+size*size+" ,1);"
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
        "_ini1(A_array[i],"+size*size+" ,1);" +
        "_ini1(B_array[i],"+size*size+" ,1);" +
        "_ini1(C_array[i],"+size*size+" ,1);" +
        "}"
    }

    dgemm.kernel_call = if (warm)
    {
      if (double_precision)
        "cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size, alpha, A, size, B, size, beta, C, size);"
      else
        "cblas_sgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size, alpha, A, size, B, size, beta, C, size);"
    }
    else
    {
      if (double_precision)
        "cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,alpha,A_array[i%numberofshifts], size,B_array[i%numberofshifts], size,beta,C_array[i%numberofshifts], size);"
      else
        "cblas_sgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,alpha,A_array[i%numberofshifts], size,B_array[i%numberofshifts], size,beta,C_array[i%numberofshifts], size);"
    }

    dgemm.destroy_buffer_call = if (warm)
    {
      "_mm_free(A);_mm_free(B);_mm_free(C);"
    }
    else
      "DestroyBuffers( (void **) A_array, numberofshifts);DestroyBuffers( (void **) B_array, numberofshifts);DestroyBuffers( (void **) C_array, numberofshifts);"

    dgemm.determineSize_call = dgemm.tuneNrRunsbySize()
    dgemm.nrRuns = dgemm.tuneNrRunsbyRunTime()

    dgemm
  }


  def fft_MKL(double_precision: Boolean,warm: Boolean, size: Long, inplace: Boolean ): CodeGeneration =
  {
    val fft = new CodeGeneration
    fft.id = if (double_precision)
      "fft_MKL_double_" + size
    else
      "fft_MKL_single_" + size
    fft.size = size.toInt
    fft.total_size = if (inplace)  (2 * size).toInt else  (4 * size).toInt

    fft.includes = "#include <mkl.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"

    val dfti_prec = if (double_precision) "DFTI_DOUBLE" else "DFTI_SINGLE"
    fft.initcode = "DFTI_DESCRIPTOR_HANDLE mklDescriptor;"+
      "MKL_LONG status;" +
      "status = DftiCreateDescriptor( &mklDescriptor, " + dfti_prec+ ",DFTI_COMPLEX, 1,"+ size + ");" +
      "if (status != 0) {\n    return -1;\n\t}\n\n\tstatus = DftiCommitDescriptor(mklDescriptor);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}"

    if (double_precision)
      fft.datatype = "double"
    else
      fft.datatype = "float"

    fft.create_buffer_call = if (warm)
    {
      fft.datatype +" * x = (" + fft.datatype + " *) _mm_malloc("+2*size+"*sizeof(" + fft.datatype + "),ALIGNMENT);" +
      (if (!inplace) fft.datatype +" * y = (" + fft.datatype + " *) _mm_malloc("+2*size+"*sizeof(" + fft.datatype + "),ALIGNMENT);" else "")
    }
    else
    {
      fft.datatype + " ** x_array = (" + fft.datatype + " **) CreateBuffers("+2*size+"* sizeof(" + fft.datatype + "),numberofshifts);" +
      (if (!inplace) fft.datatype + " ** y_array = (" + fft.datatype + " **) CreateBuffers("+2*size+"* sizeof(" + fft.datatype + "),numberofshifts);" else "")
    }

    fft.create_buffer_function = fft.create_array_of_buffers()
    fft.destroy_buffer_function = fft.destroy_array_of_buffers()
    fft.init_function = fft.ini11()

    fft.init_call = if (warm)
    {
      "_ini1(x,"+size+" ,1);" +
      (if (!inplace) "_ini1(y,"+size+" ,1);" else "")
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
        "_ini1(x_array[i],"+size+" ,1);" +
        (if (!inplace) "_ini1(y_array[i],"+size+" ,1);" else "") +
        "}"
    }

    fft.kernel_call = if (warm){
      if (inplace)
        "status = DftiComputeForward(mklDescriptor, x);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}"
      else
        "status = DftiComputeForward(mklDescriptor, x,y);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}"
    }
    else
    {
      if (inplace)
        "status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}"
      else
        "status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts], y_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}"
    }

    fft.destroy_buffer_call = if (warm)
    {
      "_mm_free(x);"+
      (if (!inplace) "_mm_free(y);" else "")
    }
    else
      "DestroyBuffers( (void **) x_array, numberofshifts);" +
      (if (!inplace) "DestroyBuffers( (void **) y_array, numberofshifts);" else "")

    fft.determineSize_call = fft.tuneNrRunsbySize()
    fft.nrRuns = fft.tuneNrRunsbyRunTime()

    fft
  }

  def fft_FFTW(double_precision: Boolean,warm: Boolean, size: Long, inplace: Boolean ): CodeGeneration =
  {
    val fft = new CodeGeneration
    fft.id = if (double_precision)
      "fft_FFTW_double_" + size
    else
    {
      assert(false,"not implemented yet")
      "fft_FFTW_single_" + size
    }

    if (inplace)
      assert(false, "not yet implemented!")

    fft.size = size.toInt
    fft.total_size = if (inplace)  (2 * size).toInt else  (4 * size).toInt

    fft.includes = "#include <fftw3.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"

    
    fft.initcode = "fftw_plan fftwPlan;"

    if (double_precision)
      fft.datatype = "fftw_complex"
    else
      fft.datatype = "float"

    fft.create_buffer_call = if (warm)
    {
      fft.datatype +" * x = (" + fft.datatype + " *) fftw_malloc("+size+"*sizeof(" + fft.datatype + "));" +
        (if (!inplace) fft.datatype +" * y = (" + fft.datatype + " *) fftw_malloc("+size+"*sizeof(" + fft.datatype + "));" else "")
    }
    else
    {
      fft.datatype + " ** x_array = (" + fft.datatype + " **) CreateBuffers("+size+"* sizeof(" + fft.datatype + "),numberofshifts);" +
        (if (!inplace) fft.datatype + " ** y_array = (" + fft.datatype + " **) CreateBuffers("+size+"* sizeof(" + fft.datatype + "),numberofshifts);" else "")
    }


    //FFTW requires special memory allocation
    fft.create_buffer_function = "void * CreateBuffers(long size, long numberofshifts)" +
      "{" +
      fft.datatype + " ** bench_buffer = (" + fft.datatype + "**) _mm_malloc(numberofshifts*sizeof(" + fft.datatype + "*),ALIGNMENT);" +
      "if (!bench_buffer) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return ;} " +
      "for(int i = 0; i < numberofshifts; i++){" +
      "bench_buffer[i] = (" + fft.datatype + "*) fftw_malloc(size * sizeof(fftw_complex));" +
      "if (!bench_buffer[i]) {\n      std::cout << \"fftw_malloc failed\";\n      measurement_end();\n      return ;} " +
      "}" +
      "return (void*)bench_buffer;" +
      "}"

    //FFTW requires special memory deallocation
    fft.destroy_buffer_function = "void DestroyBuffers(void ** bench_buffer, long numberofshifts) {" +
      "for(int i = 0; i < numberofshifts; i++)" +
      " fftw_free(bench_buffer[i]);" +
      "_mm_free(bench_buffer);" +
      "}"
    fft.init_function = "void _ini1("+ fft.datatype + " * mf, size_t row, size_t col)\n{double * m = (double *) mf;\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}"

    fft.init_call = if (warm)
    {
      "_ini1(x,"+2*size+" ,1);" +
        (if (!inplace) "_ini1(y,"+2*size+" ,1);" else "") +
      "fftwPlan = fftw_plan_dft_1d("+size+", x, y, FFTW_FORWARD, FFTW_MEASURE);"
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
        "_ini1(x_array[i],"+2*size+" ,1);" +
        (if (!inplace) "_ini1(y_array[i],"+2*size+" ,1);" else "") +
        "}"+
      "fftwPlan = fftw_plan_dft_1d("+size+", x_array[0], y_array[0], FFTW_FORWARD, FFTW_MEASURE);"
    }

    fft.kernel_call = if (warm){
      if (inplace)
        ""
      else
        "fftw_execute_dft(fftwPlan,x,y);"
    }
    else
    {
      if (inplace)
        ""
      else
        "fftw_execute_dft(fftwPlan,x_array[i%numberofshifts],y_array[i%numberofshifts]);"
    }

    fft.destroy_buffer_call = if (warm)
    {
      "fftw_free(x);"+
        (if (!inplace) "fftw_free(y);" else "")
    }
    else
      "DestroyBuffers( (void **) x_array, numberofshifts);" +
        (if (!inplace) "DestroyBuffers( (void **) y_array, numberofshifts);" else "")

    fft.determineSize_call = fft.tuneNrRunsbySize()
    fft.nrRuns = fft.tuneNrRunsbyRunTime()

    fft
  }

  //GO: This is the public available code from http://www.spiral.net/codegenerator.html
  def fft_Spiral(double_precision: Boolean,warm: Boolean, size: Long, vectorized: Boolean ): CodeGeneration =
  {

    val spiral_source = "/home/ofgeorg/" +
      (if (vectorized)
        "fft_sse/"
      else
        "fft_scalar/"
      )
    val fft = new CodeGeneration
    val vecs = if (vectorized) "vectorized_" else ""
    fft.id = if (double_precision)
      "fft_Spiral_double_" + vecs + size
    else
      "fft_Spiral_single_" + vecs + size
    fft.size = size.toInt
    fft.total_size = (4 * size).toInt

    fft.includes =
      "#include <iostream>\n#include \"" + spiral_source + "spiral_fft.h\"\n    #include \""+ spiral_source + "spiral_private.h\"\n    #include \""+ spiral_source +"spiral_private.c\"\n    #include \""+ spiral_source +"spiral_fft_double.c\""


    fft.initcode =   "spiral_status_t status; std::string statusStr;"

    if (double_precision)
      fft.datatype = "double"
    else
    {
      assert(false, "not implemented")
      fft.datatype = "float"
    }

    fft.create_buffer_call = if (warm)
    {
      fft.datatype +" * x = (" + fft.datatype + " *) _mm_malloc("+2*size+"*sizeof(" + fft.datatype + "),ALIGNMENT);" +
      fft.datatype +" * y = (" + fft.datatype + " *) _mm_malloc("+2*size+"*sizeof(" + fft.datatype + "),ALIGNMENT);"
    }
    else
    {
      fft.datatype + " ** x_array = (" + fft.datatype + " **) CreateBuffers("+2*size+"* sizeof(" + fft.datatype + "),numberofshifts);" +
      fft.datatype + " ** y_array = (" + fft.datatype + " **) CreateBuffers("+2*size+"* sizeof(" + fft.datatype + "),numberofshifts);"
    }

    fft.create_buffer_function = fft.create_array_of_buffers()
    fft.destroy_buffer_function = fft.destroy_array_of_buffers()
    fft.init_function = fft.ini11()

    fft.init_call = if (warm)
    {
      "_ini1(x,"+size+" ,1);" +
      "_ini1(y,"+size+" ,1);"
    }
    else
    {
      "for(int i = 0; i < numberofshifts; i++){" +
        "_ini1(x_array[i],"+size+" ,1);" +
        "_ini1(y_array[i],"+size+" ,1);"  +
        "}"
    }

    fft.kernel_call = if (warm){


      "status = spiral_fft_double("+ size + ", 1, x, y);"
    }
    else
    {
      "status = spiral_fft_double("+ size + ", 1, x_array[i%numberofshifts], y_array[i%numberofshifts]);"
    }

    fft.destroy_buffer_call = if (warm)
    {
      "_mm_free(x);"+
      "_mm_free(y);"
    }
    else
      "DestroyBuffers( (void **) x_array, numberofshifts);" +
      "DestroyBuffers( (void **) y_array, numberofshifts);"

    fft.determineSize_call = fft.tuneNrRunsbySize()
    fft.nrRuns = fft.tuneNrRunsbyRunTime()

    fft
  }



  def blas_copy_MKL(size: Long ): CodeGeneration =
  {
    val blas_copy = new CodeGeneration
    blas_copy.id =  "blas_copy_MKL_" + size

    blas_copy.size = size.toInt
    blas_copy.total_size = (2 * size).toInt
    blas_copy.includes = "#include <mkl.h>\n#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n"
    blas_copy.initcode = "double alpha = 1.1;"


    blas_copy.datatype = "double"


    blas_copy.create_buffer_call =
    {
      blas_copy.datatype +" * x = (" + blas_copy.datatype + " *) _mm_malloc("+size+"*sizeof(" + blas_copy.datatype + "),ALIGNMENT);" +
      blas_copy.datatype +" * y = (" + blas_copy.datatype + " *) _mm_malloc("+size+"*sizeof(" + blas_copy.datatype + "),ALIGNMENT);"
    }

    blas_copy.create_buffer_function = blas_copy.create_array_of_buffers()
    blas_copy.destroy_buffer_function = blas_copy.destroy_array_of_buffers()
    blas_copy.init_function = blas_copy.ini11()

    blas_copy.init_call =
    {
      "_ini1(x,"+size+" ,1);_ini1(y,"+size+" ,1);"
    }


    blas_copy.kernel_call =  "cblas_dcopy("+size+", x, 1, y, 1);"

    blas_copy.destroy_buffer_call =  "_mm_free(x);_mm_free(y);"


    blas_copy.determineSize_call = blas_copy.tuneNrRunsbySize()
    blas_copy.nrRuns = blas_copy.tuneNrRunsbyRunTime()
    blas_copy

  }
  
  
  /*





  def ninefold_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("#define NB 50")
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void dgemm(double *A, double * B, double * C, unsigned long size) {")

    p("int i=0, j=0, k=0;")
    p("int ii, jj, kk;")

    p("double cij, cij1, ci1j, ci1j1;")
    p("double aik, aik1, ai1k, ai1k1, bkj, bk1j, bkj1, bk1j1;")
    p("double t1, t2, t3;")

    p("for (i = 0; i < size; i+=NB) {")
    p("    for (j = 0; j < size; j+=NB) {")
    p("        for (k = 0; k < size; k+=NB) {")
    p("            for (ii = 0; ii < NB; ii+=2) {")
    p("                for (jj = 0; jj < NB; jj+=2) {")

    p("                    cij   = C[i*size + ii*size + j + jj];")
    p("                    cij1  = C[i*size + ii*size + j + jj + 1];")
    p("                    ci1j  = C[i*size + ii*size + size + j + jj];")
    p("                    ci1j1 = C[i*size + ii*size + size + j + jj + 1];")

    p("                    aik   = A[i*size + ii*size + k];")
    p("                    ai1k  = A[i*size + ii*size + size + k];")
    p("                    bkj   = B[k*size + j + jj];")
    p("                    bkj1  = B[k*size + j + jj + 1];")

    p("                    for (kk = 0; kk < NB-2; kk+=2) {")

    p("                        t1    = aik*bkj;")
    p("                        t2    = ai1k*bkj;")
    p("                        bkj   = B[k*size + kk*size + size + j + jj];")
    p("                        t3    = aik*bkj1;")
    p("                        aik   = A[i*size + ii*size + k + kk + 1];")
    p("                        cij   = cij   + t1;")
    p("                        t1    = ai1k*bkj1;")
    p("                        ai1k  = A[i*size + ii*size + size + k + kk + 1];")
    p("                        bkj1  = B[k*size + kk*size + size + j + jj + 1];")
    p("                        ci1j  = ci1j  + t2;")
    p("                        t2    = aik*bkj;")
    p("                        cij1  = cij1  + t3;")
    p("                        t3    = ai1k*bkj;")
    p("                        bkj   = B[k*size + kk*size + 2*size + j + jj];")
    p("                        ci1j1 = ci1j1 + t1;")
    p("                        t1    = aik*bkj1;")
    p("                        aik   = A[i*size + ii*size + k + kk + 2];")
    p("                        cij   = cij   + t2;")
    p("                        t2    = ai1k*bkj1;")
    p("                        ai1k  = A[i*size + ii*size + size + k + kk + 2];")
    p("                        bkj1  = B[k*size + kk*size + 2*size + j + jj + 1];")
    p("                        ci1j  = ci1j  + t3;")
    p("                        cij1  = cij1  + t1;")
    p("                        ci1j1 = ci1j1 + t2;")

    p("                    }")

    p("                    t1    = aik*bkj;")
    p("                    t2    = ai1k*bkj;")
    p("                    bkj   = B[k*size + NB*size - size + j + jj];")
    p("                    t3    = aik*bkj1;")
    p("                    aik   = A[i*size + ii*size + k + NB - 1];")
    p("                    cij   = cij   + t1;")
    p("                    t1    = ai1k*bkj1;")
    p("                    ai1k  = A[i*size + ii*size + size + k + NB - 1];")
    p("                    bkj1  = B[k*size + NB*size - size + j + jj + 1];")
    p("                    ci1j  = ci1j  + t2;")
    p("                    t2    = aik*bkj;")
    p("                    cij1  = cij1  + t3;")
    p("                    t3    = ai1k*bkj;")
    p("                    ci1j1 = ci1j1 + t1;")
    p("                    t1    = aik*bkj1;")
    p("                    cij   = cij   + t2;")
    p("                    t2    = ai1k*bkj1;")
    p("                    ci1j  = ci1j  + t3;")
    p("                    cij1  = cij1  + t1;")
    p("                    ci1j1 = ci1j1 + t2;")

    p("                    C[i*size + ii*size + j + jj]   = cij;")
    p("                    C[i*size + ii*size + j + jj + 1]  = cij1;")
    p("                    C[i*size + ii*size + size + j + jj]  = ci1j;")
    p("                    C[i*size + ii*size + size + j + jj + 1] = ci1j1;")
    p("                }")
    p("            }")
    p("        }")
    p("    }")
    p("}")

    p("}")

    p("int main () { ")
    p("srand(1984);")

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

      p("_ini1(A,"+size+" ,"+size+");")
      p("_ini1(B,"+size+" ,"+size+");")
      p("_ini1(C,"+size+" ,"+size+");")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "dgemm(A,B,C,size);" ,"" )

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

        p("long size_per_run = " +size + ";")
        p(" size_per_run = size_per_run *  size_per_run * 3 * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")

        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (3*size*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;") 
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** B_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** C_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("_ini1(B_array[i],"+size+" ,"+size+");")
        p("_ini1(C_array[i],"+size+" ,"+size+");")
        p("}")



        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	      p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A_array[i%numberofshifts], B_array[i%numberofshifts],C_array[i%numberofshifts], size);")
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
        p("dgemm(A,B,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        //p("std::cout << \"deallocate\";")
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

  def sixfold_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("#define NB 50")
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void dgemm(double *A, double * B, double * C, unsigned long size) {")

    p("int i=0, j=0, k=0;")
    p("int ii, jj, kk;")

    p("double cij, cij1, ci1j, ci1j1;")
    p("double aik, aik1, ai1k, ai1k1, bkj, bk1j, bkj1, bk1j1;")
    p("double t1, t2, t3;")

    p("for (i = 0; i < size; i+=NB) {")
    p("    for (j = 0; j < size; j+=NB) {")
    p("        for (k = 0; k < size; k+=NB) {")
    p("            for (ii = 0; ii < NB; ii+=1) {")
    p("                for (jj = 0; jj < NB; jj+=1) {")
    p("                  for (kk = 0; kk < NB; kk+=1) {")
    p("                 	C[i*size+ii*size+j+jj] += A[i*size+ii*size+k+kk]*B[k*size+kk*size+j+jj];")
    p("               	 }")
    p("                }")
    p("            }")
    p("        }")
    p("    }")
    p("}")

    p("}")

    p("int main () { ")
    p("srand(1984);")

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

      p("_ini1(A,"+size+" ,"+size+");")
      p("_ini1(B,"+size+" ,"+size+");")
      p("_ini1(C,"+size+" ,"+size+");")

      p("int n = " +size + ";")
      //Tune the number of runs
     //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "dgemm(A,B,C,size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")

        p("long size_per_run = " +size + ";")
        p(" size_per_run = size_per_run *  size_per_run * 3 * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")

        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (3*size*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;") 
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** B_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** C_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("_ini1(B_array[i],"+size+" ,"+size+");")
        p("_ini1(C_array[i],"+size+" ,"+size+");")
        p("}")


        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A_array[i%numberofshifts], B_array[i%numberofshifts],C_array[i%numberofshifts], size);")
        p("}")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	      p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A_array[i%numberofshifts], B_array[i%numberofshifts],C_array[i%numberofshifts], size);")
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
        p("dgemm(A,B,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        //p("std::cout << \"deallocate\";")
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

  def tripple_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void dgemm(double *A, double * B, double * C, unsigned long size) {")
    p("long wtf = 0;")

    p("for (int i = 0; i < size; i++)")
    p("for (int j = 0; j < size; j++)")
    p("for (int k = 0; k < size; k++){")
    //p("C[i][j] += A[i][k]*B[k][j];")
    p("C[i*size+j] += A[i*size+k]*B[k*size+j];")

    p("}")

    p("}")

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("int size = " +size + ";")
      //allocate
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * B = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * C = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")

      p("_ini1(A,"+size+" ,"+size+");")
      p("_ini1(B,"+size+" ,"+size+");")
      p("_ini1(C,"+size+" ,"+size+");")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "dgemm(A,B,C,size);" ,"" )
      //p("long runs = 2;")

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("long size_per_run = " +size + ";")
        p(" size_per_run = size_per_run *  size_per_run * 3 * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")


        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")

        p("long numberofshifts = (100 * 1024 * 1024 / (" + (3*size*size)+ "* sizeof(" + prec + ")));")

        p("if (numberofshifts < 2) numberofshifts = 2;")
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("__asm cpuid;")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** B_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** C_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("_ini1(B_array[i],"+size+" ,"+size+");")
        p("_ini1(C_array[i],"+size+" ,"+size+");")
        p("}")

        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A_array[i%numberofshifts], B_array[i%numberofshifts],C_array[i%numberofshifts], size);")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	      p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A_array[i%numberofshifts], B_array[i%numberofshifts],C_array[i%numberofshifts], size);")
        /*p("for (int i = 0; i < size; i++)")
        p("for (int j = 0; j < size; j++)")
        p("for (int k = 0; k < size; k++){")
        //p("C[i][j] += A[i][k]*B[k][j];")
        p("C[i*size+j] += A[i*size+k]*B[k*size+j];")
        //p("C[i*size+j] = A[i*size+k];")*/
        p("}")
        p( "measurement_stop(runs);")
        p("__asm cpuid;")
        p( " }")
        /*p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);") */

        p("DestroyBuffers( (void **) A_array, numberofshifts);")
        p("DestroyBuffers( (void **) B_array, numberofshifts);")
        p("DestroyBuffers( (void **) C_array, numberofshifts);")

      }
      else
      {
        //run it
        //p("const int size = "+ size+ ";");
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        //p("for(int i = 0; i < runs; i++){")
        //p("dgemm(A,B,C, size);")
        p("for (int i = 0; i < size; i++)")
        p("for (int j = 0; j < size; j++)")
        p("for (int k = 0; k < size; k++){")
        //p("C[i][j] += A[i][k]*B[k][j];")
        p("C[i*size+j] += A[i*size+k]*B[k*size+j];")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        //p( "measurement_stop(runs);")
        p( "measurement_stop(1);")
        p( " }")
        //p("std::cout << \"deallocate\";")
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
  
  def daxpy_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void daxpy(double *x, double * y, double alpha, unsigned long size) {")

    p("double tmp = 1;")
    p("for (int i = 0; i < size; i++)")
    //p("tmp += tmp;")
    p("y[i] = x[i]*1.1 + y[i];")
    //p("a = _mm256_load_pd(")

    //p("y[i] += 1.1*x[i];")

    //p("y[i] = x[i];")
    p("x[0] = tmp;")
    p("}")


    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "daxpy(x,y,3.0,size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(x);")
        p("_mm_free(y);")

        p("long size_per_run = " +size + ";")
        p(" size_per_run = size_per_run * 2 * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")

        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;") 
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+size+" , 1);")
        p("_ini1(y_array[i],"+size+" , 1);")
        p("}")

        p("for(int i = 0; i < runs; i++){")
        p("daxpy(x_array[i%numberofshifts], y_array[i%numberofshifts], 3.1, size);")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	      p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("daxpy(x_array[i%numberofshifts], y_array[i%numberofshifts], 3.1,size);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
      }
      else
      {
/*
        //run ity        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A,B,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
*/
      }
      p("}")

    }
    p("measurement_end();")
    p("}")
  }


  def dgemv_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void dgemv(double *y, double *A, double * x, unsigned long size) {")

    p("for (int i = 0; i < size; i++)")
    p("for (int j = 0; j< size; j++)")
    p("y[i]+=( A[i*size+j]*x[j]);")
    p("}")

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")
      p("_ini1(A,"+size+" ,"+size+");")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "dgemv(y,A,x,size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(x);")
        p("_mm_free(y);")
        p("_mm_free(A);")

        p("long size_per_run = " +size + ";")
        p(" size_per_run = (size_per_run *  size_per_run +  size_per_run * 2) * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")


        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")
        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+size+" , 1);")
        p("_ini1(y_array[i],"+size+" , 1);")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("}")


        p("for(int i = 0; i < runs; i++){")
        p("dgemv(y_array[i%numberofshifts],A_array[i%numberofshifts], y_array[i%numberofshifts], size);")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemv(y_array[i%numberofshifts],A_array[i%numberofshifts], y_array[i%numberofshifts], size);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
        p("DestroyBuffers( (void **) A_array, numberofshifts);")
      }
      else
      {
        /*
                //run ity        p("for(int r = 0; r < " + Config.repeats + "; r++){")
                p("measurement_start();")
                p("for(int i = 0; i < runs; i++){")
                p("dgemm(A,B,C, size);")
                //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
                p("}")
                p( "measurement_stop(runs);")
                p( " }")
                p("std::cout << \"deallocate\";")
                //deallocate the buffers
                p("_mm_free(A);")
                p("_mm_free(B);")
                p("_mm_free(C);")
        */
      }
      p("}")

    }
    p("measurement_end();")
    p("}")
  }


  def copy_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

     p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
//     p("#include <immintrin.h>")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("unsigned long size = " + size + ";")
      p("unsigned long runs = 10;")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	    p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dcopy(size, x, 1, y, 1);")
        p("}")
        p( "measurement_stop(runs);")
 	  //p("std::cout << y[rand()%size] << \"\\n\";")
        p( " }")
        p("_mm_free(x);")
        p("_mm_free(y);")
      p("}")

    }
    p("measurement_end();")
    p("}")
  }


  def write_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], par: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)

    if(par) p("#include <omp.h>")
    p("#include <immintrin.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    //     p("#include <immintrin.h>")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")

    if(!par) {
      p("double write_array(size_t size, double * x) {")

      p("	__m256d t0, t1;")
      p("		t0 = _mm256_load_pd(x);")
      p("		t1 = _mm256_load_pd(x+4);")
      p("	__declspec(align(32)) double res[4];")
      p("	for (size_t i=0; i < size; i+=8) {")
      p("		 _mm256_stream_pd(x+i,t0);")
      p("		 _mm256_stream_pd(x+i+4,t1);")
      p("	}")
      //p("	_mm256_store_pd(res, _mm256_add_pd(t0,t1));")

      p("	return res[0];")
      p("}")
    } else {
      p("double write_array_par(size_t size, double * x) {")

      p("	double t=0, t0;")

      p("#pragma omp parallel private(t0)")
      p("	{")
      p("#pragma omp for schedule(static, size/4)")
      p("		for (size_t i=0; i < size; i++) {")
      p("			x[i] = p;")
      p("		}")
      p("		t += t0;")
      p("	}")
      p("	return t;")

      p("}")
    }

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("unsigned long size = " + size + ";")
      p("unsigned long runs = 10;")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

      p("for(int r = 0; r < " + Config.repeats + "; r++){")
      p("measurement_start();")
      p("for(int i = 0; i < runs; i++){")
      if(!par) {
        p("write_array(size, x);")
      } else {
        p("write_array_par(size, x);")
      }
      p("}")
      p( "measurement_stop(runs);")
      //p("std::cout << y[rand()%size] << \"\\n\";")
      p( " }")
      p("_mm_free(x);")
      p("_mm_free(y);")
      p("}")

    }
    p("measurement_end();")
    p("}")
  }



  def read_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], par: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    
    if(par) p("#include <omp.h>")
    p("#include <immintrin.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
//     p("#include <immintrin.h>")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    
    if(!par) {
      p("double read_array(size_t size, double * x) {")

      p("	__m256d t0, t1;")
      p("	__declspec(align(32)) double res[4];")
      p("	for (size_t i=0; i < size; i+=8) {")
      p("		t0 = _mm256_load_pd(x+i);")
      p("		t1 = _mm256_load_pd(x+i+4);")
      p("	}")
      p("	_mm256_store_pd(res, _mm256_add_pd(t0,t1));")

      p("	return res[0];")
      p("}")
    } else {
      p("double read_array_par(size_t size, double * x) {")

      p("	double t=0, t0;")

      p("#pragma omp parallel private(t0)")
      p("	{")
      p("#pragma omp for schedule(static, size/4)")
      p("		for (size_t i=0; i < size; i++) {")
      p("			t0 = x[i];")
      p("		}")
      p("		t += t0;")
      p("	}")
      p("	return t;")

      p("}")
    }

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("unsigned long size = " + size + ";")
      p("unsigned long runs = 10;")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
	if(!par) {
	  p("read_array(size, x);")
	} else {
	  p("read_array_par(size, x);")
	}
        p("}")
        p( "measurement_stop(runs);")
 	//p("std::cout << y[rand()%size] << \"\\n\";")
        p( " }")
        p("_mm_free(x);")
        p("_mm_free(y);")
      p("}")

    }
    p("measurement_end();")
    p("}")
  }

  def scale_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void daxpy(double *x, double * y, unsigned long size) {")

    p("for (int i = 0; i < size; i++)")
    p("y[i] = 1.1*x[i];")
    p("}")

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "daxpy(x,y,size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;") 
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+size+" , 1);")
        p("_ini1(y_array[i],"+size+" , 1);")
        p("}")



        //p( "std::cout << \"ENTERING\\n\";")
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        //p( "std::cout << runs << \"\\n\";")
	p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("daxpy(x_array[i%numberofshifts], y_array[i%numberofshifts], size);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
      }
      else
      {
/*
        //run ity        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A,B,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
*/
      }
      p("}")

    }
    p("measurement_end();")
    p("}")
  }

  def add_loop(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")



    p("void daxpy(double *x, double * y, unsigned long size) {")

    p("for (int i = 0; i < size; i++)")
    p("y[i] += x[i];")
    p("}")

    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("unsigned long size = " +size + ";")
      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")

      p("_ini1(x,"+size+" , 1);")
      p("_ini1(y,"+size+" , 1);")

      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "daxpy(x,y,size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;") 
	      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+size+" , 1);")
        p("_ini1(y_array[i],"+size+" , 1);")
        p("}")



        p("for(int r = 0; r < " + Config.repeats + "; r++){")
	p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("daxpy(x_array[i%numberofshifts], y_array[i%numberofshifts], size);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) x_array, numberofshifts);")
        p("DestroyBuffers( (void **) y_array, numberofshifts);")
      }
      else
      {
/*
        //run ity        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("dgemm(A,B,C, size);")
        //p("cblas_dgemm(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", B, 1, 0., C, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")
*/
      }
      p("}")

    }
    p("measurement_end();")
    p("}")
  }

  def fft_Spiral(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)


    val vectorized = double_precision //GO: FIXME!
    val prec = "double" // GO: miss using flag already for vectorization
    //val prec = if (double_precision) "double" else "float"

    p("#include <mkl.h>")
    p("#include <iostream>")
    p(Config.MeasuringCoreH)
    p("#define page 64")

    if (vectorized)
    {
      if (Config.isWin)
        p("#include \"C:\\Users\\ofgeorg\\fft_sse\\spiral_fft.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_sse\\spiral_private.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_sse\\spiral_private.c\"\n    #include \"C:\\Users\\ofgeorg\\fft_sse\\spiral_fft_double.c\"")
      else
        //p("#include \""+Config.home+"/fft_sse/spiral_fft.h\"\n    #include \""+Config.home+"/fft_sse/spiral_private.h\"\n    #include \""+Config.home+"/fft_sse/spiral_private.c\"\n    #include \""+Config.home+"/fft_sse/spiral_fft_double.c\"")
        p("#include \"/tmp/fft_sse/spiral_fft.h\"\n    #include \"/tmp/fft_sse/spiral_private.h\"\n    #include \"/tmp/fft_sse/spiral_private.c\"\n    #include \"/tmp/fft_sse/spiral_fft_double.c\"")

    }
    else
    {
      if (Config.isWin)
        p("#include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_fft.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_private.h\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_private.c\"\n    #include \"C:\\Users\\ofgeorg\\fft_scalar\\spiral_fft_double.c\"")
      else
        //p("#include \""+Config.home+"/fft_scalar/spiral_fft.h\"\n    #include \""+Config.home+"/fft_scalar/spiral_private.h\"\n    #include \""+Config.home+"/fft_scalar/spiral_private.c\"\n    #include \""+Config.home+"/fft_scalar/spiral_fft_double.c\"")
        p("#include \"/tmp/fft_scalar/spiral_fft.h\"\n    #include \"/tmp/fft_scalar/spiral_private.h\"\n    #include \"/tmp/fft_scalar/spiral_private.c\"\n    #include \"/tmp/fft_scalar/spiral_fft_double.c\"")

    }
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p(counterstring)
    p(initstring)

    for (size <- sizes)
    {
      if (size > 8192)
        assert(false,"Size not supported!!!!")

      p("{")

      p("spiral_status_t status;")
      p("std::string statusStr;")


      sourcefile.println()
      //Tune the number of runs

      p(prec + " * in = (" + prec + "*) _mm_malloc(" + 2*size + "* sizeof(" + prec + "),page);" )
      p(prec + " * out = (" + prec + "*) _mm_malloc(" + 2*size + "* sizeof(" + prec + "),page);" )
      p("_ini1(in,"+2*size+" ,1);")
      p("_ini1(out,"+2*size+" ,1);")
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"status = spiral_fft_double("+ size + ", 1, in, out);", "std::cout << out[0];" )
      //find out the number of shifts required
      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



      if (!warmData)
      {
        p("long size_per_run = " + 2*size + "* sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = (100 * 1024 * 1024)/size_per_run;")

        p("_mm_free(in);")
        p("_mm_free(out);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (2*2*size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2 * 2 * size) + "* sizeof(" + prec + ")));")

        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** in_array = (double **) CreateBuffers("+2*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** out_array = (double **) CreateBuffers("+2*size+"* sizeof(" + prec + "),numberofshifts);")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(in_array[i],"+2*size+" ,1);")
        p("_ini1(out_array[i],"+2*size+" ,1);")
        p("}")

        //corpse
        p("for(int i = 0; i < runs; i++){")
        p("status = spiral_fft_double("+ size + ", 1, in_array[i%numberofshifts], out_array[i%numberofshifts]);")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("status = spiral_fft_double("+ size + ", 1, in_array[i%numberofshifts], out_array[i%numberofshifts]);")
        p("}")
        p( "measurement_stop(runs);")
        //p("std::cout << out_array[0][0];")
        p("  switch (status) {\n    case SPIRAL_SIZE_NOT_SUPPORTED:\n    statusStr = \"SIZE_NOT_SUPPORTED\";\n    break;\n    case SPIRAL_INVALID_PARAM:\n      statusStr = \"SPIRAL_INVALID_PARAM\";\n    break;\n    case SPIRAL_OUT_OF_MEMORY:\n      statusStr = \"SPIRAL_OUT_OF_MEMORY\";\n    break;\n    case SPIRAL_OK:\n      statusStr = \"worked!\";\n    break;\n  }")
        p( " }")
        p("DestroyBuffers( (void **) in_array, numberofshifts);")
        p("DestroyBuffers( (void **) out_array, numberofshifts);")
      }
      else
      {
        //run it
        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("status = spiral_fft_double("+ size + ", 1, in, out);")
        p("}")
        p( "measurement_stop(runs);")
        //p("std::cout << out[0];")
        p("  switch (status) {\n    case SPIRAL_SIZE_NOT_SUPPORTED:\n    statusStr = \"SIZE_NOT_SUPPORTED\";\n    break;\n    case SPIRAL_INVALID_PARAM:\n      statusStr = \"SPIRAL_INVALID_PARAM\";\n    break;\n    case SPIRAL_OUT_OF_MEMORY:\n      statusStr = \"SPIRAL_OUT_OF_MEMORY\";\n    break;\n    case SPIRAL_OK:\n      statusStr = \"worked!\";\n    break;\n  }")
        p( " }")
        //p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(in);")
        p("_mm_free(out);")

      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }
   */

  /*
  def fft_FFTW(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)



    p("#include <iostream>")
    p(Config.MeasuringCoreH)
    sourcefile.println("k")
    sourcefile.println("#include <cstdio>")
    sourcefile.println("#include <stdlib.h>")

    p("#define page 64")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    //CodeGeneration.create_array_of_buffers(sourcefile) //special for FFTW


    //GO: Using special Create Buffers and Destroy Buffers here cause FFTW has its own malloc
    val prec =  "fftw_complex"
    p("void * CreateBuffers(long size, long numberofshifts)")
    p("{")
    p( prec + " ** bench_buffer = (" + prec + "**) _mm_malloc(numberofshifts*sizeof(" + prec + "*),page);" )
    p( "if (!bench_buffer) {\n      std::cout << \"malloc failed\";\n      measurement_end();\n      return ;} ")
    p("for(int i = 0; i < numberofshifts; i++){")
    p("bench_buffer[i] = (" + prec + "*) fftw_malloc(size * sizeof(fftw_complex));")
    p( "if (!bench_buffer[i]) {\n      std::cout << \"fftw_malloc failed\";\n      measurement_end();\n      return ;} ")
    p("}")
    p("return (void*)bench_buffer;")
    p("}")


    p("void DestroyBuffers(void ** bench_buffer, long numberofshifts) {")
    p("for(int i = 0; i < numberofshifts; i++)")
    p(" fftw_free(bench_buffer[i]);")
    p("_mm_free(bench_buffer);")
    p("}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p(counterstring)
    p(initstring)

    for (size <- sizes)
    {
      p("{")
      p("fftw_plan fftwPlan;")
      p("fftw_complex * in;")
      p("fftw_complex * out;")
      //p("std::cout << \"before fftwmalloc\";");
      p("in =  (fftw_complex*) fftw_malloc("+size+ " * sizeof(fftw_complex));")
      p("out =  (fftw_complex*) fftw_malloc("+size+ " * sizeof(fftw_complex));")
      //p("std::cout << \"before plan\";");
      p("fftwPlan = fftw_plan_dft_1d("+size+", in, out, FFTW_FORWARD, FFTW_MEASURE);")

      //p("std::cout << \"before tune\";");
      p("_ini1((double*)in,"+2*size+" ,1);")
      p("_ini1((double*)out,"+2*size+" ,1);")
      //tune nr runs
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"fftw_execute_dft(fftwPlan,in,out);", "std::cout << out[1];" )
      //find out the number of shifts required
      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



      if (!warmData)
      {
        p("long size_per_run = " + 2*size + "* sizeof(fftw_complex);")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = (100 * 1024 * 1024)/size_per_run;")


        p("fftw_free(in);")
	      p("fftw_free(out);")
        //allocate
        p("long numberofshifts =  measurement_getNumberOfShifts(" + 2*(size)+ "* sizeof(fftw_complex),runs*"+Config.repeats+");")

        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("fftw_complex ** in_array = (fftw_complex **) CreateBuffers("+size+"* sizeof(fftw_complex),numberofshifts);")
        p("fftw_complex ** out_array = (fftw_complex **) CreateBuffers("+size+"* sizeof(fftw_complex),numberofshifts);")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1((double*) in_array[i],"+2*size+" ,1);")
        p("_ini1((double*) out_array[i],"+2*size+" ,1);")

        p("}")

        //corpse
        p("for(int i = 0; i < runs; i++){")
        p("fftw_execute_dft(fftwPlan,in_array[i%numberofshifts],out_array[i%numberofshifts]);")
        p("}")


        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("fftw_execute_dft(fftwPlan,in_array[i%numberofshifts],out_array[i%numberofshifts]);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        p("DestroyBuffers( (void **) in_array, numberofshifts);")
        p("DestroyBuffers( (void **) out_array, numberofshifts);")

      }
      else
      {
        //run it
        //p("std::cout << \"before repeats\";");

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        	p("measurement_start();")
	        p("for(int i = 0; i < runs; i++){")
		        p("fftw_execute_dft(fftwPlan,in,out);")
	        p("}")
        	p( "measurement_stop(runs);")
        p( " }")
        //p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("fftw_free(in);")
	p("fftw_free(out);") 
      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }
  */


  /*
  def fft_NR(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    {
      def p(x: String) = sourcefile.println(x)
      val prec = if (double_precision) "double" else "float"

      p("#include <mkl.h>")
      p("#include <iostream>")
      p(Config.MeasuringCoreH)

      p("/************************************************\n* FFT code from the book Numerical Recipes in C *\n* Visit www.nr.com for the licence.             *\n************************************************/\n\n// The following line must be defined before including math.h to correctly define M_PI\n#define _USE_MATH_DEFINES\n#include <math.h>\n#include <stdio.h>\n#include <stdlib.h>\n\n#define PI\tM_PI\t/* pi to machine precision, defined in math.h */\n#define TWOPI\t(2.0*PI)\n\n/*\n FFT/IFFT routine. (see pages 507-508 of Numerical Recipes in C)\n\n Inputs:\n\tdata[] : array of complex* data points of size 2*NFFT+1.\n\t\tdata[0] is unused,\n\t\t* the n'th complex number x(n), for 0 <= n <= length(x)-1, is stored as:\n\t\t\tdata[2*n+1] = real(x(n))\n\t\t\tdata[2*n+2] = imag(x(n))\n\t\tif length(Nx) < NFFT, the remainder of the array must be padded with zeros\n\n\tnn : FFT order NFFT. This MUST be a power of 2 and >= length(x).\n\tisign:  if set to 1, \n\t\t\t\tcomputes the forward FFT\n\t\t\tif set to -1, \n\t\t\t\tcomputes Inverse FFT - in this case the output values have\n\t\t\t\tto be manually normalized by multiplying with 1/NFFT.\n Outputs:\n\tdata[] : The FFT or IFFT results are stored in data, overwriting the input.\n*/\n\nvoid four1(double data[], int nn, int isign)\n{\n    int n, mmax, m, j, istep, i;\n    double wtemp, wr, wpr, wpi, wi, theta;\n    double tempr, tempi;\n    \n    n = nn << 1;\n    j = 1;\n    for (i = 1; i < n; i += 2) {\n\tif (j > i) {\n\t    tempr = data[j];     data[j] = data[i];     data[i] = tempr;\n\t    tempr = data[j+1]; data[j+1] = data[i+1]; data[i+1] = tempr;\n\t}\n\tm = n >> 1;\n\twhile (m >= 2 && j > m) {\n\t    j -= m;\n\t    m >>= 1;\n\t}\n\tj += m;\n    }\n    mmax = 2;\n    while (n > mmax) {\n\tistep = 2*mmax;\n\ttheta = TWOPI/(isign*mmax);\n\twtemp = sin(0.5*theta);\n\twpr = -2.0*wtemp*wtemp;\n\twpi = sin(theta);\n\twr = 1.0;\n\twi = 0.0;\n\tfor (m = 1; m < mmax; m += 2) {\n\t    for (i = m; i <= n; i += istep) {\n\t\tj =i + mmax;\n\t\ttempr = wr*data[j]   - wi*data[j+1];\n\t\ttempi = wr*data[j+1] + wi*data[j];\n\t\tdata[j]   = data[i]   - tempr;\n\t\tdata[j+1] = data[i+1] - tempi;\n\t\tdata[i] += tempr;\n\t\tdata[i+1] += tempi;\n\t    }\n\t    wr = (wtemp = wr)*wpr - wi*wpi + wr;\n\t    wi = wi*wpr + wtemp*wpi + wi;\n\t}\n\tmmax = istep;\n    }\n}")
      p("#define page 64")
      val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
      CodeGeneration.create_array_of_buffers(sourcefile)
      CodeGeneration.destroy_array_of_buffers(sourcefile)
      p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
      p("int main () { ")
      p(counterstring)
      p(initstring)

      for (size <- sizes)
      {
        p("{")

        //Tune the number of runs
        p(prec + " * x = (" + prec + "*) _mm_malloc(" + 2*size+1 + "* sizeof(" + prec + "),page);" ) //Note the +1 - strange NR behaviour
        p("_ini1(x,"+2*size+1+" ,1);")
        CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"four1(x,"+size+",1);", "std::cout << x[1];" )
        //find out the number of shifts required
        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



        if (!warmData)
        {
          p("long size_per_run = " + 2*size+1 + "* sizeof(" + prec + ");")
          p("if(runs * size_per_run < (100 * 1024 * 1024))")
          p("runs = (100 * 1024 * 1024)/size_per_run;")

          p("_mm_free(x);")
          //allocate
          //p("long numberofshifts =  measurement_getNumberOfShifts(" + (2*size+1)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
          p("long numberofshifts =   (100 * 1024 * 1024 / (" + (2*size+1)+ "* sizeof(" + prec + ")));")

          //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
          p("double ** x_array = (double **) CreateBuffers("+2*size+1+"* sizeof(" + prec + "),numberofshifts);")

          p("for(int i = 0; i < numberofshifts; i++){")
          p("_ini1(x_array[i],"+2*size+1+" ,1);")
          p("}")

          p("for(int i = 0; i < runs; i++){")
          p("four1(x_array[i%numberofshifts],"+size+",1);")
          p("}")


          p("for(int r = 0; r < " + Config.repeats + "; r++){")
          p("measurement_start();")
          p("for(int i = 0; i < runs; i++){")
          p("four1(x_array[i%numberofshifts],"+size+",1);")
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
          p("four1(x,"+size+",1);")
          p("}")
          p( "measurement_stop(runs);")
          p( " }")
          //p("std::cout << \"deallocate\";")
          //deallocate the buffers
          p("_mm_free(x);")

        }
        p("}")
      }
      p("measurement_end();")
      p("}")
    }
  }


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


  def memonly5 (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
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

        p("for(long i = 0; i < size; i=i+4096){")
        p("A[i] = A[ (size-1) - i]; ")
        p("}")

      }

      p("measurement_start();")
      p("for(long i = 0; i < size; i=i+4096){")
        p("for(long j = 0; j < 600 && j+i < size; j=j+4096){")
        p("A[j] = A[ (size-1) - j]; ")
      p("}")
      p("}")
      p("measurement_stop(runs);")
      p("_mm_free(A);")

      p("}")
      p("}")
    }
    p("measurement_end();")
    p("}")
  }


  def dgemm(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)


    val prec = if (double_precision) "double" else "float"
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")
      p("const int size = " +size + ";")
      //allocate
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * B = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * C = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")


      p("_ini1(A,"+size+" ,"+size+");")
      p("_ini1(B,"+size+" ,"+size+");")
      p("_ini1(C,"+size+" ,"+size+");")


      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile, "cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1.0,A, size,B, size,1.0,C, size);" ,"" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("_mm_free(A);")
        p("_mm_free(B);")
        p("_mm_free(C);")

        p("long size_per_run = " +size + ";")
        p(" size_per_run = size_per_run *  size_per_run * 3 * sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = ceil((100 * 1024 * 1024)/size_per_run);")

        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size*size*3)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (size*size*3)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")

        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** B_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** C_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("}")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(B_array[i],"+size+" ,"+size+");")
        p("}")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(C_array[i],"+size+" ,"+size+");")
        p("}")

        //corpses
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1.0,A_array[i%numberofshifts], size,B_array[i%numberofshifts], size,1.0,C_array[i%numberofshifts], size);")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemm(CblasRowMajor,CblasNoTrans, CblasNoTrans, size, size , size,1.0,A_array[i%numberofshifts], size,B_array[i%numberofshifts], size,1.0,C_array[i%numberofshifts], size);")
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
        //p("std::cout << \"deallocate\";")
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


  def dgemm_Atlas(sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {

    sourcefile.println("#ifdef __cplusplus\nextern \"C\" {\n#endif")
    sourcefile.println("#include <cblas.h>")
    sourcefile.println("#include <clapack.h>")

    sourcefile.println("#ifdef __cplusplus\n}\n#endif")
    dgemm(sourcefile ,sizes, counters, double_precision, warmData)
  }


  def dgemm_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    sourcefile.println("#include <mkl.h>")
    dgemm(sourcefile ,sizes, counters, double_precision, warmData)
  }



  def daxpy_MKL (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"
    p("#include <mkl.h>")
    p("#include <iostream>")
    p("#include <iostream>\n#include <fstream>\n#include <cstdlib>\n#include <ctime>\n#include <cmath>\n")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")

    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p("srand(1984);")

    p(counterstring)
    p(initstring)
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")

      //allocate
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("_ini1(x,"+size+" ,1);")
      p("_ini1(y,"+size+" ,1);")





      p("int n = " +size + ";")
      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"cblas_daxpy("+size+", alpha, x, 1, y, 1);","" )

      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {

        p("long size_per_run = " + 2*size + "l* sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = (100 * 1024 * 1024)/size_per_run;")

        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (size+size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")
        p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+size+" ,1);")
        p("}")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(y_array[i],"+size+" ,1);")
        p("}")



        //corpses
        p("for(int i = 0; i < runs; i++){")
        p("cblas_daxpy("+size+", alpha, x_array[i%numberofshifts], 1, y_array[i%numberofshifts], 1);")
        p("}")

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
      //  p("std::cout << \"deallocate\";")
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
    p("#define page 64")
    p("#define THRESHOLD " + Config.testDerivate_Threshold)
    p("using namespace std;")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)


    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")

    p("int main () { ")
    p(counterstring)
    p(initstring)
    p("srand(1984);")
    for (size <- sizes)
    {
      p("{")
      p("double alpha = 1.1;")

      //allocate
      p("double * A = (double *) _mm_malloc("+size*size+"*sizeof(double),page);")
      p("double * x = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("double * y = (double *) _mm_malloc("+size+"*sizeof(double),page);")
      p("int n = " +size + ";")



      p("_ini1(A,"+size+" ,"+size+");")
      p("_ini1(x,"+size+" ,1);")
      p("_ini1(y,"+size+" ,1);")



      //Tune the number of runs
      //p("std::cout << \"tuning\";")
      //tuneNrRuns(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 0., y, 1);","" )
      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 1., y, 1);","" )
      //find out the number of shifts required
      //p("std::cout << runs << \"allocate\";")
      //allocate the buffers
      //p("std::cout << \"run\";")
      if (!warmData)
      {
        p("long size_per_run = " + 2*size+size*size + "* sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = (100 * 1024 * 1024)/size_per_run;")

        p("_mm_free(A);")
        p("_mm_free(x);")
        p("_mm_free(y);")
        //allocate
        //p("long numberofshifts =  100*measurement_getNumberOfShifts(" + (size*size+size+size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / ( " + (size*size+size+size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")
        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")

        p("double ** A_array = (double **) CreateBuffers("+size*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** x_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+size+"* sizeof(" + prec + "),numberofshifts);")


        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(A_array[i],"+size+" ,"+size+");")
        p("_ini1(x_array[i],"+size+" ,1);")
        p("_ini1(y_array[i],"+size+" ,1);")
        p("}")



        //corpse
        /*p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A_array[i%numberofshifts], "+size+", x_array[i%numberofshifts], 1, 1.1, y_array[i%numberofshifts], 1);")
        p("}")*/

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A_array[i%numberofshifts], "+size+", x_array[i%numberofshifts], 1, 1.1, y_array[i%numberofshifts], 1);")
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
        p("runs = 1;")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("cblas_dgemv(CblasRowMajor, CblasNoTrans,"+size+" ,"+size+", alpha, A, "+size+", x, 1, 1.1, y, 1);")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        //p("std::cout << \"deallocate\";")
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

  def fft_MKL_outplace (sourcefile: PrintStream,sizes: List[Long], counters: Array[HWCounters.Counter], double_precision: Boolean = true, warmData: Boolean = false) =
  {
    def p(x: String) = sourcefile.println(x)
    val prec = if (double_precision) "double" else "float"

    p("#include <mkl.h>")
    p("#include <iostream>")
    p(Config.MeasuringCoreH)
    p("#define page 64")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p("srand(1984);")
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
      p(prec + " * y = (" + prec + "*) _mm_malloc(" + 2*size + "* sizeof(" + prec + "),page);" )
      p("_ini1(x,"+2*size+" ,1);")
      p("_ini1(y,"+2*size+" ,1);")

      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"status = DftiComputeForward(mklDescriptor, x);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}", "std::cout << x[0];" )
      //find out the number of shifts required
      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



      if (!warmData)
      {
        p("_mm_free(x);")
        p("_mm_free(y);")
        p("long size_per_run = " + 4*size + "* sizeof(" + prec + ");")
        p("if(runs * size_per_run < (100 * 1024 * 1024))")
        p("runs = (100 * 1024 * 1024)/size_per_run;")

        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (2*size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "*2* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")
        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("double ** x_array = (double **) CreateBuffers("+2*size+"* sizeof(" + prec + "),numberofshifts);")
        p("double ** y_array = (double **) CreateBuffers("+2*size+"* sizeof(" + prec + "),numberofshifts);")
        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+2*size+" ,1);")
        p("_ini1(y_array[i],"+2*size+" ,1);")
        p("}")

        p("for(int i = 0; i < runs; i++){")
        p("status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts], y_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}")
        p("}")

        p("for(int r = 0; r < " + Config.repeats + "; r++){")
        p("measurement_start();")
        p("for(int i = 0; i < runs; i++){")
        p("status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts], y_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}")
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
        p("status = DftiComputeForward(mklDescriptor, x,y);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}")
        p("}")
        p( "measurement_stop(runs);")
        p( " }")
        //p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(x);")

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
    p("#define page 64")
    val (counterstring, initstring ) = CodeGeneration.Counters2CCode(counters)
    CodeGeneration.create_array_of_buffers(sourcefile)
    CodeGeneration.destroy_array_of_buffers(sourcefile)
    p("void _rands(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;\n}")
    p("void _ini1(double * m, size_t row, size_t col)\n{\n  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)1.1;\n}")
    p("int main () { ")
    p("srand(1984);")
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
      p("_ini1(x,"+2*size+" ,1);")

      CodeGeneration.tuneNrRunsbyRunTime(sourcefile,"status = DftiComputeForward(mklDescriptor, x);\n\tif (status != 0) {\n\t\tstd::cout << \"status -1\";\nreturn -1;\n\t}", "std::cout << x[0];" )
      //find out the number of shifts required
      //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")



      if (!warmData)
      {
        p("_mm_free(x);")
        //allocate
        //p("long numberofshifts =  measurement_getNumberOfShifts(" + (2*size)+ "* sizeof(" + prec + "),runs*"+Config.repeats+");")
        p("long numberofshifts = (100 * 1024 * 1024 / (" + (2*size)+ "* sizeof(" + prec + ")));")
        p("if (numberofshifts < 2) numberofshifts = 2;")
        //p("std::cout << \" Shifts: \" << numberofshifts << \" --\"; ")
        p("double ** x_array = (double **) CreateBuffers("+2*size+"*2* sizeof(" + prec + "),numberofshifts);")

        p("for(int i = 0; i < numberofshifts; i++){")
        p("_ini1(x_array[i],"+2*size+" ,1);")
        p("}")

        //corpse
        p("for(int i = 0; i < runs; i++){")
        p("status = DftiComputeForward(mklDescriptor, x_array[i%numberofshifts]);\n\tif (status != 0) {\n\t\t std::cout << \"status -1\";\nreturn -1;\n\t}")
        p("}")


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
        //p("std::cout << \"deallocate\";")
        //deallocate the buffers
        p("_mm_free(x);")

      }
      p("}")
    }
    p("measurement_end();")
    p("}")
  }


  def check( kernel: (PrintStream, List[Long],  Array[HWCounters.Counter],  Boolean,  Boolean ) => Unit,
            sizes_in: List[Long],name: String, counters: Array[HWCounters.Counter],double_precision: Boolean = true, warmData: Boolean = false, flags: String) =
  {
    for (s <- sizes_in){
    val sizes: List[Long] = List(s)
    def single_kernel(sourcefile: PrintStream) = kernel (sourcefile: PrintStream,sizes, counters, double_precision, warmData)
    val kernel_res = CommandService.fromScratch(name, single_kernel, flags)
    kernel_res.prettyprint()
    }

  }
  def run_kernel (path: File,kernel: (PrintStream, List[Long],  Array[HWCounters.Counter],  Boolean,  Boolean ) => Unit,
                  sizes_in: List[Long],name: String, counters: Array[HWCounters.Counter],double_precision: Boolean = true, warmData: Boolean = false, flags: String) =
  {

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
      for (s <- sizes_in)
      {
        //this way we do a single measurment setup for each size
        val sizes: List[Long] = List(s)
        def single_kernel(sourcefile: PrintStream) = kernel (sourcefile: PrintStream,sizes, counters, double_precision, warmData)
        val kernel_res = CommandService.fromScratch(name, single_kernel, flags)
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

  // run kernel with flops/tlb counters. Gets all the data needed for the plots
  def run_kernel2 (path: File, kernel: (PrintStream, List[Long], Array[HWCounters.Counter], Boolean, Boolean, Boolean) => Unit,
                     sizes_in: List[Long], name: String, double_precision: Boolean = true, warmData: Boolean = false, warmTLB: Boolean = false, flags: String) =
    {
        val file = new File(path.getPath + File.separator + "flop_"+ name + ".txt")

        if(!file.exists())
        {
            val outputFile1 = new PrintStream(path.getPath + File.separator + "flop_"+ name + ".txt")
            val outputFile2 = new PrintStream(path.getPath + File.separator + "tsc_"+ name + ".txt")
            val outputFile3 = new PrintStream(path.getPath + File.separator + "size_"+ name + ".txt")
            val outputFile4 = new PrintStream(path.getPath + File.separator + "bytes_transferred_" + name + ".txt")
            val outputFile5 = new PrintStream(path.getPath + File.separator + "tlb_misses_" + name + ".txt")

            var first1 = true
            for (s <- sizes_in)
            {
                //this way we do a single measurment setup for each size
                val sizes: List[Long] = List(s)

                def kernelTLB(sourcefile: PrintStream) = kernel (sourcefile: PrintStream, sizes, IvyBridge.tlbs, double_precision, warmData, warmTLB)
                def kernelFlops(sourcefile: PrintStream) = kernel (sourcefile: PrintStream, sizes, IvyBridge.flops, double_precision, warmData, warmTLB)

                val tlb_res = CommandService.fromScratch(name, kernelTLB, flags)
                val flops_res = CommandService.fromScratch(name, kernelFlops, flags)

                var first = true
                for (i <- 0 until Config.repeats)
                {
                    if (!first)
                    {
                        outputFile1.print(" ")
                        outputFile2.print(" ")
                        outputFile4.print(" ")
                        outputFile5.print(" ")
                    }

                    first = false

                    outputFile1.print(flops_res.getSCounter(0)(i) + flops_res.getSCounter(1)(i) + flops_res.getSCounter(2)(i)*2 + flops_res.getSCounter(3)(i)*4)
                    outputFile2.print(flops_res.getTSC(i))
                    outputFile4.print(flops_res.getbytes_transferred(i))
                    outputFile5.print(tlb_res.getSCounter(0)(i) + tlb_res.getSCounter(1)(i) + tlb_res.getSCounter(2)(i) + tlb_res.getSCounter(3)(i))
                }

                outputFile1.print("\n")
                outputFile2.print("\n")
                outputFile4.print("\n")
                outputFile5.print("\n")

                if (first1)
                {
                    outputFile3.print(s)
                    first1 = false
                }
                else
                    outputFile3.print(" " + s)
            }

            outputFile1.close()
            outputFile2.close()
            outputFile3.close()
            outputFile4.close()
            outputFile5.close()
        }
        else
            println(name + " read from cached file")
    }



*/




}
