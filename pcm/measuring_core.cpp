/*
Copyright (c) 2009-2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
// written by Roman Dementiev,
//            Thomas Willhalm,
//            Patrick Ungerer


/*!     \file cpucounterstest.cpp
        \brief Example of using CPU counters: implements a simple performance counter monitoring utility
*/
#define HACK_TO_REMOVE_DUPLICATE_ERROR 
#include <iostream>
#include <fstream>
#include <list>
#ifdef _MSC_VER
#pragma warning(disable : 4996) // for sprintf
#include <windows.h>
#include "PCM_Win/windriver.h"
#else
#include <unistd.h>
#include <signal.h>
#endif
#include <math.h>
#include <iomanip>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <string>
#include <assert.h>
#include "cpucounters.h"
#include "measuring_core.h"



#define SIZE (10000000)
#define DELAY 1 // in seconds

using namespace std;



template <class IntType>
std::string unit_format(IntType n)
{
    char buffer[1024];
    if (n <= 9999ULL)
    {
        sprintf(buffer, "%4d  ", int32(n));
        return buffer;
    }
    if (n <= 9999999ULL)
    {
        sprintf(buffer, "%4d K", int32(n / 1000ULL));
        return buffer;
    }
    if (n <= 9999999999ULL)
    {
        sprintf(buffer, "%4d M", int32(n / 1000000ULL));
        return buffer;
    }
    if (n <= 9999999999999ULL)
    {
        sprintf(buffer, "%4d G", int32(n / 1000000000ULL));
        return buffer;
    }

    sprintf(buffer, "%4d T", int32(n / (1000000000ULL * 1000ULL)));
    return buffer;
}


template <class IntType>
double float_format(IntType n)
{
	return double(n)/1024/1024;
}

std::string temp_format(int32 t)
{
    char buffer[1024];
    if (t == PCM_INVALID_THERMAL_HEADROOM)
        return "N/A";

    sprintf(buffer, "%2d", t);
    return buffer;
}

void print_help(char * prog_name)
{
        #ifdef _MSC_VER
    cout << " Usage: pcm <delay>|\"external_program parameters\"|--help|--uninstallDriver|--installDriver <other options>" << endl;
        #else
    cout << " Usage: pcm <delay>|\"external_program parameters\"|--help <other options>" << endl;
        #endif
    cout << endl;
    cout << " \n Other options:" << endl;
    cout << " -nc or --nocores or /nc => hides core related output" << endl;
    cout << " -ns or --nosockets or /ns => hides socket related output" << endl;
    cout << " -nsys or --nosystem or /nsys => hides system related output" << endl;
    cout << " -csv or /csv => print compact csv format" << endl;
    cout << " Example:  pcm.x 1 -nc -ns " << endl;
    cout << endl;
}


#ifdef _MSC_VER
BOOL cleanup(DWORD)
{
    PCM::getInstance()->cleanup();
    return FALSE;
}
#else
void cleanup(int s)
{
    signal(s, SIG_IGN);
    PCM::getInstance()->cleanup();
    exit(0);
}
#endif


PCM * m ;

CoreCounterState * cstates1;
CoreCounterState * cstates2;
SocketCounterState * sktstate1;
SocketCounterState * sktstate2;
SystemCounterState *sstate1;
SystemCounterState *sstate2;

long cycles_a, cycles_b;

 ofstream flog;

 std::streambuf *coutbuf;



 bool gflushData = false;
 bool gflushICache = false;
 bool gflushTLB = false;

 
 list<uint64> *plists0;
 list<uint64> *plists1;
 list<uint64> *plists2;
 list<uint64> *plists3;
 list<uint64> *plists4;
 list<uint64> *plists5;
 list<uint64> *plists6;
 list<uint64> *plists7;
 list<uint64> *plist_cycles;
 list<uint64> *plist_refcycles;
 list<uint64> *plist_tsc;

 list<uint64> *plist_mcread;
 list<uint64> *plist_mcwrite;


 ofstream * fplist0;
 ofstream * fplist1;
 ofstream * fplist2;
 ofstream * fplist3;
 ofstream * fplist4;
 ofstream * fplist5;
 ofstream * fplist6;
 ofstream * fplist7;
 ofstream * fplist_cycles;
 ofstream * fplist_refcycles;
 ofstream * fplist_tsc;
 ofstream * fplist_mcread;
 ofstream * fplist_mcwrite;
 


