#ifndef MEASURING_CORE_HEADER
#define MEASURING_CORE_HEADER

#include "cpuid_info.h"


//int perfmon_init(long * custom_counters = NULL, long offcore_response0 = 0, long offcore_response1 = 0);
int perfmon_init(long * custom_counters , long offcore_response0 , long offcore_response1 );
void perfmon_start();
void perfmon_stop(long runs=1);
void perfmon_end();
// Start Dani
//bool perfmon_customTest(size_t runs, size_t vlen);
bool perfmon_testDerivative(size_t runs, double threshold, size_t points=1);
//void perfmon_meanSingleRun();
//bool perfmon_testSD(size_t runs);
void perfmon_emptyLists(bool clearRuns=true);
void dumpMeans();
// End Dani

void flushITLB();
void flushDTLB();
void flushICache();
void flushDCache();

// Vicky --- Functions for getting cache parameters with CPUID



cpuid_cache_descriptor_t getTLBinfo(cpuid_leaf2_qualifier_t cacheType);
unsigned long getLLCSize();

#endif
