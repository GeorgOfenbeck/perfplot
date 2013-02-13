/*
 * calibration.cpp
 *
 *  Created on: Feb 4, 2013
 *      Author: danieles
 */

#include <iostream>
#include <fstream>
#include <cstdlib>
#include <ctime>
#include <cmath>
#include <x86intrin.h>
#include <mkl_cblas.h>

#include "../measuring_core.h"
#include "../types.h"
#include "funcs.h"

using namespace std;

#define REP 2
#define THRESHOLD 0.0005

long events[] = {	/* double Scalar */ 0x10, 0x80, /* double packed */ 0x10, 0x10, /*double AVX*/ 0x11, 0x02, ARCH_LLC_MISS_EVTNR, ARCH_LLC_MISS_UMASK };

void _printM(double const * m, size_t const row, size_t const col, string title, ostream& stream)
{
  stream << "=========" << endl;
  stream << title << ":" << endl;
  stream << "[ ";
  for (size_t i = 0; i < row; ++i) {
      for (size_t j = 0; j < col; ++j) {
    	  stream << m[i*col + j] << "\t";
      }
      stream << ";" << endl;
  }
  stream << "]" << endl;
  stream << "=========" << endl << endl;
}

void _rands(double * m, size_t row, size_t col)
{
  for (size_t i = 0; i < row*col; ++i)  m[i] = (double)(rand())/RAND_MAX;;
}

void _zeros(double * m, size_t row, size_t col)
{
  for (size_t i = 0; i < row*col; ++i)  m[i] = 0.;
}

//void _tempBuild(double ** A, double ** B, double ** C, size_t M, size_t K, size_t N, size_t fA=1, size_t fB=1, size_t fC=1)
//{
//  *A = (double *)_mm_malloc(M*K*fA*sizeof(double), 4*1024);
//  *B = (double *)_mm_malloc(K*N*fB*sizeof(double), 4*1024);
//  *C = (double *)_mm_malloc(M*N*fC*sizeof(double), 4*1024);
//
//  _rands(*A, M, K);
//  _rands(*B, K, N);
//  _rands(*C, M, N);
//
//}

void _buildRandInit(double ** m, size_t rows, size_t cols, size_t alignment, size_t factorM=1)
{
  *m = (double *)_mm_malloc(rows*cols*factorM*sizeof(double), alignment);

  _rands(*m, rows, cols);

}

void _tempDestroy(double * A, double * B, double * C)
{
  _mm_free(A);
  _mm_free(B);
  _mm_free(C);
}

void _destroy(double * m)
{
  _mm_free(m);
}

//#define COLD

//void reduction(size_t n) {
//
//	ofstream devnull("/dev/null");
//
//	double * x, dest;
//	_buildRandInit(&x, n, 1, 4*1024);
////	double * x = (double *)_mm_malloc(n*sizeof(double), 4*1024), dest;
////	for(size_t j = 0; j < n; j++) x[j] = rand()/(double)RAND_MAX;
//
//	size_t runs = 16;
//	for(; runs <= (1 << 20); runs *= 2){
//
//		perfmon_start();
//		for(int i = 0; i < runs; i++)
//			reduction_K4_L4(x, &dest, n);
//		perfmon_stop(runs);
//
//		_printM(&dest, 1, 1, "", devnull);
//
//		if(perfmon_testDerivative(runs, THRESHOLD))
//			break;
//		perfmon_emptyLists(false); //don't clear the vector of runs
//	}
//
//#ifdef COLD
//	_destroy(x);
//#endif
//
//	cout << "\n\nUsing threshold " << THRESHOLD << " value for RUNS: " << runs << endl;
//
//	perfmon_emptyLists();
//
//#ifdef COLD
//	size_t factor = getNumberOfShifts(n*sizeof(double), runs*REP);
//
//	_buildRandInit(&x, n, 1, 4*1024, factor);
//
//	size_t idx = 0, maxidx = n*factor;
//#endif
//
//	for(int r = 0; r < REP; r++) {
//		perfmon_start();
//		for(int i = 0; i < runs; i++) {
//
//#ifdef COLD
//			reduction_K4_L4(x+idx, &dest, n);
//			idx+=n; if (idx >= maxidx) idx = 0;
//#else
//			reduction_K4_L4(x, &dest, n);
//#endif
//		}
//		perfmon_stop(runs);
//	}
//
//	_printM(&dest, 1, 1, "", devnull);
//
//	_destroy(x);
//}