int flushTLB()
{
	
}


int flushICache()
{

}


int flushCache()
{
	//GO: TODO - get LLC cache size from CPUID
	long size = 14 * 1024 * 1024; //14 MB
	double * buffer = (double *) malloc(size);
	double result = 0;
	for (int i = 0; i < size; i=i+4)
	{
		result += buffer[i];
	}
	for (int i = 0; i < size; i=i+4)
	{
		result += buffer[i];
	}
	free(buffer);
	std::cout << "flushed cache"<< result << endl;
}



int perfmon_init(int type, bool flushData = false, bool flushICache = false, bool flushTLB = false)
{
        gflushData = flushData;
        gflushICache = flushICache;
        gflushTLB = flushTLB;
	
	flog.open("log.txt");	 
	coutbuf = std::cout.rdbuf(); //save old buf
	std::cout.rdbuf(flog.rdbuf());
	

	cout << "Starting log.\n";  
	cout << endl;
	cout << " Using: Intel(r) Performance Counter Monitor "<< INTEL_PCM_VERSION << endl;
	cout << endl;
	cout << " Copyright (c) 2009-2012 Intel Corporation" << endl;
	cout << endl;


	
	    #ifdef _MSC_VER
    // Increase the priority a bit to improve context switching delays on Windows
    SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_ABOVE_NORMAL);

    TCHAR driverPath[1024];
    GetCurrentDirectory(1024, driverPath);
    wcscat(driverPath, L"\\msr.sys");

    //SetConsoleCtrlHandler((PHANDLER_ROUTINE)cleanup, TRUE);
        #else
    signal(SIGINT, cleanup);
    signal(SIGKILL, cleanup);
    signal(SIGTERM, cleanup);
        #endif

        #ifdef _MSC_VER
    // WARNING: This driver code (msr.sys) is only for testing purposes, not for production use
    Driver drv;
    // drv.stop();     // restart driver (usually not needed)
    if (!drv.start(driverPath))
    {
		cout << "Cannot access CPU counters" << endl;
		cout << "You must have signed msr.sys driver in your current directory and have administrator rights to run this program" << endl;
    }
        #endif

    m = PCM::getInstance();

	PCM::ErrorCode status;
	switch (type)
	{
	case 0:
		status = m->program();
		break;

	case 1:		
		//GO: FLOP (Double) Events
		{
			
			PCM::CustomCoreEventDescription events[8];	
			events[0].event_number = FP_COMP_OPS_EXE_SSE_SCALAR_DOUBLE_EVTNR;
			events[0].umask_value = FP_COMP_OPS_EXE_SSE_SCALAR_DOUBLE_UMASK;	

			events[1].event_number = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_EVTNR;
			events[1].umask_value = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_UMASK;

			events[2].event_number = SIMD_FP_256_PACKED_DOUBLE_EVTNR;
			events[2].umask_value = SIMD_FP_256_PACKED_DOUBLE_UMASK;

			events[3].event_number = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_EVTNR;
			events[3].umask_value = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_UMASK;	

			events[4].event_number = FP_COMP_OPS_EXE_SSE_PACKED_SINGLE_EVTNR;
			events[4].umask_value = FP_COMP_OPS_EXE_SSE_PACKED_SINGLE_UMASK;

			events[5].event_number = SIMD_FP_256_PACKED_SINGLE_EVTNR;
			events[5].umask_value = SIMD_FP_256_PACKED_SINGLE_UMASK;

			events[6].event_number = ARCH_LLC_REFERENCE_EVTNR;
			events[6].umask_value =  ARCH_LLC_REFERENCE_UMASK;

			events[7].event_number = ARCH_LLC_MISS_EVTNR;
			events[7].umask_value = ARCH_LLC_MISS_UMASK;
			

			status = m->program(PCM::CUSTOM_CORE_EVENTS,events);
		}
		break;
/*	case 1:		
		//GO: FLOP (Double) Events
		{
			
			PCM::CustomCoreEventDescription events[4];	
			events[0].event_number = FP_COMP_OPS_EXE_SSE_SCALAR_DOUBLE_EVTNR;
			events[0].umask_value = FP_COMP_OPS_EXE_SSE_SCALAR_DOUBLE_UMASK;	

			events[1].event_number = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_EVTNR;
			events[1].umask_value = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_UMASK;

			events[2].event_number = SIMD_FP_256_PACKED_DOUBLE_EVTNR;
			events[2].umask_value = SIMD_FP_256_PACKED_DOUBLE_UMASK;

			events[3].event_number = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_EVTNR; //event 3 needs to be set to something usefull
			events[3].umask_value = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_UMASK;

			status = m->program(PCM::CUSTOM_CORE_EVENTS,events);
		}
		break;*/
	case 2: 
		//GO: FLOP (Single) Events
		{
			PCM::CustomCoreEventDescription events[4];	
			events[0].event_number = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_EVTNR;
			events[0].umask_value = FP_COMP_OPS_EXE_SSE_FP_SCALAR_SINGLE_UMASK;	

			events[1].event_number = FP_COMP_OPS_EXE_SSE_PACKED_SINGLE_EVTNR;
			events[1].umask_value = FP_COMP_OPS_EXE_SSE_PACKED_SINGLE_UMASK;

			events[2].event_number = SIMD_FP_256_PACKED_SINGLE_EVTNR;
			events[2].umask_value = SIMD_FP_256_PACKED_SINGLE_UMASK;

			events[3].event_number = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_EVTNR; //event 3 needs to be set to something usefull
			events[3].umask_value = FP_COMP_OPS_EXE_SSE_FP_PACKED_DOUBLE_UMASK;

			status = m->program(PCM::CUSTOM_CORE_EVENTS,events);
		}
		break;

	case 3: 
		//GO: FLOP (Single) Events
		{
			PCM::CustomCoreEventDescription events[4];	
			events[0].event_number = ARCH_LLC_REFERENCE_EVTNR;
			events[0].umask_value =  ARCH_LLC_REFERENCE_UMASK;

			events[1].event_number = ARCH_LLC_MISS_EVTNR;
			events[1].umask_value = ARCH_LLC_MISS_UMASK;

			events[2].event_number = UNC_L3_MISS_ANY_EVTNR;
			events[2].umask_value = UNC_L3_MISS_ANY_UMASK;

			events[3].event_number = MEM_LOAD_RETIRED_L2_HIT_EVTNR;
			events[3].umask_value = MEM_LOAD_RETIRED_L2_HIT_UMASK;

			status = m->program(PCM::CUSTOM_CORE_EVENTS,events);
		}
		break;



	default:
		cout << "Unknown Measurement Type in call to perfmon_init" << endl;
        return -1;
	}

    
    switch (status)
    {
    case PCM::Success:
        break;
    case PCM::MSRAccessDenied:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (no MSR or PCI CFG space access)." << endl;
        return -1;
    case PCM::PMUBusy:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (Performance Monitoring Unit is occupied by other application). Try to stop the application that uses PMU." << endl;
        cout << "Alternatively you can try to reset PMU configuration at your own risk. Try to reset? (y/n)" << endl;
		char yn;
		yn = 'y'; //GO: what could possibly go wrong ;P		
        //std::cin >> yn;
        if ('y' == yn)
        {
            m->resetPMU();
            cout << "PMU configuration has been reset. Try to rerun the program again." << endl;
        }
        return -1;
    default:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (Unknown error)." << endl;
        return -1;
    }

    cout << "\nDetected "<< m->getCPUBrandString() << " \"Intel(r) microarchitecture codename "<<m->getUArchCodename()<<"\""<<endl;

	ofstream fCPUBrandString;
	fCPUBrandString.open("CPUBrandString.txt");
	fCPUBrandString << m->getCPUBrandString();
	fCPUBrandString.close();
	
	ofstream fArchCodename;
	fArchCodename.open("ArchCodename.txt");
	fArchCodename << m->getUArchCodename();
	fCPUBrandString.close();

	/*
	ofstream fcorerunningperf;
	fcorerunningperf.open("CorerunningPerf.txt");
	fcorerunningperf << m->getCorerunningPerf();
	fcorerunningperf.close();
	*/

	ofstream fnrcores;
	fnrcores.open("NrCores.txt");
	fnrcores << m->getNumCores();
	fnrcores.close();


   uint32 nrcores = m->getNumCores();     
   
   plists0 = new list<uint64>[nrcores];
   plists1 = new list<uint64>[nrcores];
   plists2 = new list<uint64>[nrcores];
   plists3 = new list<uint64>[nrcores];

   plists4 = new list<uint64>[nrcores];
   plists5 = new list<uint64>[nrcores];
   plists6 = new list<uint64>[nrcores];
   plists7 = new list<uint64>[nrcores];


   plist_cycles = new list<uint64>[nrcores]; //saving rtdsc here
   plist_refcycles = new list<uint64>[nrcores]; //saving rtdsc here
   plist_tsc = new list<uint64>[nrcores]; //saving rtdsc here

   plist_mcread = new list<uint64>[1]; //GO: fixme multi socket
   plist_mcwrite = new list<uint64>[1];

