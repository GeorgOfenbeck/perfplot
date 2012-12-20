/*
Copyright (c) 2009-2012, Intel Corporation
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Intel Corporation nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
//
// monitor CPU conters for ksysguard
//
// contact: Thomas Willhalm, Patrick Ungerer, Roman Dementiev
//
// This program is not a tutorial on how to write nice interpreters
// but a proof of concept on using ksysguard with performance counters
//

/*!     \file pcm-sensor.cpp
        \brief Example of using CPU counters: implements a graphical plugin for KDE ksysguard
*/
#define HACK_TO_REMOVE_DUPLICATE_ERROR
#include <iostream>
#include <string>
#include <sstream>
#include "cpuasynchcounter.h"

using namespace std;

int main()
{
    AsynchronCounterState counters;

    cout << "CPU counter sensor "<< INTEL_PCM_VERSION << endl;
    cout << "(C) 2010,2012 Intel Corp." << endl;
    cout << "ksysguardd 1.2.0" << endl;
    cout << "ksysguardd> ";

    while (1)
    {
        string s;
        cin >> s;

        // list counters
        if (s == "monitors") {
            for (uint32 i = 0; i < counters.getNumCores(); ++i) {
                for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                    if (a == counters.getSocketId(i)) {
                        cout << "Socket" << a << "/CPU" << i << "/Frequency\tfloat" << endl;
                        cout << "Socket" << a << "/CPU" << i << "/IPC\tfloat" << endl;
                        cout << "Socket" << a << "/CPU" << i << "/L2CacheHitRatio\tfloat" << endl;
                        cout << "Socket" << a << "/CPU" << i << "/L3CacheHitRatio\tfloat" << endl;
                        cout << "Socket" << a << "/CPU" << i << "/L2CacheMisses\tinteger" << endl;
                        cout << "Socket" << a << "/CPU" << i << "/L3CacheMisses\tinteger" << endl;
			cout << "Socket" << a << "/CPU" << i << "/CoreC0StateResidency\tfloat" << endl;
			cout << "Socket" << a << "/CPU" << i << "/CoreC3StateResidency\tfloat" << endl;
			cout << "Socket" << a << "/CPU" << i << "/CoreC6StateResidency\tfloat" << endl;
			cout << "Socket" << a << "/CPU" << i << "/CoreC7StateResidency\tfloat" << endl;
			cout << "Socket" << a << "/CPU" << i << "/ThermalHeadroom\tinteger" << endl;
                    }
            }
            for (uint32 a = 0; a < counters.getNumSockets(); ++a) {
                cout << "Socket" << a << "/BytesReadFromMC\tfloat" << endl;
                cout << "Socket" << a << "/BytesWrittenToMC\tfloat" << endl;
                cout << "Socket" << a << "/Frequency\tfloat" << endl;
                cout << "Socket" << a << "/IPC\tfloat" << endl;
                cout << "Socket" << a << "/L2CacheHitRatio\tfloat" << endl;
                cout << "Socket" << a << "/L3CacheHitRatio\tfloat" << endl;
                cout << "Socket" << a << "/L2CacheMisses\tinteger" << endl;
                cout << "Socket" << a << "/L3CacheMisses\tinteger" << endl;
		cout << "Socket" << a << "/CoreC0StateResidency\tfloat" << endl;
		cout << "Socket" << a << "/CoreC3StateResidency\tfloat" << endl;
		cout << "Socket" << a << "/CoreC6StateResidency\tfloat" << endl;
		cout << "Socket" << a << "/CoreC7StateResidency\tfloat" << endl;
		cout << "Socket" << a << "/PackageC2StateResidency\tfloat" << endl;		
		cout << "Socket" << a << "/PackageC3StateResidency\tfloat" << endl;		
		cout << "Socket" << a << "/PackageC6StateResidency\tfloat" << endl;		
		cout << "Socket" << a << "/PackageC7StateResidency\tfloat" << endl;		
		cout << "Socket" << a << "/ThermalHeadroom\tinteger" << endl;
		cout << "Socket" << a << "/CPUEnergy\tfloat" << endl;
		cout << "Socket" << a << "/DRAMEnergy\tfloat" << endl;
            }
            for (uint32 a = 0; a < counters.getNumSockets(); ++a) {
                for (uint32 l = 0; l < counters.getQPILinksPerSocket(); ++l)
                    cout << "Socket" << a << "/BytesIncomingToQPI" << l << "\tfloat" << endl;
            }

            cout << "QPI_Traffic\tfloat" << endl;
            cout << "Frequency\tfloat" << endl;
            cout << "IPC\tfloat" << endl;       //double check output
            cout << "L2CacheHitRatio\tfloat" << endl;
            cout << "L3CacheHitRatio\tfloat" << endl;
            cout << "L2CacheMisses\tinteger" << endl;
            cout << "L3CacheMisses\tinteger" << endl;
	    cout << "CoreC0StateResidency\tfloat" << endl;
            cout << "CoreC3StateResidency\tfloat" << endl;
            cout << "CoreC6StateResidency\tfloat" << endl;
            cout << "CoreC7StateResidency\tfloat" << endl;
            cout << "PackageC2StateResidency\tfloat" << endl;	    
            cout << "PackageC3StateResidency\tfloat" << endl;	    
            cout << "PackageC6StateResidency\tfloat" << endl;	    
            cout << "PackageC7StateResidency\tfloat" << endl;	    
	    cout << "CPUEnergy\tfloat" << endl;
            cout << "DRAMEnergy\tfloat" << endl;
        }

        // provide metadata

        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/Frequency?";
                    if (s == c.str()) {
                        cout << "FREQ. CPU" << i << "\t\t\tMHz" << endl;
                    }
		  }
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/ThermalHeadroom?";
                    if (s == c.str()) {
                        cout << "Temperature reading in 1 degree Celsius relative to the TjMax temperature (thermal headroom) for CPU" << i << "\t\t\t°C" << endl;
                    }
		  }
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/CoreC0StateResidency?";
                    if (s == c.str()) {
                        cout << "core C0-state residency for CPU" << i << "\t\t\t%" << endl;
                    }
		  }		  
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/CoreC3StateResidency?";
                    if (s == c.str()) {
                        cout << "core C3-state residency for CPU" << i << "\t\t\t%" << endl;
                    }
		  }
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/CoreC6StateResidency?";
                    if (s == c.str()) {
                        cout << "core C6-state residency for CPU" << i << "\t\t\t%" << endl;
                    }
		  }
		  {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/CoreC7StateResidency?";
                    if (s == c.str()) {
                        cout << "core C7-state residency for CPU" << i << "\t\t\t%" << endl;
                    }
		  }		  
                }
        }
        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/IPC?";
                    if (s == c.str()) {
                        cout << "IPC CPU" << i << "\t0\t\t" << endl;
                        //cout << "CPU" << i << "\tInstructions per Cycle\t0\t1\t " << endl;
                    }
                }
        }
        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/L2CacheHitRatio?";
                    if (s == c.str()) {
                        cout << "L2 Cache Hit Ratio CPU" << i << "\t0\t\t" << endl;
                        //   cout << "CPU" << i << "\tL2 Cache Hit Ratio\t0\t1\t " << endl;
                    }
                }
        }
        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/L3CacheHitRatio?";
                    if (s == c.str()) {
                        cout << "L3 Cache Hit Ratio CPU" << i << "\t0\t\t " << endl;
                    }
                }
        }
        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/L2CacheMisses?";
                    if (s == c.str()) {
                        cout << "L2 Cache Misses CPU" << i << "\t0\t\t " << endl;
                        //cout << "CPU" << i << "\tL2 Cache Misses\t0\t1\t " << endl;
                    }
                }
        }
        for (uint32 i = 0; i < counters.getNumCores(); ++i) {
            for (uint32 a = 0; a < counters.getNumSockets(); ++a)
                if (a == counters.getSocketId(i)) {
                    stringstream c;
                    c << "Socket" << a << "/CPU" << i << "/L3CacheMisses?";
                    if (s == c.str()) {
                        cout << "L3 Cache Misses CPU" << i << "\t0\t\t " << endl;
                        //cout << "CPU" << i << "\tL3 Cache Misses\t0\t1\t " << endl;
                    }
                }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/BytesReadFromMC?";
            if (s == c.str()) {
                cout << "read from MC Socket" << i << "\t0\t\tGB" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/DRAMEnergy?";
            if (s == c.str()) {
                cout << "Energy consumed by DRAM on socket " << i << "\t0\t\tJoule" << endl;
            }
        }        
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/CPUEnergy?";
            if (s == c.str()) {
                cout << "Energy consumed by CPU package " << i << "\t0\t\tJoule" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/ThermalHeadroom?";
            if (s == c.str()) {
                cout << "Temperature reading in 1 degree Celsius relative to the TjMax temperature (thermal headroom) for CPU package " << i << "\t0\t\t°C" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/CoreC0StateResidency?";
            if (s == c.str()) {
                cout << "core C0-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/CoreC3StateResidency?";
            if (s == c.str()) {
                cout << "core C3-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/CoreC6StateResidency?";
            if (s == c.str()) {
                cout << "core C6-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }        
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/CoreC7StateResidency?";
            if (s == c.str()) {
                cout << "core C7-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/PackageC2StateResidency?";
            if (s == c.str()) {
                cout << "package C2-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/PackageC3StateResidency?";
            if (s == c.str()) {
                cout << "package C3-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }        
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/PackageC6StateResidency?";
            if (s == c.str()) {
                cout << "package C6-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }        
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/PackageC7StateResidency?";
            if (s == c.str()) {
                cout << "package C7-state residency for CPU package " << i << "\t0\t\t%" << endl;
            }
        }        
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/BytesWrittenToMC?";
            if (s == c.str()) {
                cout << "written to MC Socket" << i << "\t0\t\tGB" << endl;
                //cout << "CPU" << i << "\tBytes written to memory channel\t0\t1\t GB" << endl;
            }
        }

        for (uint32 l = 0; l < counters.getQPILinksPerSocket(); ++l) {
            for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
                stringstream c;
                c << "Socket" << i << "/BytesIncomingToQPI" << l << "?";
                if (s == c.str()) {
                    //cout << "Socket" << i << "\tBytes incoming to QPI link\t" << l<< "\t\t GB" << endl;
                    cout << "incoming to Socket" << i << " QPI Link" << l << "\t0\t\tGB" << endl;
                }
            }
        }

        {
            stringstream c;
            c << "QPI_Traffic?";
            if (s == c.str()) {
                cout << "Traffic on all QPIs\t0\t\tGB" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/Frequency?";
            if (s == c.str()) {
                cout << "Socket" << i << " Frequency\t0\t\tMHz" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/IPC?";
            if (s == c.str()) {
                cout << "Socket" << i << " IPC\t0\t\t" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/L2CacheHitRatio?";
            if (s == c.str()) {
                cout << "Socket" << i << " L2 Cache Hit Ratio\t0\t\t" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/L3CacheHitRatio?";
            if (s == c.str()) {
                cout << "Socket" << i << " L3 Cache Hit Ratio\t0\t\t" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/L2CacheMisses?";
            if (s == c.str()) {
                cout << "Socket" << i << " L2 Cache Misses\t0\t\t" << endl;
            }
        }

        for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
            stringstream c;
            c << "Socket" << i << "/L3CacheMisses?";
            if (s == c.str()) {
                cout << "Socket" << i << " L3 Cache Misses\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "Frequency?";
            if (s == c.str()) {
                cout << "Frequency system wide\t0\t\tMhz" << endl;
            }
        }

        {
            stringstream c;
            c << "IPC?";
            if (s == c.str()) {
                cout << "IPC system wide\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "L2CacheHitRatio?";
            if (s == c.str()) {
                cout << "System wide L2 Cache Hit Ratio\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "L3CacheHitRatio?";
            if (s == c.str()) {
                cout << "System wide L3 Cache Hit Ratio\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "L2CacheMisses?";
            if (s == c.str()) {
                cout << "System wide L2 Cache Misses\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "L3CacheMisses?";
            if (s == c.str()) {
                cout << "System wide L3 Cache Misses\t0\t\t" << endl;
            }
        }

        {
            stringstream c;
            c << "L3CacheMisses?";
            if (s == c.str()) {
                cout << "System wide L3 Cache Misses\t0\t\t" << endl;
            }
        }
        
        {
            stringstream c;
            c << "DRAMEnergy?";
            if (s == c.str()) {
                cout << "System wide energy consumed by DRAM \t0\t\tJoule" << endl;
            }
        }        
        {
            stringstream c;
            c << "CPUEnergy?";
            if (s == c.str()) {
                cout << "System wide energy consumed by CPU packages \t0\t\tJoule" << endl;
            }
        }
        {
            stringstream c;
            c << "CoreC0StateResidency?";
            if (s == c.str()) {
                cout << "System wide core C0-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c << "CoreC3StateResidency?";
            if (s == c.str()) {
                cout << "System wide core C3-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c << "CoreC6StateResidency?";
            if (s == c.str()) {
                cout << "System wide core C6-state residency \t0\t\t%" << endl;
            }
        }        
        {
            stringstream c;
            c << "CoreC7StateResidency?";
            if (s == c.str()) {
                cout << "System wide core C7-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c  << "PackageC2StateResidency?";
            if (s == c.str()) {
                cout << "System wide package C2-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c  << "PackageC3StateResidency?";
            if (s == c.str()) {
                cout << "System wide package C3-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c  << "PackageC6StateResidency?";
            if (s == c.str()) {
                cout << "System wide package C6-state residency \t0\t\t%" << endl;
            }
        }
        {
            stringstream c;
            c  << "PackageC7StateResidency?";
            if (s == c.str()) {
                cout << "System wide package C7-state residency \t0\t\t%" << endl;
            }
        }        

        // sensors

#define OUTPUT_CORE_METRIC(name,function) \
        for (uint32 i = 0; i < counters.getNumCores(); ++i) { \
            for (uint32 a = 0; a < counters.getNumSockets(); ++a) \
                if (a == counters.getSocketId(i)) { \
                    stringstream c; \
                    c << "Socket" << a << "/CPU" << i << name; \
                    if (s == c.str()) { \
                        cout << function << endl; \
                    } \
                } \
        }
        
        OUTPUT_CORE_METRIC("/Frequency", (counters.get<double, ::getAverageFrequency>(i) / 1000000 ) )
	OUTPUT_CORE_METRIC("/IPC", (counters.get<double, ::getIPC>(i) ) )
	OUTPUT_CORE_METRIC("/L2CacheHitRatio", (counters.get<double, ::getL2CacheHitRatio>(i) ) )
	OUTPUT_CORE_METRIC("/L3CacheHitRatio", (counters.get<double, ::getL3CacheHitRatio>(i) ) )
        OUTPUT_CORE_METRIC("/L2CacheMisses", (counters.get<uint64, ::getL2CacheMisses>(i) / 1000000) )
        OUTPUT_CORE_METRIC("/L3CacheMisses", (counters.get<uint64, ::getL3CacheMisses>(i) / 1000000) )
        OUTPUT_CORE_METRIC("/CoreC0StateResidency", (counters.get<double, ::getCoreC0Residency>(i)*100.) )
        OUTPUT_CORE_METRIC("/CoreC3StateResidency", (counters.get<double, ::getCoreC3Residency>(i)*100.) )
        OUTPUT_CORE_METRIC("/CoreC6StateResidency", (counters.get<double, ::getCoreC6Residency>(i)*100.) )
        OUTPUT_CORE_METRIC("/CoreC7StateResidency", (counters.get<double, ::getCoreC7Residency>(i)*100.) )
        OUTPUT_CORE_METRIC("/ThermalHeadroom", (counters.get<int32, ::getThermalHeadroom>(i)) )

        #define OUTPUT_SOCKET_METRIC(name, function) \
        for (uint32 i = 0; i < counters.getNumSockets(); ++i) { \
            stringstream c; \
            c << "Socket" << i << name; \
            if (s == c.str()) { \
                cout << function << endl; \
            } \
        }
        
        OUTPUT_SOCKET_METRIC("/DRAMEnergy", (counters.getSocket<double, ::getDRAMConsumedJoules>(i)) )
        OUTPUT_SOCKET_METRIC("/CPUEnergy", (counters.getSocket<double, ::getConsumedJoules>(i)) )
        OUTPUT_SOCKET_METRIC("/CoreC0StateResidency", (counters.getSocket<double, ::getCoreC0Residency>(i)*100.) )
        OUTPUT_SOCKET_METRIC("/CoreC3StateResidency", (counters.getSocket<double, ::getCoreC3Residency>(i)*100.) )
        OUTPUT_SOCKET_METRIC("/CoreC6StateResidency", (counters.getSocket<double, ::getCoreC6Residency>(i)*100.) )
        OUTPUT_SOCKET_METRIC("/CoreC7StateResidency", (counters.getSocket<double, ::getCoreC7Residency>(i)*100.) )
        OUTPUT_SOCKET_METRIC("/PackageC2StateResidency", (counters.getSocket<double, ::getPackageC2Residency>(i)*100.) )        
        OUTPUT_SOCKET_METRIC("/PackageC3StateResidency", (counters.getSocket<double, ::getPackageC3Residency>(i)*100.) )        
        OUTPUT_SOCKET_METRIC("/PackageC6StateResidency", (counters.getSocket<double, ::getPackageC6Residency>(i)*100.) )        
        OUTPUT_SOCKET_METRIC("/PackageC7StateResidency", (counters.getSocket<double, ::getPackageC7Residency>(i)*100.) )        
        OUTPUT_SOCKET_METRIC("/ThermalHeadroom", (counters.getSocket<int32, ::getThermalHeadroom>(i)) )
        OUTPUT_SOCKET_METRIC("/BytesReadFromMC", (double(counters.getSocket<uint64, ::getBytesReadFromMC>(i)) / 1024 / 1024 / 1024) )
        OUTPUT_SOCKET_METRIC("/BytesWrittenToMC", (double(counters.getSocket<uint64, ::getBytesWrittenToMC>(i)) / 1024 / 1024 / 1024 ) )
        OUTPUT_SOCKET_METRIC("/Frequency", (counters.getSocket<double, ::getAverageFrequency>(i) / 1000000 ) )
        OUTPUT_SOCKET_METRIC("/IPC", (counters.getSocket<double, ::getIPC>(i) ) )
        OUTPUT_SOCKET_METRIC("/L2CacheHitRatio", (counters.getSocket<double, ::getL2CacheHitRatio>(i) ) )
        OUTPUT_SOCKET_METRIC("/L3CacheHitRatio", (counters.getSocket<double, ::getL3CacheHitRatio>(i) ) )
        OUTPUT_SOCKET_METRIC("/L2CacheMisses", (counters.getSocket<uint64, ::getL2CacheMisses>(i) ) )
        OUTPUT_SOCKET_METRIC("/L3CacheMisses", (counters.getSocket<uint64, ::getL3CacheMisses>(i) ) )

        for (uint32 l = 0; l < counters.getQPILinksPerSocket(); ++l) {
            for (uint32 i = 0; i < counters.getNumSockets(); ++i) {
                stringstream c;
                c << "Socket" << i << "/BytesIncomingToQPI" << l;
                if (s == c.str()) {
                    cout << double(counters.getSocket<uint64, ::getIncomingQPILinkBytes>(i, l)) / 1024 / 1024 / 1024 << endl;
                }
            }
        }

	#define OUTPUT_SYSTEM_METRIC(name, function) \
	{ \
            stringstream c; \
            c << name; \
            if (s == c.str()) { \
                cout << function << endl; \
            } \
        }
        
        OUTPUT_SYSTEM_METRIC("DRAMEnergy", (counters.getSystem<double, ::getDRAMConsumedJoules>()) )
        OUTPUT_SYSTEM_METRIC("CPUEnergy", (counters.getSystem<double, ::getConsumedJoules>()) )
        OUTPUT_SYSTEM_METRIC("CoreC0StateResidency", (counters.getSystem<double, ::getCoreC0Residency>()*100.) )
        OUTPUT_SYSTEM_METRIC("CoreC3StateResidency", (counters.getSystem<double, ::getCoreC3Residency>()*100.) )
        OUTPUT_SYSTEM_METRIC("CoreC6StateResidency", (counters.getSystem<double, ::getCoreC6Residency>()*100.) )
        OUTPUT_SYSTEM_METRIC("CoreC7StateResidency", (counters.getSystem<double, ::getCoreC7Residency>()*100.) )
        OUTPUT_SYSTEM_METRIC("PackageC2StateResidency", (counters.getSystem<double, ::getPackageC2Residency>()*100.) )
        OUTPUT_SYSTEM_METRIC("PackageC3StateResidency", (counters.getSystem<double, ::getPackageC3Residency>()*100.) )        
        OUTPUT_SYSTEM_METRIC("PackageC6StateResidency", (counters.getSystem<double, ::getPackageC6Residency>()*100.) )        
        OUTPUT_SYSTEM_METRIC("PackageC7StateResidency", (counters.getSystem<double, ::getPackageC7Residency>()*100.) )        
        OUTPUT_SYSTEM_METRIC("Frequency", (double(counters.getSystem<double, ::getAverageFrequency>()) / 1000000) )
	OUTPUT_SYSTEM_METRIC("IPC", (double(counters.getSystem<double, ::getIPC>())) )
	OUTPUT_SYSTEM_METRIC("L2CacheHitRatio", (double(counters.getSystem<double, ::getL2CacheHitRatio>())) )
	OUTPUT_SYSTEM_METRIC("L3CacheHitRatio", (double(counters.getSystem<double, ::getL3CacheHitRatio>())) )
	OUTPUT_SYSTEM_METRIC("L2CacheMisses", (double(counters.getSystem<uint64, ::getL2CacheMisses>())) )
	OUTPUT_SYSTEM_METRIC("L3CacheMisses", (double(counters.getSystem<uint64, ::getL3CacheMisses>())) )
	OUTPUT_SYSTEM_METRIC("QPI_Traffic", (double(counters.getSystem<uint64, ::getAllIncomingQPILinkBytes>()) / 1024 / 1024 / 1024) )

        // exit
        if (s == "quit" || s == "exit") {
            break;
        }


        cout << "ksysguardd> ";
    }

    return 0;
}
