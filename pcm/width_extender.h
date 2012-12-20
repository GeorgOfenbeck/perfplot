/*
Copyright (c) 2009-2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
// written by Roman Dementiev
//            Austen Ott

#ifndef WIDTH_EXTENDER_HEADER_
#define WIDTH_EXTENDER_HEADER_ 

#ifdef _MSC_VER
#include <windows.h>
#else
#include <pthread.h>
#endif

#include <stdlib.h>
#include "cpucounters.h"

#ifdef _MSC_VER
DWORD WINAPI WatchDogProc(LPVOID state);
#else
void * WatchDogProc(void * state);
#endif

class CounterWidthExtender
{

#ifdef _MSC_VER
	HANDLE UpdateThread;
	HANDLE CounterMutex;
#else
    pthread_t UpdateThread;
    pthread_mutex_t CounterMutex;
#endif

    MsrHandle * msr;
    uint64 msr_addr;
    uint64 extended_value;
    uint64 last_raw_value;

    CounterWidthExtender(); // forbidden
    CounterWidthExtender(CounterWidthExtender&); // forbidden

    uint64 internal_read()
    {
	 uint64 result = 0, new_raw_value = 0;
#ifdef _MSC_VER
	 WaitForSingleObject(CounterMutex,INFINITE);
#else
	 pthread_mutex_lock(&CounterMutex);
#endif
         msr->read(msr_addr,&new_raw_value);
	 if(new_raw_value < last_raw_value)
         {
                extended_value += ((1ULL<<32ULL)-last_raw_value) + new_raw_value;
         }
	 else
	 {
		extended_value += (new_raw_value-last_raw_value);
         }

         last_raw_value = new_raw_value;

         result = extended_value;	
#ifdef _MSC_VER
	 ReleaseMutex(CounterMutex);
#else
	 pthread_mutex_unlock(&CounterMutex);
#endif
         return result;
    }

public:
    CounterWidthExtender(MsrHandle * msr_, uint64 msr_addr_): msr(msr_), msr_addr(msr_addr_)
    {
        msr->read(msr_addr,&last_raw_value);
        extended_value = last_raw_value;

#ifdef _MSC_VER
		CounterMutex = CreateMutex(NULL,FALSE,NULL);
		UpdateThread = CreateThread(NULL,0,(LPTHREAD_START_ROUTINE)WatchDogProc,this,0,NULL);
#else
        pthread_mutex_init(&CounterMutex, NULL);
        pthread_create(&UpdateThread, NULL, WatchDogProc, this);
#endif
    }
    ~CounterWidthExtender()
    {
#ifdef _MSC_VER
		TerminateThread(UpdateThread,0);
		CloseHandle(UpdateThread);
		CloseHandle(CounterMutex);
#else
        pthread_cancel(UpdateThread);
        pthread_mutex_destroy(&CounterMutex);
#endif
    }
    
    uint64 read() // read extended value
    {
	return internal_read();
    }
};


#endif