//	PCM::getInstance()->cleanup();
   
   
}

void perfmon_start ()
{

	/*long ret = SetThreadIdealProcessor(GetCurrentThread(),0);
	if (ret == -1)
		ret = GetLastError() * 100;
	DWORD cur_core =  GetCurrentProcessorNumber();	//GO TODO: this only works with windows!
	long out = cur_core; */
	ofstream fcorerunningperf;
	fcorerunningperf.open("CorerunningPerf.txt");
	fcorerunningperf << "0";
	fcorerunningperf.close();
	

	cstates1 = new  CoreCounterState[PCM::getInstance()->getNumCores()];
    cstates2 = new  CoreCounterState[PCM::getInstance()->getNumCores()];
    sktstate1 = new SocketCounterState[m->getNumSockets()];
    sktstate2 = new SocketCounterState[m->getNumSockets()];    
	sstate1 = new SystemCounterState();
	sstate2 = new SystemCounterState();
    const int cpu_model = m->getCPUModel();
    *sstate1 = getSystemCounterState();
    for (uint32 i = 0; i < m->getNumSockets(); ++i)
        sktstate1[i] = getSocketCounterState(i);
    for (uint32 i = 0; i < m->getNumCores(); ++i)
        cstates1[i] = getCoreCounterState(i);

	
}


