/*
 * Reduction functions for K and L in {1..6} with K|L
 */
 
void reduction_K1_L1(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%1;
  for(i = 0; i < limit; i+=1) {
    t0 += v[i+0];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K1_L2(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%2;
  for(i = 0; i < limit; i+=2) {
    t0 += v[i+0];
    t0 += v[i+1];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K1_L3(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%3;
  for(i = 0; i < limit; i+=3) {
    t0 += v[i+0];
    t0 += v[i+1];
    t0 += v[i+2];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K1_L4(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%4;
  for(i = 0; i < limit; i+=4) {
    t0 += v[i+0];
    t0 += v[i+1];
    t0 += v[i+2];
    t0 += v[i+3];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K1_L5(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%5;
  for(i = 0; i < limit; i+=5) {
    t0 += v[i+0];
    t0 += v[i+1];
    t0 += v[i+2];
    t0 += v[i+3];
    t0 += v[i+4];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K1_L6(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;

  long int limit = n - n%6;
  for(i = 0; i < limit; i+=6) {
    t0 += v[i+0];
    t0 += v[i+1];
    t0 += v[i+2];
    t0 += v[i+3];
    t0 += v[i+4];
    t0 += v[i+5];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0;

}

void reduction_K2_L2(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;

  long int limit = n - n%2;
  for(i = 0; i < limit; i+=2) {
    t0 += v[i+0];
    t1 += v[i+1];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1;

}

void reduction_K2_L4(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;

  long int limit = n - n%4;
  for(i = 0; i < limit; i+=4) {
    t0 += v[i+0];
    t0 += v[i+1];
    t1 += v[i+2];
    t1 += v[i+3];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1;

}

void reduction_K2_L6(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;

  long int limit = n - n%6;
  for(i = 0; i < limit; i+=6) {
    t0 += v[i+0];
    t0 += v[i+1];
    t0 += v[i+2];
    t1 += v[i+3];
    t1 += v[i+4];
    t1 += v[i+5];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1;

}

void reduction_K3_L3(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;
  double t2 = 0.;

  long int limit = n - n%3;
  for(i = 0; i < limit; i+=3) {
    t0 += v[i+0];
    t1 += v[i+1];
    t2 += v[i+2];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1 + t2;

}

void reduction_K3_L6(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;
  double t2 = 0.;

  long int limit = n - n%6;
  for(i = 0; i < limit; i+=6) {
    t0 += v[i+0];
    t0 += v[i+1];
    t1 += v[i+2];
    t1 += v[i+3];
    t2 += v[i+4];
    t2 += v[i+5];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1 + t2;

}

void reduction_K4_L4(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;
  double t2 = 0.;
  double t3 = 0.;

  long int limit = n - n%4;
  for(i = 0; i < limit; i+=4) {
    t0 += v[i+0];
    t1 += v[i+1];
    t2 += v[i+2];
    t3 += v[i+3];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1 + t2 + t3;

}

void reduction_K5_L5(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;
  double t2 = 0.;
  double t3 = 0.;
  double t4 = 0.;

  long int limit = n - n%5;
  for(i = 0; i < limit; i+=5) {
    t0 += v[i+0];
    t1 += v[i+1];
    t2 += v[i+2];
    t3 += v[i+3];
    t4 += v[i+4];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1 + t2 + t3 + t4;

}

void reduction_K6_L6(double * v, double * dest, long int n) {

  long int i;
  double t0 = 0.;
  double t1 = 0.;
  double t2 = 0.;
  double t3 = 0.;
  double t4 = 0.;
  double t5 = 0.;

  long int limit = n - n%6;
  for(i = 0; i < limit; i+=6) {
    t0 += v[i+0];
    t1 += v[i+1];
    t2 += v[i+2];
    t3 += v[i+3];
    t4 += v[i+4];
    t5 += v[i+5];
  }

  for(; i < n; i++) {
    t0 += v[i];
  }

  *dest = t0 + t1 + t2 + t3 + t4 + t5;

}

void mmm_triple(double const * A, double const * B, double * C, unsigned sizeM, unsigned sizeK, unsigned sizeN)
{
	for(unsigned i = 0; i < sizeM; i++)
		for (unsigned j = 0; j < sizeN; ++j) {
			*C = 0.;
			for (unsigned k = 0; k < sizeK; ++k) {
				C[i*sizeN+j] += A[i*sizeK+k]*B[k*sizeN+j];
			}
		}
}
