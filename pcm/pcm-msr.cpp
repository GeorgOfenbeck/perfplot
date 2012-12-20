/*
Copyright (c) 2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// written by Roman Dementiev
#define HACK_TO_REMOVE_DUPLICATE_ERROR
#include "cpucounters.h"
#ifdef _MSC_VER
#pragma warning(disable : 4996) // for sprintf
#include <windows.h>
#include "../PCM_Win/windriver.h"
#else
#include <unistd.h>
#endif
#include <iostream>
#include <stdlib.h>
#include <iomanip>
#include <string.h>
#ifdef _MSC_VER
#include "freegetopt/getopt.h"
#endif

uint64 read_number(char * str)
{
        std::istringstream stream(str);        
	if(strstr(str,"x")) stream >> std::hex ;
        uint64 result = 0;
        stream >> result;
        return result;
}

void print_usage(const char * progname)
{
	  std::cout << "Usage "<<progname<<" [-w value] [-c core] [-d] msr\n\n";
	  std::cout << "  Reads specified msr (model specific register) \n";
	  std::cout << "   -w value : write the value before reading \n";
	  std::cout << "   -c core  : perform msr read/write on specified core (default is 0)\n";
          std::cout << "   -d       : output all numbers in dec (default is hex)\n";
          std::cout << "\n";
}

int main(int argc, char * argv[])
{
    std::cout << "\n Intel(r) Performance Counter Monitor " << INTEL_PCM_VERSION << std::endl;
    std::cout << "\n MSR read/write utility\n\n";
    
    uint64 value = 0;
    bool write = false;
    int core = 0;
    int msr = -1;
    bool dec = false;

	int my_opt = -1;
	while ((my_opt = getopt(argc, argv, "w:c:d")) != -1)
	{
		switch(my_opt)
		{
			case 'w':
                                write = true;
				value = read_number(optarg);
				break;
			case 'c':
				core = (int) read_number(optarg);
				break;
                        case 'd':
                                dec = true;
                                break;
			default:
				print_usage(argv[0]);
				return -1;
		}
	}

	 if (optind >= argc)
	 {
		 print_usage(argv[0]);
		 return -1;
	 }

    msr = (int) read_number(argv[optind]);

    #ifdef _MSC_VER
    // Increase the priority a bit to improve context switching delays on Windows
    SetThreadPriority(GetCurrentThread(), THREAD_PRIORITY_ABOVE_NORMAL);

    TCHAR driverPath[1024];
    GetCurrentDirectory(1024, driverPath);
    wcscat(driverPath, L"\\msr.sys");

    // WARNING: This driver code (msr.sys) is only for testing purposes, not for production use
    Driver drv;
    // drv.stop();     // restart driver (usually not needed)
    if (!drv.start(driverPath))
    {
		std::cout << "Can not load MSR driver." << std::endl;
		std::cout << "You must have signed msr.sys driver in your current directory and have administrator rights to run this program" << std::endl;
        return -1;
    }
    #endif

    MsrHandle h(core);
    if(!dec) std::cout << std::hex << std::showbase;
    if(write)
    {
        std::cout << " Writing "<< value << " to MSR "<< msr << " on core "<< core << std::endl;
        h.write(msr,value);
    }
    value = 0;
    h.read(msr,&value);
    std::cout << " Read value "<< value << " from MSR "<< msr << " on core "<< core << "\n" << std::endl;
}