void perfmon_stop()
{
	*sstate2 = getSystemCounterState();
    for (uint32 i = 0; i < m->getNumSockets(); ++i)
        sktstate2[i] = getSocketCounterState(i);
    for (uint32 i = 0; i < m->getNumCores(); ++i)
        cstates2[i] = getCoreCounterState(i);

	for (uint32 i = 0; i < m->getNumCores(); ++i)
	{			
		plists0[i].push_front(getCustom0(cstates1[i], cstates2[i]));
		plists1[i].push_front(getCustom1(cstates1[i], cstates2[i]));
		plists2[i].push_front(getCustom2(cstates1[i], cstates2[i]));
		plists3[i].push_front(getCustom3(cstates1[i], cstates2[i]));
		plists4[i].push_front(getCustom4(cstates1[i], cstates2[i]));
		plists5[i].push_front(getCustom5(cstates1[i], cstates2[i]));
		plists6[i].push_front(getCustom6(cstates1[i], cstates2[i]));
		plists7[i].push_front(getCustom7(cstates1[i], cstates2[i]));

		plist_cycles[i].push_front(getCycles(cstates1[i], cstates2[i]));
		plist_refcycles[i].push_front(getRefCycles(cstates1[i], cstates2[i]));
		plist_tsc[i].push_front(getInvariantTSC(cstates1[i], cstates2[i]));		
	}			

	plist_mcread[0].push_front(getBytesReadFromMC(sktstate1[0], sktstate2[0]));
	plist_mcwrite[0].push_front(getBytesWrittenToMC(sktstate1[0], sktstate2[0]));



}





