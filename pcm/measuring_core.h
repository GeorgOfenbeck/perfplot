#ifndef MEASURING_CORE_HEADER
#define MEASURING_CORE_HEADER



#ifdef __cplusplus
extern "C" {
#endif
//int measurement_init(int type, bool flushData , bool flushICache , bool flushTLB );
int measurement_init(long * custom_counters , unsigned long offcore_response0 , unsigned long offcore_response1 );
void measurement_start();
void measurement_stop(unsigned long runs);
void measurement_end();
// Start Dani
//bool measurement_customTest(size_t runs, size_t vlen);
unsigned long measurement_run_multiplier(unsigned long threshold);
bool measurement_testDerivative(size_t runs, double alpha_threshold, double avg_threshold, double time_threshold, double *d, size_t points);
//void measurement_meanSingleRun();
//bool measurement_testSD(size_t runs);
void measurement_emptyLists(bool clearRuns);
void dumpMeans();

unsigned long measurement_getNumberOfShifts(unsigned long size, unsigned long initialGuess);
// End Dani

void flushITLB();
void flushDTLB();
void flushICache();
void flushDCache();

unsigned long getLLCSize();


#ifdef __cplusplus
}
#endif
#endif


