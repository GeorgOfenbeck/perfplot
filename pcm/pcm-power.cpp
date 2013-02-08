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
#ifdef _MSC_VER
#include "freegetopt/getopt.h"
#endif

void MySleep(int delay)
{
#ifdef _MSC_VER
    if(delay) Sleep(delay*1000);
#else
    ::sleep(delay);
#endif
}

void MySleepMs(int delay_ms)
{
#ifdef _MSC_VER
    if(delay_ms) Sleep(delay_ms);
#else
    ::sleep(delay_ms/1000);
#endif
}

void MySystem(char * sysCmd)
{
    std::cout << "\n Executing \"";
    std::cout << sysCmd;
    std::cout << "\" command:\n" << std::endl;
    system(sysCmd);
	std::cout << std::endl;
}

int getFirstRank(int imc_profile)
{
    return imc_profile*2;
}
int getSecondRank(int imc_profile)
{
    return (imc_profile*2)+1;
}

double getCKEOffResidency(uint32 channel, uint32 rank, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    return double(getMCCounter(channel,(rank&1)?2:0,before,after))/double(getDRAMClocks(channel,before,after));
}

int64 getCKEOffAverageCycles(uint32 channel, uint32 rank, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    uint64 div = getMCCounter(channel,(rank&1)?3:1,before,after);
    if(div)
      return getMCCounter(channel,(rank&1)?2:0,before,after)/div;
    
    return -1;
}

int64 getCyclesPerTransition(uint32 channel, uint32 rank, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    uint64 div = getMCCounter(channel,(rank&1)?3:1,before,after);
    if(div)
      return getDRAMClocks(channel,before,after)/div;
    
    return -1;
}

uint64 getSelfRefreshCycles(uint32 channel, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    return getMCCounter(channel,0,before,after);
}

uint64 getSelfRefreshTransitions(uint32 channel, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    return getMCCounter(channel,1,before,after);
}

double getNormalizedPCUCounter(uint32 counter, const JKTUncorePowerState & before, const JKTUncorePowerState & after)
{
    return double(getPCUCounter(counter, before, after))/double(getPCUClocks(before,after));
}

int default_freq_band[3] = {12,20,40};
int freq_band[3];

void print_usage(const char * progname)
{
	  std::cout << "\nUsage "<<progname<<" (delay | \"external_program\") [-m imc_profile] [-p pcu_profile] [-a freq_band0] [-b freq_band1] [-c freq_band2]\n\n";
      std::cout << "  <delay>            - delay in seconds between updates. Either delay or \"external program\" parameters must be supplied\n";
	  std::cout << "  \"external_program\" - start external program and print the performance metrics for the execution at the end\n";
      std::cout << "  <imc_profile>      - profile (counter group) for IMC PMU. Possible values are: 0,1,2,3,4,-1 \n";
	  std::cout << "                       profile  0 - rank 0 and rank 1 residencies (default) \n";
	  std::cout << "                       profile  1 - rank 2 and rank 3 residencies \n";
	  std::cout << "                       profile  2 - rank 4 and rank 5 residencies \n";
	  std::cout << "                       profile  3 - rank 6 and rank 7 residencies \n";
	  std::cout << "                       profile  4 - self-refresh residencies \n";
	  std::cout << "                       profile -1 - omit IMC PMU output\n";
      std::cout << "  <pcu_profile>      - profile (counter group) for PCU PMU. Possible values are: 0,1,2,3,4,5,-1 \n";
	  std::cout << "                       profile  0 - frequency residencies (default) \n";
	  std::cout << "                       profile  1 - core C-state residencies. The unit is the number of physical cores on the socket who were in C0, C3 or C6 during the measurement interval (e.g. 'C0 residency is 3.5' means on average 3.5 physical cores were resident in C0 state)\n";
	  std::cout << "                       profile  2 - Prochot (throttled) residencies and thermal frequency limit cycles \n";
	  std::cout << "                       profile  3 - {Thermal,Power,Clipped} frequency limit cycles \n";
	  std::cout << "                       profile  4 - {OS,Power,Clipped} frequency limit cycles \n";
          std::cout << "                       profile  5 - frequency transition statistics \n";
	  std::cout << "                       profile -1 - omit PCU PMU output\n";
	  std::cout << "  <freq_band0>       - frequency minumum for band 0 for PCU frequency residency profile [in 100MHz units] (default is "<<
		  default_freq_band[0] <<"= "<< 100*default_freq_band[0] <<"MHz)\n";
	  std::cout << "  <freq_band1>       - frequency minumum for band 1 for PCU frequency residency profile [in 100MHz units] (default is "<<
		  default_freq_band[1] <<"= "<< 100*default_freq_band[1]<<"MHz)\n";
	  std::cout << "  <freq_band2>       - frequency minumum for band 2 for PCU frequency residency profile [in 100MHz units] (default is "<<
		  default_freq_band[2] <<"= "<< 100*default_freq_band[2]<<"MHz)\n";
}