void perfmon_end()
{
	char tstring[100];
	uint32 nrcores = m->getNumCores();        	
	fplist0 = new ofstream[nrcores];
	fplist1 = new ofstream[nrcores];
	fplist2 = new ofstream[nrcores];
	fplist3 = new ofstream[nrcores];
	fplist4 = new ofstream[nrcores];
	fplist5 = new ofstream[nrcores];
	fplist6 = new ofstream[nrcores];
	fplist7 = new ofstream[nrcores];

	fplist_cycles = new ofstream[nrcores];
	fplist_refcycles = new ofstream[nrcores];
	fplist_tsc = new ofstream[nrcores];

	fplist_mcread = new ofstream[1];
	fplist_mcwrite = new ofstream[1];

	list<uint64>::iterator it;
	for (uint32 i = 0; i < m->getNumCores(); ++i)
	{	
		stringstream ss0;
		
		ss0 << "Custom_ev0_core" << i << ".txt";
		//strcpy(ss0.str(),tstring);
		fplist0[i].open(ss0.str().c_str());
		//fplist0[i].open(tstring);
  	    for (it =  plists0[i].begin(); it != plists0[i].end(); ++it)
			fplist0[i] << *it << " ";
		fplist0[i].close();

		stringstream ss1;
		ss1 << "Custom_ev1_core" << i << ".txt";
		fplist1[i].open(ss1.str().c_str());
  	    for (it =  plists1[i].begin(); it != plists1[i].end(); ++it)
			fplist1[i] << *it << " ";
		fplist1[i].close();

		stringstream ss2;
		ss2 << "Custom_ev2_core" << i << ".txt";
		fplist2[i].open(ss2.str().c_str());
  	    for (it =  plists2[i].begin(); it != plists2[i].end(); ++it)
			fplist2[i] << *it << " ";
		fplist2[i].close();

		stringstream ss3;
		ss3 << "Custom_ev3_core" << i << ".txt";
		fplist3[i].open(ss3.str().c_str());
  	    for (it =  plists3[i].begin(); it != plists3[i].end(); ++it)
			fplist3[i] << *it << " ";
		fplist3[i].close();



		stringstream ss4;
		ss4 << "Cycles_core_" << i << ".txt";
		fplist_cycles[i].open(ss4.str().c_str());
  	    for (it =  plist_cycles[i].begin(); it != plist_cycles[i].end(); ++it)
			fplist_cycles[i] << *it << " ";
		fplist_cycles[i].close();

		stringstream ss5;
		ss5 << "RefCycles_core_" << i << ".txt";
		fplist_refcycles[i].open(ss5.str().c_str());
  	    for (it =  plist_refcycles[i].begin(); it != plist_refcycles[i].end(); ++it)
			fplist_refcycles[i] << *it << " ";
		fplist_refcycles[i].close();

		stringstream ss6;
		ss6 << "TSC_core_" << i << ".txt";
		fplist_tsc[i].open(ss6.str().c_str());
  	    for (it =  plist_tsc[i].begin(); it != plist_tsc[i].end(); ++it)
			fplist_tsc[i] << *it << " ";
		fplist_tsc[i].close();
		

		stringstream ss7;
		ss7 << "Custom_ev4_core" << i << ".txt";
		fplist4[i].open(ss7.str().c_str());
  	    for (it =  plists4[i].begin(); it != plists4[i].end(); ++it)
			fplist4[i] << *it << " ";
		fplist4[i].close();

		stringstream ss8;
		ss8 << "Custom_ev5_core" << i << ".txt";
		fplist5[i].open(ss8.str().c_str());
  	    for (it =  plists5[i].begin(); it != plists5[i].end(); ++it)
			fplist5[i] << *it << " ";
		fplist5[i].close();

		stringstream ss9;
		ss9 << "Custom_ev6_core" << i << ".txt";
		fplist6[i].open(ss9.str().c_str());
  	    for (it =  plists6[i].begin(); it != plists6[i].end(); ++it)
			fplist6[i] << *it << " ";
		fplist6[i].close();

		stringstream ss10;
		ss10 << "Custom_ev7_core" << i << ".txt";
		fplist7[i].open(ss10.str().c_str());
  	    for (it =  plists7[i].begin(); it != plists7[i].end(); ++it)
			fplist7[i] << *it << " ";
		fplist7[i].close();



	}
	
	stringstream ss_read;
	ss_read << "MC_read.txt";
	fplist_mcread[0].open(ss_read.str().c_str());	
	for (it =  plist_mcread[0].begin(); it != plist_mcread[0].end(); ++it)
		fplist_mcread[0] << *it << " ";
	fplist_mcread[0].close();

	stringstream ss_write;
	ss_write << "MC_write.txt";
	fplist_mcwrite[0].open(ss_write.str().c_str());  	
	for (it =  plist_mcwrite[0].begin(); it != plist_mcwrite[0].end(); ++it)
		fplist_mcwrite[0] << *it << " ";
	fplist_mcwrite[0].close();
	
	
	PCM::getInstance()->cleanup();	
    /*delete[] cstates1;
    delete[] cstates2;
    delete[] sktstate1;
    delete[] sktstate2;
	delete[] sstate1;
	delete[] sstate2; */
	flog.close();
	std::cout.rdbuf(coutbuf);
}