void mmm(size_t n) {

	ofstream devnull("/dev/null");
	double * A, * B, * C;

	_buildRandInit(&A, n, n, 4*1024);
	_buildRandInit(&B, n, n, 4*1024);
	_buildRandInit(&C, n, n, 4*1024);

	size_t runs = 1;
	double slope = THRESHOLD, max_slope = 1000*THRESHOLD;
	double d;
	for(size_t count = 0; runs <= (1 << 20); count++){

		measurement_start();
		for(int i = 0; i < runs; i++)
			cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, n, n, n, 1., A, n, B, n, 0., C, n);
		measurement_stop(runs);

		_printM(C, n, n, "", devnull);

		if(measurement_testDerivative(runs, slope, 1e7, 10, &d))
			break;
		measurement_emptyLists(false); //don't clear the vector of runs

		if (count >= 2) {
			cout << "Runs = " << runs << " d = " << d << endl;

			if (runs <= (1 << 10)) runs *= 2;
			else if ((fabs(d) <= max_slope) && (slope*10 <= max_slope)) slope *= 10;
			else runs += 10*count;
		}
	}

#ifdef COLD
	_destroy(A); _destroy(B); _destroy(C);
#endif

	cout << "\n\nUsing threshold " << THRESHOLD << " value for RUNS: " << runs << endl;

	measurement_emptyLists();

#ifdef COLD
	size_t n2 = n*n;
	size_t factor = measurement_getNumberOfShifts(3*n2*sizeof(double), runs*REP);

	_buildRandInit(&A, n, n, 4*1024, factor);
	_buildRandInit(&B, n, n, 4*1024, factor);
	_buildRandInit(&C, n, n, 4*1024, factor);

	size_t idx = 0, maxidx = n2*factor;
#endif

	for(int r = 0; r < REP; r++) {
		measurement_start();
		for(int i = 0; i < runs; i++) {

#ifdef COLD
			cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, n, n, n, 1., A+idx, n, B+idx, n, 0., C+idx, n);
			idx+=n2; if (idx >= maxidx) idx = 0;
#else
			cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, n, n, n, 1., A, n, B, n, 0., C, n);
#endif
		}
		measurement_stop(runs);
		_printM(C, n, n, "", devnull);
	}


	_destroy(A); _destroy(B); _destroy(C);
}