int main(int argc, char * argv[])
{
    std::cout << "\n Intel(r) Performance Counter Monitor " << INTEL_PCM_VERSION << std::endl;
    std::cout << "\n Intel(r) microarchitecture codename \"Sandy-Bridge EP\" Power Monitor\n Copyright (c) 2011-2012 Intel Corporation\n";
    
    int imc_profile = 0;
    int pcu_profile = 0;
    int delay = -1;
	char * ext_program = NULL;

	freq_band[0] = default_freq_band[0];
	freq_band[1] = default_freq_band[1];
	freq_band[2] = default_freq_band[2];


	int my_opt = -1;
	while ((my_opt = getopt(argc, argv, "m:p:a:b:c:")) != -1)
	{
		switch(my_opt)
		{
			case 'm':
				imc_profile = atoi(optarg);
				break;
			case 'p':
				pcu_profile = atoi(optarg);
				break;
			case 'a':
				freq_band[0] = atoi(optarg);
				break;
			case 'b':
				freq_band[1] = atoi(optarg);
				break;
			case 'c':
				freq_band[2] = atoi(optarg);
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

    delay = atoi(argv[optind]);
	if(delay == 0) 
		ext_program = argv[optind];
	else
		delay = (delay<0)?1:delay;

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
		std::cout << "Can not access CPU performance counters" << std::endl;
		std::cout << "You must have signed msr.sys driver in your current directory and have administrator rights to run this program" << std::endl;
        return -1;
    }
    #endif

    PCM * m = PCM::getInstance();
    m->disableJKTWorkaround();

    if(PCM::JAKETOWN != m->getCPUModel())
    {
       std::cout <<"Unsupported processor model ("<<m->getCPUModel()<<"). Only model "<<PCM::JAKETOWN<<" (JAKETOWN) is supported."<< std::endl;
       return -1;
    }

    if(PCM::Success != m->programSNB_EP_PowerMetrics(imc_profile,pcu_profile,freq_band))
    {
	  #ifdef _MSC_VER
		std::cout << "You must have signed msr.sys driver in your current directory and have administrator rights to run this program" << std::endl;
      #elif defined(__linux__)
      std::cout << "You need to be root and loaded 'msr' Linux kernel module to execute the program. You may load the 'msr' module with 'modprobe msr'. \n";
      #endif
      return -1;
    }
    JKTUncorePowerState * BeforeState = new JKTUncorePowerState[m->getNumSockets()];
	JKTUncorePowerState * AfterState = new JKTUncorePowerState[m->getNumSockets()];
	uint64 BeforeTime = 0, AfterTime = 0;
    
    std::cout << std::dec << std::endl;
    std::cout.precision(2);
    std::cout << std::fixed;
    std::cout << "\nMC counter group: "<<imc_profile << std::endl;
    std::cout << "PCU counter group: "<<pcu_profile << std::endl; 
	if(pcu_profile == 0)
	   std::cout << "Freq bands [0/1/2]: "<<freq_band[0]*100 << " MHz; "<< freq_band[1]*100 << " MHz; "<<freq_band[2]*100 << " MHz; "<<std::endl; 
    if(!ext_program) 
		std::cout << "Update every "<<delay<<" seconds"<< std::endl;

    uint32 i = 0;

	BeforeTime = m->getTickCount();
    for(i=0; i<m->getNumSockets(); ++i)
      BeforeState[i] = m->getJKTUncorePowerState(i); 
 
    while(1)
    {
      std::cout << "----------------------------------------------------------------------------------------------"<<std::endl;
      
	  #ifdef _MSC_VER
	  int delay_ms = delay * 1000;
	  // compensate slow Windows console output
	  if(AfterTime) delay_ms -= (int)(m->getTickCount() - BeforeTime);
	  if(delay_ms < 0) delay_ms = 0;
      #else
	  int delay_ms = delay * 1000;
      #endif

	  if(ext_program)
		MySystem(ext_program);
	  else
		MySleepMs(delay_ms);

	  AfterTime = m->getTickCount();
      for(i=0; i<m->getNumSockets(); ++i)
        AfterState[i] = m->getJKTUncorePowerState(i);
     
	  std::cout << "Time elapsed: "<<AfterTime-BeforeTime<<" ms\n";
	  std::cout << "Called sleep function for "<<delay_ms<<" ms\n";
      for(uint32 socket=0;socket<m->getNumSockets();++socket)
      {
	for(uint32 port=0;port<2;++port)
	{
	  std::cout << "S"<<socket<<"P"<<port
	    << "; QPIClocks: "<< getQPIClocks(port,BeforeState[socket],AfterState[socket])
	    << "; L0p Tx Cycles: "<< 100.*getNormalizedQPIL0pTxCycles(port,BeforeState[socket],AfterState[socket])<< "%"
	    << "; L1 Cycles: "    << 100.*getNormalizedQPIL1Cycles(port,BeforeState[socket],AfterState[socket])<< "%"
	    << "\n";
	}
	for(uint32 channel=0;channel<4;++channel)
	{
	  if(imc_profile <= 3 && imc_profile >= 0)
	  {
              std::cout << "S"<<socket<<"CH"<<channel <<"; DRAMClocks: "<< getDRAMClocks(channel,BeforeState[socket],AfterState[socket])
                 << "; Rank"<<getFirstRank(imc_profile)<<" CKE Off Residency: "<< std::setw(3) << 
				100.*getCKEOffResidency(channel,getFirstRank(imc_profile),BeforeState[socket],AfterState[socket])<<"%"
		<< "; Rank"<<getFirstRank(imc_profile)<<" CKE Off Average Cycles: "<< 
				getCKEOffAverageCycles(channel,getFirstRank(imc_profile),BeforeState[socket],AfterState[socket])
		<< "; Rank"<<getFirstRank(imc_profile)<<" Cycles per transition: "<< 
				getCyclesPerTransition(channel,getFirstRank(imc_profile),BeforeState[socket],AfterState[socket])
                << "\n";

              std::cout << "S"<<socket<<"CH"<<channel <<"; DRAMClocks: "<< getDRAMClocks(channel,BeforeState[socket],AfterState[socket])
                << "; Rank"<<getSecondRank(imc_profile)<<" CKE Off Residency: "<< std::setw(3) <<
			100.*getCKEOffResidency(channel,getSecondRank(imc_profile),BeforeState[socket],AfterState[socket])<<"%"
                << "; Rank"<<getSecondRank(imc_profile)<<" CKE Off Average Cycles: "<< 
			getCKEOffAverageCycles(channel,getSecondRank(imc_profile),BeforeState[socket],AfterState[socket])
                << "; Rank"<<getSecondRank(imc_profile)<<" Cycles per transition: "<< 
			getCyclesPerTransition(channel,getSecondRank(imc_profile),BeforeState[socket],AfterState[socket])
                << "\n";

               
	  } else if(imc_profile == 4)
	  {
	      std::cout << "S"<<socket<<"CH"<<channel
		<< "; DRAMClocks: "<< getDRAMClocks(channel,BeforeState[socket],AfterState[socket])
		<< "; Self-refresh cycles: "<< getSelfRefreshCycles(channel,BeforeState[socket],AfterState[socket])
		<< "; Self-refresh transitions: "<< getSelfRefreshTransitions(channel,BeforeState[socket],AfterState[socket])
		<< "\n";
	  }
	}
	    switch(pcu_profile)
        {
        case 0:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; Freq band 0/1/2 cycles: "<< 100.*getNormalizedPCUCounter(1,BeforeState[socket],AfterState[socket])<<"%"
             << "; "<< 100.*getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])<<"%"
             << "; "<< 100.*getNormalizedPCUCounter(3,BeforeState[socket],AfterState[socket])<<"%"
             << "\n";
			break;

		case 1:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; core C0/C3/C6-state residency: "<< getNormalizedPCUCounter(1,BeforeState[socket],AfterState[socket])
             << "; "<< getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])
             << "; "<< getNormalizedPCUCounter(3,BeforeState[socket],AfterState[socket])
             << "\n";
		  break;

		case 2:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; Internal prochot cycles: "<< getNormalizedPCUCounter(1,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "; External prochot cycles:" << getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "; Thermal freq limit cycles:" << getNormalizedPCUCounter(3,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "\n";
		  break;

		case 3:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; Thermal freq limit cycles: "<< getNormalizedPCUCounter(1,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "; Power freq limit cycles:" << getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "; Clipped freq limit cycles:" << getNormalizedPCUCounter(3,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "\n";
		  break;

		case 4:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; OS freq limit cycles: "<< getNormalizedPCUCounter(1,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "; Power freq limit cycles:" << getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])*100. <<" %"
			 << "; Clipped freq limit cycles:" << getNormalizedPCUCounter(3,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "\n";
		  break;
                case 5:
          std::cout << "S"<<socket
             << "; PCUClocks: "<< getPCUClocks(BeforeState[socket],AfterState[socket])
             << "; Frequency transition count: "<< getPCUCounter(1,BeforeState[socket],AfterState[socket]) <<" "
             << "; Cycles spent changing frequency: " << getNormalizedPCUCounter(2,BeforeState[socket],AfterState[socket])*100. <<" %"
             << "\n";
                  break;

        }

        std::cout << "S"<<socket
              << "; Consumed energy units: "<< getConsumedEnergy(BeforeState[socket],AfterState[socket])
              << "; Consumed Joules: "<< getConsumedJoules(BeforeState[socket],AfterState[socket])
			  << "; Watts: "<< 1000.*getConsumedJoules(BeforeState[socket],AfterState[socket])/double(AfterTime-BeforeTime)
              << "; Thermal headroom below TjMax: " << AfterState[socket].getPackageThermalHeadroom()
              << "\n";
        std::cout << "S"<<socket
              << "; Consumed DRAM energy units: "<< getDRAMConsumedEnergy(BeforeState[socket],AfterState[socket])
              << "; Consumed DRAM Joules: "<< getDRAMConsumedJoules(BeforeState[socket],AfterState[socket])
                          << "; DRAM Watts: "<< 1000.*getDRAMConsumedJoules(BeforeState[socket],AfterState[socket])/double(AfterTime-BeforeTime)
              << "\n";


      }
      std::swap(BeforeState,AfterState);
	  std::swap(BeforeTime,AfterTime);

	  if(ext_program)
	  {
		  std::cout << "----------------------------------------------------------------------------------------------"<<std::endl;
		  break;
	  }
    }

	delete [] BeforeState;
    delete [] AfterState;
}