int init()
{
	#ifdef PCM_FORCE_SILENT
    null_stream nullStream1, nullStream2;
    std::cout.rdbuf(&nullStream1);
    std::cerr.rdbuf(&nullStream2);
    #endif

    cout << endl;
    cout << " Intel(r) Performance Counter Monitor "<< INTEL_PCM_VERSION << endl;
    cout << endl;
    cout << " Copyright (c) 2009-2012 Intel Corporation" << endl;
    cout << endl;



	    #ifdef _MSC_VER
    // Increase the priority a bit to improve context switching delays on Windows
    SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_ABOVE_NORMAL);

    TCHAR driverPath[1024];
    GetCurrentDirectory(1024, driverPath);
    wcscat(driverPath, L"\\msr.sys");

    //SetConsoleCtrlHandler((PHANDLER_ROUTINE)cleanup, TRUE);
        #else
    signal(SIGINT, cleanup);
    signal(SIGKILL, cleanup);
    signal(SIGTERM, cleanup);
        #endif

        #ifdef _MSC_VER
    // WARNING: This driver code (msr.sys) is only for testing purposes, not for production use
    Driver drv;
    // drv.stop();     // restart driver (usually not needed)
    if (!drv.start(driverPath))
    {
		cout << "Cannot access CPU counters" << endl;
		cout << "You must have signed msr.sys driver in your current directory and have administrator rights to run this program" << endl;
    }
        #endif

    m = PCM::getInstance();
    PCM::ErrorCode status = m->program();
    switch (status)
    {
    case PCM::Success:
        break;
    case PCM::MSRAccessDenied:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (no MSR or PCI CFG space access)." << endl;
        return -1;
    case PCM::PMUBusy:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (Performance Monitoring Unit is occupied by other application). Try to stop the application that uses PMU." << endl;
        cout << "Alternatively you can try to reset PMU configuration at your own risk. Try to reset? (y/n)" << endl;
        char yn;
        std::cin >> yn;
        if ('y' == yn)
        {
            m->resetPMU();
            cout << "PMU configuration has been reset. Try to rerun the program again." << endl;
        }
        return -1;
    default:
        cout << "Access to Intel(r) Performance Counter Monitor has denied (Unknown error)." << endl;
        return -1;
    }

    cout << "\nDetected "<< m->getCPUBrandString() << " \"Intel(r) microarchitecture codename "<<m->getUArchCodename()<<"\""<<endl;
