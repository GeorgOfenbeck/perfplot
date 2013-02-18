#ifndef MEASURING_CORE_HEADER
#define MEASURING_CORE_HEADER

#include "cpuid_info.h"


//int measurement_init(int type, bool flushData , bool flushICache , bool flushTLB );
int measurement_init(long * custom_counters = NULL, long offcore_response0 = 0, long offcore_response1 = 0);
void measurement_start();
void measurement_stop(long runs=1);
void measurement_end();
// Start Dani
//bool measurement_customTest(size_t runs, size_t vlen);
long measurement_run_multiplier(long threshold);
bool measurement_testDerivative(size_t runs, double alpha_threshold, double avg_threshold, double time_threshold, double *d, size_t points=1);
//void measurement_meanSingleRun();
//bool measurement_testSD(size_t runs);
void measurement_emptyLists(bool clearRuns=true);
void dumpMeans();

unsigned long measurement_getNumberOfShifts(unsigned long size, unsigned long initialGuess);
// End Dani

void flushITLB();
void flushDTLB();
void flushICache();
void flushDCache();

// Vicky --- Functions for getting cache parameters with CPUID



cpuid_cache_descriptor_t getTLBinfo(cpuid_leaf2_qualifier_t cacheType);
unsigned long getLLCSize();

#endif




