-----------------------------------------------------------------------
 Background
-----------------------------------------------------------------------
[Background information and paper for this software](http://spiral.net/software/roofline.html)

-----------------------------------------------------------------------
 Reference for Citing
-----------------------------------------------------------------------

Georg Ofenbeck, Ruedi Steinmann, Victoria Caparros, Daniele G. Spampinato and Markus Püschel 
Applying the Roofline Model 
Proc. IEEE International Symposium on Performance Analysis of Systems and Software (ISPASS), 2014

-----------------------------------------------------------------------
 About the Software
-----------------------------------------------------------------------

Perfplot is a collection of scripts and tools that allow a user to instrument performance counters on a recent Intel platform, measure them and use the results to generate roofline and performance plots.

The code is divided into three parts:
  - modified version of Intel PCM
    (original here: http://software.intel.com/en-us/articles/intel-performance-counter-monitor-a-better-way-to-measure-cpu-utilization)
  - Scala scripts to compile code with PCM / collect data / prepare data for python plot scripts
  - python plot scripts
 
 The scala and the python part are optional.



-----------------------------------------------------------------------
  Install PCM
-----------------------------------------------------------------------

First make sure that PCM works on your system.
To do so go into the folder perfplot/pcm and follow the instructions for your OS.

Short version for linux:
  cd perfplot/pcm
  make
  ./pcm.x 1
 
  This will build and run the original pcm and run it outputting the timing at fixed intervals.
  If you see the output the pcm is ready to use. Otherwise resolve the issues as output by the program.
  
  If it fails to execute even after you fixed the permissons, try if it will work using root.
  If so it might be a problem that seems to occur with a recent kernel patch in linux - at the very bottom of this document is a suggested fix.
 
-----------------------------------------------------------------------
  Using the modified PCM
-----------------------------------------------------------------------  
 
The make file will also generate a .lib
This is the modified version of PCM that is enriched with the following functions:

int measurement_init(long * custom_counters , unsigned long offcore_response0 , unsigned long offcore_response1 );
void measurement_start();
void measurement_stop(unsigned long runs);
void measurement_end();  


To use the library instrument your code such that at some point it calls init,
then as many start/stop pairs as required and finally calls end.

Init takes an array of 8 elements of long that describe the counters you want to use.
E.g. for Flops for Sandybridge from the Intel manual:
    "10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed."
    "10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed."
    "10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed."
    "11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions."

    should be passed as:
     10H,01H,10H,80H etc. etc.
     
Offcore parameters are used if offcore response should be queried (see intel manual for codes) - but can be ignored for most users. (passing 0 ignores them)

Every cycle of start/stop will query the counters and put them internally into a list (divided by #runs passed in stop)
This list will get dumped into many files once end is called.

There will be a txt file per Core and Counter containing all numbers separated.

-----------------------------------------------------------------------
  Using the scala scripts
-----------------------------------------------------------------------  
     
The scala scripts do exactly the steps described before in an automatic way:
  - compiling and linking a source file with the modified PCM.
  - making sure that sufficient runs are done
  - run in temporary folders - collect the data
  - collect several runs into a format that the python plot scripts expect
 
To use run sbt within the root folder. (http://www.scala-sbt.org/)
Look at the example in test-src/Example.scala

You can run the example by running
"test-only Example" within sbt  

I tried to comment more in this file and also in the main function it utilizes which can be found in:

src/CodeGeneration.scala

under
def Example (....)

Reading this function and the Example.scala should hopefully get you going.


To instrument your own code modify CodeGeneration.scala and add your own function there.


Change Example.scala accordingly

 
The script will output the results of a run within a folder specified in the script.
These results are then used in the python scripts.


Caveats to watch out for:
- Always make sure that PCM is working (after every reboot/ after every time your program crashes) by running ./pcm.x 1 in the pcm folder before running the scala script
- if the timed program crashes the scala script will still wait for the result files looping endless - you will see an errror - terminate with ctrl+C




-----------------------------------------------------------------------
  Using the python scripts
-----------------------------------------------------------------------  
The rooflines themselfs are hardcoded atm in the pythonscript - so adjust them as you need (bandwidth and peak performance).

Add the name of the series you would like to be plotted (see example) and just run (within the same folder then the result)

-----------------------------------------------------------------------
             Install Python 2.7 (to generate the plots)
-----------------------------------------------------------------------

For the plotting utility, you need to have python2.7.
If you have it already, you can skip this section and
install matplolib directly.
If you have another version of python and would like to keep
it, you can use 'virtualenv' to create and manage different
Python enviroments. That way, you can keep your preferred
Python version, and use 2.7 only when needed.

Instructions for installing Python2.7 with virtualenv:

- Install python2.7 in your local directory

- Create a directory where virtualenv will install
  all the libraries and binaries associated with the
  different python installations:

  For example:
         mkdir virtualenv
          cd virtualenv
          mkdir 2.7.3   //In case you want to have more versions in the future  

- Download this python script

     https://raw.github.com/pypa/virtualenv/master/virtualenv.py  
 
  and execute it typing

      /PATH_TO_YOUR_LOCAL_PYTHON2.7 virtualenv.py PATH_TO_VIRTUALENV/virtualenv/2.7.3

    IMPORTANT: to facilitate the rest of installation process,
    make sure that the python you use to runthe  virtualenv script is the
    python2.7 you just installed locally.

- The previous script should have created the directories
  PATH_TO_VIRTUALENV/virtualenv/2.7.3/lib/python2.7/site-packages/ and
  PATH_TO_VIRTUALENV/virtualenv/2.7.3/bin

- Activate the python2.7 environment:

       PATH_TO_VIRTUALENV/virtualenv/2.7.3/bin/activate

  If you want to activate this enviroment permanently, to can
  update your ~/.bashprofile and add

       source PATH_TO_VIRTUALENV/virtualenv/2.7.3/bin/activate

  After activating, you should see the python version in your prompt:
    
    For example:
    (2.7.3)[caparrov@rho ~]$
   
   You can also check which python version you are using by typing:

    python --version



-----------------------------------------------------------------------
         Install Matplotlib (to generate the plots)
-----------------------------------------------------------------------

There are several ways of installing matplotlib depending on you OS, and a
lot of documentation can be found on the website:

    http://matplotlib.org/users/installing.html

The most important thing is that you make sure you have installed
scipy and numpy libraries (needed by matplotlib)

    http://www.scipy.org/Installing_SciPy
    
IMPORTANT: If you are using python with virtualenv, when you do manual installs,
make sure that you provide the right path for python.

   For example:
     python setup.py install --prefix=PATH_TO_VIRTUALEVN/virtualenv/2.7.3


At the end of the installation process, you should have the following directories:

PATH_TO_VIRTUALENV/virtualenv/2.7.3/lib/python2.7/site-packages/scipy/
PATH_TO_VIRTUALENV/virtualenv/2.7.3/lib/python2.7/site-packages/numpy/
PATH_TO_VIRTUALENV/virtualenv/2.7.3/lib/python2.7/site-packages/matplotlib/