//	PCM::getInstance()->cleanup();

}


	
static char dummy;
static int const CacheLineSize=64;


void start ()
{
#ifdef FLUSH
	cout << "Flushing Cache " <<endl;
	size_t blockSize = 13 * (1<<20); //TODO: choose size according to LLC
	char * buffer = (char *) malloc(blockSize);

	// bring the whole buffer into memory
	for (size_t i = 0; i < blockSize; i+=CacheLineSize) {
		dummy += buffer[i];
	}

	free((void*) buffer);
#endif 

	//bring the whole buffer into memory


	cstates1 = new  CoreCounterState[PCM::getInstance()->getNumCores()];
    cstates2 = new  CoreCounterState[PCM::getInstance()->getNumCores()];
    sktstate1 = new SocketCounterState[m->getNumSockets()];
    sktstate2 = new SocketCounterState[m->getNumSockets()];    

	sstate1 = new SystemCounterState();
	sstate2 = new SystemCounterState();

    const int cpu_model = m->getCPUModel();
    *sstate1 = getSystemCounterState();
    for (uint32 i = 0; i < m->getNumSockets(); ++i)
        sktstate1[i] = getSocketCounterState(i);
    for (uint32 i = 0; i < m->getNumCores(); ++i)
        cstates1[i] = getCoreCounterState(i);
}


void stop_flop ()
{
	       *sstate2 = getSystemCounterState();
        for (uint32 i = 0; i < m->getNumSockets(); ++i)
            sktstate2[i] = getSocketCounterState(i);
        for (uint32 i = 0; i < m->getNumCores(); ++i)
            cstates2[i] = getCoreCounterState(i);

			for (uint32 i = 0; i < m->getNumCores(); ++i)
            {
				  cout << " " << setw(3) << i << "   " << setw(2) << m->getSocketId(i) <<
                    "     " << getExecUsage(cstates1[i], cstates2[i]) <<
                    "   " << getIPC(cstates1[i], cstates2[i]) <<
                    "   " << getRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "    " << getActiveRelativeFrequency(cstates1[i], cstates2[i]) <<
					"    " << unit_format(getScalarDouble(cstates1[i], cstates2[i])) <<
					"    " << unit_format(getPackedDouble(cstates1[i], cstates2[i])) <<
					"    " << unit_format(getAVXDouble(cstates1[i], cstates2[i])) <<				                    
                    "\n";
			}
}


