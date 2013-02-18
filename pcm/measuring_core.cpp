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
#include "dummyFunction.h"

#include <algorithm>
#include <climits>
#include <vector>


#define SIZE (10000000)
#define DELAY 1 // in seconds



//Vicky - Auxiliary functions for execuitng CPUID
#define __WORDSIZE 64
#define cpuid(aid,bid, cid, did) __asm__( "cpuid" : "=a"(eax), "=b"(ebx), "=c"(ecx), "=d"(edx) : "a"(aid), "b"(bid), "c"(cid), "d"(did))
#define b(val, base, end) ((val << (__WORDSIZE-end-1)) >> (__WORDSIZE-end+base-1))
#define max(a,b) ((a) > (b) ? (a) : (b))
// End of auxiliary functions

using namespace std;


extern long g_offcore_response0, g_offcore_response1;

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
 ofstream f_error;

 std::streambuf *coutbuf;
 std::streambuf *cerrtbuf;


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

 list<uint64> *plist_nrruns;
 list<uint64> *plist_mcread;
 list<uint64> *plist_mcwrite;


 ofstream * fplist_nrruns;
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
 
// Dani start
 vector<size_t> *runvec;
 vector<double> *meancyclesvec;
// vector<double> *sdcyclesvec;
// Dani end


//Vicky - get LLC cache size from CPUID
unsigned long getLLCSize(){
  unsigned long LLCsize , cacheLevel, associativity, nSets, nPartitions, lineSize;
  unsigned long eax, ebx, ecx, edx;
  int cacheInstance = -1;
  do{
    cacheInstance++;
    cpuid(4,0,cacheInstance,0);
    cacheLevel = b(eax, 5, 7);
  }while(cacheLevel != 0);
  // cacheInstance-1 is the LLC
  cpuid(4,0,cacheInstance-1,0);
  associativity = b(ebx, 22, 31)+1;
  nSets = b(ecx, 0, 31)+1;
  nPartitions = b(ebx, 12, 21)+1;
  lineSize = b(ebx, 0, 11)+1;
  LLCsize = associativity*nSets*nPartitions*lineSize;
  return LLCsize;
}
 // End of obtaining LLC cache size from CPUID



//Vicky - get TLB cache size info from CPUID


cpuid_cache_descriptor_t getTLBinfo(cpuid_leaf2_qualifier_t cacheType){
  
  unsigned long eax, ebx, ecx, edx;
  cpuid_cache_descriptor_t tlb_info;
  tlb_info.size = 0;
  tlb_info.entries = 0;
  cpuid(2,0,0,0);
  
  unsigned long cachesInfo[]={b(eax, 0, 7),
          b(eax, 8, 15),
          b(eax, 16, 23),
          b(eax, 24, 31),
          b(ebx, 0, 7),
          b(ebx, 8, 15),
          b(ebx, 16, 23),
          b(ebx, 24, 31),
          b(ecx, 0, 7),
          b(ecx, 8, 15),
          b(ecx, 16, 23),
          b(ecx, 24, 31),
          b(edx, 0, 7),
          b(edx, 8, 15),
          b(edx, 16, 23),
    b(edx, 24, 31)};
 
     
    for (unsigned int i = 0; i< 16; i++) {
      if (cachesInfo[i]!= 0) {
        for (unsigned int j=0; j< sizeof(intel_cpuid_leaf2_descriptor_table) / sizeof(cpuid_cache_descriptor_t) ;j++ ) {
       cpuid_cache_descriptor_t desc = intel_cpuid_leaf2_descriptor_table[j];
          if (desc.value == cachesInfo[i]) {
            // Entry found
              if (desc.type == TLB && desc.level == cacheType) {
                // From all the TLBs, take the number of entries of the larger one ("last-level")
                if ( desc.entries > tlb_info.entries)
                  tlb_info = desc;
            }
          }
        }
      }
    }
  return tlb_info;
}


// For flushing instruction caches, it is useless to calculate the cache
// size because the dummy code that is going to be run is created
// at compilation time... We just make it large enough to flush the
// caches, regardless their sizes

void flushITLB()
{
	dummyFunction();
  
}



void flushDTLB()
{
  cpuid_cache_descriptor_t tlb_info = getTLBinfo(DATA);
//  cout << "DTLB entries " << tlb_info.entries << endl;
 // cout << "DTLB page size " << tlb_info.size << " (B)\n";
  
	long size = tlb_info.entries*tlb_info.size*2;
	double * buffer = (double *) malloc(size);
	double result = 0;
  
	for (unsigned long i = 0; i < size/sizeof(double); i=i+tlb_info.size)
	{
		result += buffer[i];
	}
	for (unsigned long i = 0; i < size/sizeof(double); i=i+tlb_info.size)
	{
		result += buffer[i];
	}
	free(buffer);
  
}




void flushICache()
{
  dummyFunction();
}


