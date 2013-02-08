#ifndef MEASURING_CORE_HEADER
#define MEASURING_CORE_HEADER



int perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );
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

int flushTLB();
int flushICache();
int flushCache();


#endif