void stop ()
{
	//	TimeAfterSleep = m->getTickCount();
        *sstate2 = getSystemCounterState();
        for (uint32 i = 0; i < m->getNumSockets(); ++i)
            sktstate2[i] = getSocketCounterState(i);
        for (uint32 i = 0; i < m->getNumCores(); ++i)
            cstates2[i] = getCoreCounterState(i);
		
		const int cpu_model = m->getCPUModel();
		       // sanity checks
        if (cpu_model == PCM::ATOM)
        {
            assert(getNumberOfCustomEvents(0, *sstate1, *sstate2) == getL2CacheMisses(*sstate1, *sstate2));
            assert(getNumberOfCustomEvents(1, *sstate1, *sstate2) == getL2CacheMisses(*sstate1, *sstate2) + getL2CacheHits(*sstate1, *sstate2));
        }
        else
        {
            assert(getNumberOfCustomEvents(0, *sstate1, *sstate2) == getL3CacheMisses(*sstate1, *sstate2));
            assert(getNumberOfCustomEvents(1, *sstate1, *sstate2) == getL3CacheHitsNoSnoop(*sstate1, *sstate2));
            assert(getNumberOfCustomEvents(2, *sstate1, *sstate2) == getL3CacheHitsSnoop(*sstate1, *sstate2));
            assert(getNumberOfCustomEvents(3, *sstate1, *sstate2) == getL2CacheHits(*sstate1, *sstate2));
        }


		
            if(!(m->getNumSockets() == 1 && cpu_model==PCM::ATOM))
            {
				cout << " Core (SKT) | EXEC | IPC  | FREQ  | AFREQ | L3MISS | L2MISS | L3HIT | L2HIT | L3CLK | L2CLK  | READ  | WRITE | TEMP" << "\n" << "\n";
				cout << "-------------------------------------------------------------------------------------------------------------------" << "\n";
				for (uint32 i = 0; i < m->getNumCores(); ++i)
            {
                if (cpu_model != PCM::ATOM)
                    cout << " " << setw(3) << i << "   " << setw(2) << m->getSocketId(i) <<
                    "     " << getExecUsage(cstates1[i], cstates2[i]) <<
                    "   " << getIPC(cstates1[i], cstates2[i]) <<
                    "   " << getRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "    " << getActiveRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "    " << unit_format(getL3CacheMisses(cstates1[i], cstates2[i])) <<
                    "   " << unit_format(getL2CacheMisses(cstates1[i], cstates2[i])) <<
                    "    " << getL3CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "    " << getL2CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "    " << getCyclesLostDueL3CacheMisses(cstates1[i], cstates2[i]) <<
                    "    " << getCyclesLostDueL2CacheMisses(cstates1[i], cstates2[i]) <<
                    "     N/A     N/A" <<
                    "     " << temp_format(cstates2[i].getThermalHeadroom()) <<
                    "\n";
                else
                    cout << " " << setw(3) << i << "   " << setw(2) << m->getSocketId(i) <<
                    "     " << getExecUsage(cstates1[i], cstates2[i]) <<
                    "   " << getIPC(cstates1[i], cstates2[i]) <<
                    "   " << getRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "   " << unit_format(getL2CacheMisses(cstates1[i], cstates2[i])) <<
                    "    " << getL2CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "     " << temp_format(cstates2[i].getThermalHeadroom()) <<
                    "\n";
            }

                cout << "-------------------------------------------------------------------------------------------------------------------" << "\n";
                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i <<
                    "     " << getExecUsage(sktstate1[i], sktstate2[i]) <<
                    "   " << getIPC(sktstate1[i], sktstate2[i]) <<
                    "   " << getRelativeFrequency(sktstate1[i], sktstate2[i]) <<
                    "    " << getActiveRelativeFrequency(sktstate1[i], sktstate2[i]) <<
                    "    " << unit_format(getL3CacheMisses(sktstate1[i], sktstate2[i])) <<
                    "   " << unit_format(getL2CacheMisses(sktstate1[i], sktstate2[i])) <<
                    "    " << getL3CacheHitRatio(sktstate1[i], sktstate2[i]) <<
                    "    " << getL2CacheHitRatio(sktstate1[i], sktstate2[i]) <<
                    "    " << getCyclesLostDueL3CacheMisses(sktstate1[i], sktstate2[i]) <<
                    "    " << getCyclesLostDueL2CacheMisses(sktstate1[i], sktstate2[i]);
                    if (!(m->memoryTrafficMetricsAvailable()))
                       cout << "     N/A     N/A";
                   else
                       cout << "    " << getBytesReadFromMC(sktstate1[i], sktstate2[i]) / double(1024ULL * 1024ULL * 1024ULL) <<
                               "    " << getBytesWrittenToMC(sktstate1[i], sktstate2[i]) / double(1024ULL * 1024ULL * 1024ULL);
                    cout << "     " << temp_format(sktstate2[i].getThermalHeadroom()) << "\n";
                }
            }


}


void end()
{
	PCM::getInstance()->cleanup();
	
	
    delete[] cstates1;
    delete[] cstates2;
    delete[] sktstate1;
    delete[] sktstate2;
	delete[] sstate1;
	delete[] sstate2; 
}