void flushDCache()
{
  
  unsigned long LLCsize = getLLCSize();
	long size = LLCsize*2; 
	double * buffer = (double *) malloc(size);
	double result = 0;
	for (unsigned long i = 0; i < size/sizeof(double); i=i+1)
	{
		result += buffer[i];
	}
	for (unsigned long i = 0; i < size/sizeof(double); i=i+1)
	{
		result += buffer[i];
	}
	free(buffer);
	std::cout << "flushed cache "<< result << endl;
}



int measurement_init(long * custom_counters, long offcore_response0, long offcore_response1)
{
    
	flog.open("log.txt");	 
	coutbuf = std::cout.rdbuf(); //save old buf
	std::cout.rdbuf(flog.rdbuf());

	    
	f_error.open("error_stream.txt");	 
	cerrtbuf = std::cerr.rdbuf(); //save old buf
	std::cerr.rdbuf(f_error.rdbuf());

	

	cout << "Starting log - v2.35\n";
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

    SetConsoleCtrlHandler((PHANDLER_ROUTINE)cleanup, TRUE);
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
	
	if (custom_counters == NULL)
	{
		status = m->program();
	}
	else
	{
		PCM::CustomCoreEventDescription events[4];	
		for (int i = 0; i< 4; i++)
		{
			events[i].event_number = custom_counters[i*2];
			events[i].umask_value = custom_counters[i*2+1];
		}
		status = m->program(PCM::CUSTOM_CORE_EVENTS,events);

		g_offcore_response0 = offcore_response0;
		g_offcore_response1 = offcore_response1;
			
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

	ofstream fnrsockets;
	fnrsockets.open("NrSockets.txt");
	fnrsockets << m->getNumSockets();
	fnrsockets.close();


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

   plist_nrruns = new list<uint64>[1];
   plist_mcread = new list<uint64>[1]; 
   plist_mcwrite = new list<uint64>[1];


   //Dani start
   runvec = new vector<size_t>();
   meancyclesvec = new vector<double>();
   
   //Dani end

}

void measurement_start ()
{

	/*long ret = SetThreadIdealProcessor(GetCurrentThread(),0);
	if (ret == -1)
		ret = GetLastError() * 100;
	DWORD cur_core =  GetCurrentProcessorNumber();	//GO TODO: this only works with windows!
	long out = cur_core; */
	
	/*
	ofstream fcorerunningperf;
	fcorerunningperf.open("CorerunningPerf.txt");
	fcorerunningperf << "0";
	fcorerunningperf.close();
	*/

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



void measurement_stop(long nr_runs)
{
	*sstate2 = getSystemCounterState();
    for (uint32 i = 0; i < m->getNumSockets(); ++i)
        sktstate2[i] = getSocketCounterState(i);
    for (uint32 i = 0; i < m->getNumCores(); ++i)
        cstates2[i] = getCoreCounterState(i);

	for (uint32 i = 0; i < m->getNumCores(); ++i)		
	{			

		uint64 c0,c1,c2,c3;

		c0 = getL3CacheMisses(cstates1[i], cstates2[i]);
		c1 = getL3CacheHitsNoSnoop(cstates1[i], cstates2[i]);
		c2 = getL3CacheHitsSnoop(cstates1[i], cstates2[i]);
		c3 = getL2CacheHits(cstates1[i], cstates2[i]);

		plists0[i].push_front(c0/nr_runs); //Counter0
		plists1[i].push_front(c1/nr_runs); //Counter1
		plists2[i].push_front(c2/nr_runs); //Counter2
		plists3[i].push_front(c3/nr_runs); //Counter3

		//this is a placeholder till all 8 counters are enabled again
		plists4[i].push_front(0); //Counter0
		plists5[i].push_front(0); //Counter1
		plists6[i].push_front(0); //Counter2
		plists7[i].push_front(0); //Counter3

		/*
		plists4[i].push_front(getCustom4(cstates1[i], cstates2[i]));
		plists5[i].push_front(getCustom5(cstates1[i], cstates2[i]));
		plists6[i].push_front(getCustom6(cstates1[i], cstates2[i]));
		plists7[i].push_front(getCustom7(cstates1[i], cstates2[i]));
        */
		plist_cycles[i].push_front(getCycles(cstates1[i], cstates2[i])/nr_runs);
		plist_refcycles[i].push_front(getRefCycles(cstates1[i], cstates2[i])/nr_runs);
		plist_tsc[i].push_front(getInvariantTSC(cstates1[i], cstates2[i])/nr_runs);		

		
	}
	
	long sysRead, sysWrite;
	sysRead = 0;
	sysWrite = 0;

	for (uint32 i = 0; i < m->getNumSockets(); ++i)
	{
		sysRead =+ getBytesReadFromMC(sktstate1[i], sktstate2[i]) ;
		sysWrite =+ getBytesWrittenToMC(sktstate1[i], sktstate2[i]);
	}

	plist_mcread[0].push_front(sysRead/nr_runs);
	plist_mcwrite[0].push_front(sysWrite/nr_runs);
	plist_nrruns[0].push_front(nr_runs);	
	
	// Dani start
	runvec->push_back(nr_runs);
	// Dani end

}


//// Start Dani

void measurement_emptyLists(bool clearRuns)
{
	for (uint32 i = 0; i < m->getNumCores(); ++i)
	{
		plists0[i].clear();
		plists1[i].clear();
		plists2[i].clear();
		plists3[i].clear();
		plists4[i].clear();
		plists5[i].clear();
		plists6[i].clear();
		plists7[i].clear();

		plist_cycles[i].clear();
		plist_refcycles[i].clear();
		plist_tsc[i].clear();
	}

	plist_nrruns[0].clear();
	plist_mcread[0].clear();
	plist_mcwrite[0].clear();
	
	if(clearRuns) {
		runvec->clear();
		meancyclesvec->clear();
	}
 
}
bool measurement_testDerivative(size_t runs, double alpha_threshold, double avg_threshold, double time_threshold, double *d, size_t points) {

	// Using TSC
	size_t n = plist_tsc[0].size();
//	double sumcycle2 = 0;
	double cumcycles = 0, sumcycle = 0, cycles, c_threshold = time_threshold*m->getNominalFrequency();

	uint32 ncores = m->getNumCores();
	list<uint64>::iterator* it = new list<uint64>::iterator[ncores];
	for(uint32 c = 0; c < ncores; c++)
		it[c] = plist_tsc[c].begin();

	// Average, and sd is computed based on the max. TSC
	for (size_t i = 0; i < n; ++i) {
		uint64 maxtsc = *it[0];
		for(uint32 c = 1; c < ncores; c++)
			maxtsc = max(maxtsc, *it[c]);
		cumcycles += maxtsc;
		cycles = double(maxtsc)/runs;
//		sumcycle2 += cycles*cycles;
		sumcycle += cycles;
		for(uint32 c = 0; c < ncores; c++)
			it[c]++;
	}

//	double s2 = (n*sumcycle2 - sumcycle*sumcycle)/(n*(n-1));
	double avg  = sumcycle/n;
//	double sd = sqrt(s2);

//	cout << endl << endl << "SD Test on Core " << 3 << ": " << endl;
//	cout << "\tAverage cycles: " << m << endl;
//	cout << "\tStandard deviation: " << sd << endl;
//
	meancyclesvec->push_back(avg);
//	sdcyclesvec->push_back(sd);

	if (runvec->size() < points+2)
		return false;

	n = runvec->size();

	bool condition = true;
	size_t nump = points;
	while ((condition) && (points>0)) {
		d[nump-points] = ((*meancyclesvec)[n-points] - (*meancyclesvec)[n-points-2])/((*runvec)[n-points] - (*runvec)[n-points-2]);
		condition = (fabs(d[nump-points]) <= alpha_threshold);
		--points;
	}

	cout << "With C_THRESH = " << c_threshold << " Sum. Cycles = " << cumcycles << endl;
	cout << "condition = " << condition << endl;
	cout << "AVG_THRESH = " << avg_threshold << " Avg. = " << avg << endl;

	return (condition || avg > avg_threshold || cumcycles > c_threshold);
//	return (condition || m > 1e7);
//	return (condition || m > 1e7 || sumcycle > 1e7);
//	return (condition || sumcycle > 1e7);

}

unsigned long measurement_getNumberOfShifts(unsigned long size, unsigned long initialGuess) {
	
	
	unsigned long value = initialGuess;
	unsigned long llcSize = getLLCSize();
	if (size > 2 *llcSize || size*value > 2*llcSize) { //GO: 2DO - binary search - calculate with page aligned!
		value = 2;
		while((size*value < 2*llcSize) )
			value=value*2;
	}
	return value;
}

void dumpMeans()
{
	ofstream f;
	f.open("tsc_means.csv");
	size_t n = runvec->size();
	for (size_t i = 0; i < n; ++i)
		f << (*runvec)[i] << "," << (*meancyclesvec)[i] << endl;
//		f << (*runvec)[i] << "," << (*meancyclesvec)[i] << "," << (*sdcyclesvec)[i] << endl;
	f.close();
}
// End Dani




void measurement_end()
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

	fplist_nrruns = new ofstream[1];

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
	

	stringstream ss_nrruns;
	ss_nrruns << "nrruns.txt";
	fplist_nrruns[0].open(ss_nrruns.str().c_str());  	
	for (it =  plist_nrruns[0].begin(); it != plist_nrruns[0].end(); ++it)
		fplist_nrruns[0] << *it << " ";
	fplist_nrruns[0].close();

	
	PCM::getInstance()->cleanup();	
    delete[] cstates1;
    delete[] cstates2;
    delete[] sktstate1;
    delete[] sktstate2;
	delete sstate1;
	delete sstate2;
	flog.close();
	f_error.close();
	std::cout.rdbuf(coutbuf);
	std::cerr.rdbuf(cerrtbuf);
}



