-----------------------------------------------------------------------
  Install PCM and the library for measuring performance counters 
-----------------------------------------------------------------------

TODO



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


 
