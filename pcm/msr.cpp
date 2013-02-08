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
//            Jim Harris (FreeBSD)

#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdio.h>
#ifndef _MSC_VER
#include <unistd.h>
#endif
#include "types.h"
#include "msr.h"
#include <assert.h>

#ifdef _MSC_VER

#include <windows.h>
#include "Winmsrdriver\win7\msrstruct.h"
#include "winring0/OlsDef.h"
#include "winring0/OlsApiInitExt.h"

extern HMODULE hOpenLibSys;

// here comes an implementatation for Windows
MsrHandle::MsrHandle(uint32 cpu) : cpu_id(cpu)
{
    hDriver = CreateFile(L"\\\\.\\RDMSR", GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);

    if (hDriver == INVALID_HANDLE_VALUE && hOpenLibSys == NULL)
        throw std::exception();
}

MsrHandle::~MsrHandle()
{
	if(hDriver != INVALID_HANDLE_VALUE) CloseHandle(hDriver);
}

#ifdef COMPILE_FOR_WINDOWS_7
class ThreadGroupTempAffinity
{
	GROUP_AFFINITY PreviousGroupAffinity;

	ThreadGroupTempAffinity(); // forbidden
	ThreadGroupTempAffinity(const ThreadGroupTempAffinity &); // forbidden
public:
	ThreadGroupTempAffinity(uint32 core_id)
	{
		GROUP_AFFINITY NewGroupAffinity;
		memset(&NewGroupAffinity, 0, sizeof(GROUP_AFFINITY));
		memset(&PreviousGroupAffinity, 0, sizeof(GROUP_AFFINITY));
		uint32 currentGroupSize = 0;

		while (core_id >= (currentGroupSize = GetMaximumProcessorCount(NewGroupAffinity.Group)))
		{
			core_id -= currentGroupSize;
			++NewGroupAffinity.Group;
		}
		NewGroupAffinity.Mask = 1ULL << core_id;
		SetThreadGroupAffinity(GetCurrentThread(),&NewGroupAffinity,&PreviousGroupAffinity);
	}
	~ThreadGroupTempAffinity()
	{
		SetThreadGroupAffinity(GetCurrentThread(),&PreviousGroupAffinity,NULL);
	}
};
#endif

int32 MsrHandle::write(uint64 msr_number, uint64 value)
{
	if(hDriver != INVALID_HANDLE_VALUE)
	{
		MSR_Request req;
		ULONG64 result;
		DWORD reslength = 0;
		req.core_id = cpu_id;
		req.msr_address = msr_number;
		req.write_value = value;
		BOOL status = DeviceIoControl(hDriver, IO_CTL_MSR_WRITE, &req, sizeof(MSR_Request), &result, sizeof(uint64), &reslength, NULL);
		assert(status && "Error in DeviceIoControl");
		return reslength;
	}

	cvt_ds cvt;
	cvt.ui64 = value;

	#ifdef COMPILE_FOR_WINDOWS_7
	ThreadGroupTempAffinity affinity(cpu_id);
	BOOL status = Wrmsr((DWORD)msr_number, cvt.ui32.low, cvt.ui32.high);
    #else
	BOOL status = WrmsrTx((DWORD)msr_number, cvt.ui32.low, cvt.ui32.high,(1UL << cpu_id));
    #endif

	return status?sizeof(uint64):0;
}

