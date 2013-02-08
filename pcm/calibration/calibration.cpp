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

#include <mkl_cblas.h>

#include "../measuring_core.h"
#include "funcs.h"

using namespace std;

#define REP 20
#define THRESHOLD 0.0005

int main_mine() {
//int main() {

	srand(time(NULL));
	ofstream devnull("/dev/null");

	size_t n = 128;
	double * x = (double*)malloc(n*sizeof(double)), dest;
	for(size_t j = 0; j < n; j++) x[j] = rand()/(double)RAND_MAX;

	perfmon_init(1, false, false, false);

	size_t runs = 16;
	for(; runs <= (1 << 20); runs *= 2){
//		for(int r = 0; r < 2; r++) {
		perfmon_start();
		for(int i = 0; i < runs; i++)
			reduction_K4_L4(x, &dest, n);
		perfmon_stop();
//		}
		devnull << dest;

		if(perfmon_testDerivative(runs, THRESHOLD))
			break;
		perfmon_emptyLists();
		dumpMeans();
	}

	cout << "\n\nUsing threshold " << THRESHOLD << " value for RUNS: " << runs << endl;

	perfmon_emptyLists();
	dumpMeans();

	for(int r = 0; r < REP; r++) {
		perfmon_start();
		for(int i = 0; i < runs; i++)
			reduction_K4_L4(x, &dest, n);
		perfmon_stop();
	}
	devnull << dest;


	perfmon_end();


	return EXIT_SUCCESS;
}

int main_varyingRep() {
//int main() {

	ofstream devnull("/dev/null");

	size_t n = 100000;
	double * x = (double*)malloc(n*sizeof(double)), dest;
	for(size_t j = 0; j < n; j++) x[j] = 1.;

	perfmon_init(1, false, false, false);

	for(size_t rep=2; rep < REP; rep++) {
		for(size_t r = 0; r < rep; r++) {
			perfmon_start();
			for(int i = 0; i < 32768; i++)
				reduction_K4_L4(x, &dest, n);
			perfmon_stop();
		}
		devnull << dest;

		perfmon_testDerivative(32768, THRESHOLD);
//		perfmon_meanSingleRun();
		dumpMeans();
		perfmon_emptyLists();
	}

	perfmon_end();


	return EXIT_SUCCESS;
}

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

void _build(double ** A, double ** B, double ** C, size_t M, size_t K, size_t N)
{
  *A = new double[M*K];
  *B = new double[K*N];
  *C = new double[M*N];

  _rands(*A, M, K);
  _rands(*B, K, N);
  _rands(*C, M, N);

}

void _destroy(double * A, double * B, double * C)
{
  _mm_free(A);
  _mm_free(B);
  _mm_free(C);
}


int main() {

	srand(time(NULL));
	perfmon_init(1, false, false, false);

	ofstream devnull("/dev/null");
	double * A, * B, * C;
	size_t n = 1024;

	_build(&A, &B, &C, n, n, n);

	size_t runs = 16;
	for(; runs <= (1 << 20); runs *= 2){

		perfmon_start();
		for(int i = 0; i < runs; i++)
			cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, n, n, n, 1., A, n, B, n, 0., C, n);
		perfmon_stop(runs);

		_printM(C, n, n, "", devnull);

		if(perfmon_testDerivative(runs, THRESHOLD))
			break;
		perfmon_emptyLists(false); //don't clear the vector of runs
		dumpMeans();
	}

	cout << "\n\nUsing threshold " << THRESHOLD << " value for RUNS: " << runs << endl;

	dumpMeans();
	perfmon_emptyLists();

	for(int r = 0; r < REP; r++) {
		perfmon_start();
		for(int i = 0; i < runs; i++)
			cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, n, n, n, 1., A, n, B, n, 0., C, n);
		perfmon_stop(runs);
	}

	_printM(C, n, n, "C", devnull);

	perfmon_end();


	return EXIT_SUCCESS;
}