void mvm(size_t n) {

	ofstream devnull("/dev/null");
	double alpha = 1.1, * A, * x, * y;

	_buildRandInit(&A, n, n, 4*1024);
	_buildRandInit(&x, n, 1, 4*1024);
	_buildRandInit(&y, n, 1, 4*1024);

	size_t runs = 1;
	double slope = THRESHOLD, max_slope = 1000*THRESHOLD;
	double d;

	for(size_t count = 0; runs <= (1 << 20); count++){

		measurement_start();
		for(int i = 0; i < runs; i++)
			cblas_dgemv(CblasRowMajor, CblasNoTrans, n, n, alpha, A, n, x, 1, 0., y, 1);
		measurement_stop(runs);

		_printM(y, n, 1, "", devnull);

		if(measurement_testDerivative(runs, slope, 1e7, 10, &d))
			break;
		dumpMeans();
		measurement_emptyLists(false); //don't clear the vector of runs

		if (count >= 2) {
			cout << "Runs = " << runs << " d = " << d << endl;

			if (runs <= (1 << 10)) runs *= 2;
			else if ((fabs(d) <= max_slope) && (slope*10 <= max_slope)) slope *= 10;
			else runs += 10*count;
		}
	}

#ifdef COLD
	_destroy(A); _destroy(x); _destroy(y);
#endif

	cout << "Out - Runs = " << runs << " d = " << d << endl;
	cout << "\n\nUsing threshold " << alpha << " value for RUNS: " << runs << endl;

	dumpMeans();
	measurement_emptyLists();

#ifdef COLD
	size_t n2 = n*n;
	size_t factor = measurement_getNumberOfShifts((n2+2*n)*sizeof(double), runs*REP);

	_buildRandInit(&A, n, n, 4*1024, factor);
	_buildRandInit(&x, n, 1, 4*1024, factor);
	_buildRandInit(&y, n, 1, 4*1024, factor);

	size_t m_idx = 0, m_maxidx = n2*factor;
	size_t v_idx = 0, v_maxidx = n*factor;
#endif

	for(int r = 0; r < REP; r++) {
		measurement_start();
		for(int i = 0; i < runs; i++) {

#ifdef COLD
			cblas_dgemv(CblasRowMajor, CblasNoTrans, n, n, alpha, A+m_idx, n, x+v_idx, 1, 0., y+v_idx, 1);
			m_idx+=n2; if (m_idx >= m_maxidx) m_idx = 0;
			v_idx+=n;  if (v_idx >= v_maxidx) v_idx = 0;
#else
			cblas_dgemv(CblasRowMajor, CblasNoTrans, n, n, alpha, A, n, x, 1, 0., y, 1);
#endif
		}
		measurement_stop(runs);
		_printM(y, n, 1, "", devnull);
	}


	_destroy(A); _destroy(x); _destroy(y);
}

//void daxpy(size_t n) {
//
//	ofstream devnull("/dev/null");
//	double alpha = 1.1, * x, * y;
//
//	_buildRandInit(&x, n, 1, 4*1024);
//	_buildRandInit(&y, n, 1, 4*1024);
//
//	size_t runs = 2;
//	for(; runs <= (1 << 20); runs *= 2){
//
//		perfmon_start();
//		for(int i = 0; i < runs; i++)
//			cblas_daxpy(n, alpha, x, 1, y, 1);
//		perfmon_stop(runs);
//
//		_printM(y, n, 1, "", devnull);
//
//		if(perfmon_testDerivative(runs, THRESHOLD))
//			break;
//		perfmon_emptyLists(false); //don't clear the vector of runs
//	}
//
//#ifdef COLD
//	_destroy(x); _destroy(y);
//#endif
//
//	cout << "\n\nUsing threshold " << THRESHOLD << " value for RUNS: " << runs << endl;
//
//	perfmon_emptyLists();
//
//#ifdef COLD
//	size_t factor = getNumberOfShifts(2*n*sizeof(double), runs*REP);
//
//	_buildRandInit(&x, n, 1, 4*1024, factor);
//	_buildRandInit(&y, n, 1, 4*1024, factor);
//
//	size_t idx = 0, maxidx = n*factor;
//#endif
//
//	for(int r = 0; r < REP; r++) {
//		perfmon_start();
//		for(int i = 0; i < runs; i++) {
//
//#ifdef COLD
//			cblas_daxpy(n, alpha, x+idx, 1, y+idx, 1);
//			idx+=n;  if (idx >= maxidx) idx = 0;
//#else
//			cblas_daxpy(n, alpha, x, 1, y, 1);
//#endif
//		}
//		perfmon_stop(runs);
//		_printM(y, n, 1, "", devnull);
//	}
//
//
//	_destroy(x); _destroy(y);
//}

int main(int argc, char** argv) {

	srand(time(NULL));
	measurement_init(events);

	size_t n = strtoul(argv[1], NULL, 0);

//	reduction(n);
	mmm(n);
//	mvm(n);
//	daxpy(n);

	measurement_end();

	return EXIT_SUCCESS;
}