int32 MsrHandle::read(uint64 msr_number, uint64 * value)
{
	if(hDriver != INVALID_HANDLE_VALUE)
	{
		MSR_Request req;
		// ULONG64 result;
		DWORD reslength = 0;
		req.core_id = cpu_id;
		req.msr_address = msr_number;
		BOOL status = DeviceIoControl(hDriver, IO_CTL_MSR_READ, &req, sizeof(MSR_Request), value, sizeof(uint64), &reslength, NULL);
		assert(status && "Error in DeviceIoControl");
		return reslength;
	}

	cvt_ds cvt;
	cvt.ui64 = 0;

	#ifdef COMPILE_FOR_WINDOWS_7
	ThreadGroupTempAffinity affinity(cpu_id);
	BOOL status = Rdmsr((DWORD)msr_number, &(cvt.ui32.low), &(cvt.ui32.high));
    #else
	BOOL status = RdmsrTx((DWORD)msr_number, &(cvt.ui32.low), &(cvt.ui32.high), (1UL << cpu_id));
    #endif

	if(status) *value = cvt.ui64;
	
	return status?sizeof(uint64):0;
}

#elif __APPLE__
// OSX Version

MSRAccessor * MsrHandle::driver = NULL;
int MsrHandle::num_handles = 0;

MsrHandle::MsrHandle(uint32 cpu)
{
    cpu_id = cpu; 
    if(!driver)
    {    
        driver = new MSRAccessor();
	MsrHandle::num_handles = 1;
    }
    else
    {
        MsrHandle::num_handles++;
    }
}

MsrHandle::~MsrHandle()
{
    MsrHandle::num_handles--;
    if(MsrHandle::num_handles == 0)
    {
        delete driver;
        driver = NULL;
    }
}

int32 MsrHandle::write(uint64 msr_number, uint64 value)
{
    return driver->write(cpu_id, msr_number, value);	
}

int32 MsrHandle::read(uint64 msr_number, uint64 * value){
    return driver->read(cpu_id, msr_number, value);
}

int32 MsrHandle::buildTopology(uint32 num_cores, void* ptr){
    return driver->buildTopology(num_cores, ptr);
}

uint32 MsrHandle::getNumInstances(){
    return driver->getNumInstances();
}

uint32 MsrHandle::incrementNumInstances(){
    return driver->incrementNumInstances();
}

uint32 MsrHandle::decrementNumInstances(){
    return driver->decrementNumInstances();
}

#elif (defined __FreeBSD__)

#include <sys/ioccom.h>
#include <sys/cpuctl.h>
MsrHandle::MsrHandle(uint32 cpu) : fd(-1), cpu_id(cpu)
{
    char path[200];
    sprintf(path, "/dev/cpuctl%d", cpu);
    int handle = ::open(path, O_RDWR);
    if (handle < 0) throw std::exception();
    fd = handle;
}

MsrHandle::~MsrHandle()
{
    if (fd >= 0) ::close(fd);
}

int32 MsrHandle::write(uint64 msr_number, uint64 value)
{
    cpuctl_msr_args_t args;

    args.msr = msr_number;
    args.data = value;
    return ::ioctl(fd, CPUCTL_WRMSR, &args);
}

int32 MsrHandle::read(uint64 msr_number, uint64 * value)
{
    cpuctl_msr_args_t args;
    int32 ret;

    args.msr = msr_number;
    ret = ::ioctl(fd, CPUCTL_RDMSR, &args);
    if (!ret) *value = args.data;
    return ret;
}

#else
// here comes a Linux version
MsrHandle::MsrHandle(uint32 cpu) : fd(-1), cpu_id(cpu)
{
    char * path = new char[200];
    sprintf(path, "/dev/cpu/%d/msr", cpu);
    int handle = ::open(path, O_RDWR);
    if(handle < 0)
    { // try Android msr device path
      sprintf(path, "/dev/msr%d", cpu);
      handle = ::open(path, O_RDWR);
    }
    delete[] path;
    if (handle < 0) throw std::exception();
    fd = handle;
}

MsrHandle::~MsrHandle()
{
    if (fd >= 0) ::close(fd);
}

int32 MsrHandle::write(uint64 msr_number, uint64 value)
{
    return ::pwrite(fd, (const void *)&value, sizeof(uint64), msr_number);
}

int32 MsrHandle::read(uint64 msr_number, uint64 * value)
{
    return ::pread(fd, (void *)value, sizeof(uint64), msr_number);
}

#endif
