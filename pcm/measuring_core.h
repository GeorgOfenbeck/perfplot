#ifndef MEASURING_CORE_HEADER
#define MEASURING_CORE_HEADER



int perfmon_init(int type, bool flushData , bool flushICache , bool flushTLB );
void perfmon_start();
void perfmon_stop();
void perfmon_end();

#endif

#ifdef NOTHING
	 cout << "\n";
        cout << " EXEC  : instructions per nominal CPU cycle" << "\n";
        cout << " IPC   : instructions per CPU cycle" << "\n";
        cout << " FREQ  : relation to nominal CPU frequency='unhalted clock ticks'/'invariant timer ticks' (includes Intel Turbo Boost)" << "\n";
        if (cpu_model != PCM::ATOM) cout << " AFREQ : relation to nominal CPU frequency while in active state (not in power-saving C state)='unhalted clock ticks'/'invariant timer ticks while in C0-state'  (includes Intel Turbo Boost)" << "\n";
        if (cpu_model != PCM::ATOM) cout << " L3MISS: L3 cache misses " << "\n";
        if (cpu_model == PCM::ATOM)
            cout << " L2MISS: L2 cache misses " << "\n";
        else
            cout << " L2MISS: L2 cache misses (including other core's L2 cache *hits*) " << "\n";
        if (cpu_model != PCM::ATOM) cout << " L3HIT : L3 cache hit ratio (0.00-1.00)" << "\n";
        cout << " L2HIT : L2 cache hit ratio (0.00-1.00)" << "\n";
        if (cpu_model != PCM::ATOM) cout << " L3CLK : ratio of CPU cycles lost due to L3 cache misses (0.00-1.00), in some cases could be >1.0 due to a higher memory latency" << "\n";
        if (cpu_model != PCM::ATOM) cout << " L2CLK : ratio of CPU cycles lost due to missing L2 cache but still hitting L3 cache (0.00-1.00)" << "\n";
        if (cpu_model != PCM::ATOM) cout << " READ  : bytes read from memory controller (in GBytes)" << "\n";
        if (cpu_model != PCM::ATOM) cout << " WRITE : bytes written to memory controller (in GBytes)" << "\n";
        cout << " TEMP  : Temperature reading in 1 degree Celsius relative to the TjMax temperature (thermal headroom): 0 corresponds to the max temperature" << "\n";
        cout << "\n";
        cout << "\n";
        cout.precision(2);
        cout << std::fixed;
        if (cpu_model == PCM::ATOM)
            cout << " Core (SKT) | EXEC | IPC  | FREQ | L2MISS | L2HIT | TEMP" << "\n" << "\n";
        else
            cout << " Core (SKT) | EXEC | IPC  | FREQ  | AFREQ | L3MISS | L2MISS | L3HIT | L2HIT | L3CLK | L2CLK  | READ  | WRITE | TEMP" << "\n" << "\n";


        if (show_core_output)
        {
            for (uint32 i = 0; i < m->getNumCores(); ++i)
            {
                if (cpu_model != PCM::ATOM)
                    cout << " " << setw(3) << i << "   " << setw(2) << m->getSocketId(i) <<
                    "     " << getExecUsage(cstates1[i], cstates2[i]) <<
                    "   " << getIPC(cstates1[i], cstates2[i]) <<
                    "   " << getRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "    " << getActiveRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "    " << unit_format(getL3CacheMisses(cstates1[i], cstates2[i])) <<
                    "   " << unit_format(getL2CacheMisses(cstates1[i], cstates2[i])) <<
                    "    " << getL3CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "    " << getL2CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "    " << getCyclesLostDueL3CacheMisses(cstates1[i], cstates2[i]) <<
                    "    " << getCyclesLostDueL2CacheMisses(cstates1[i], cstates2[i]) <<
                    "     N/A     N/A" <<
                    "     " << temp_format(cstates2[i].getThermalHeadroom()) <<
                    "\n";
                else
                    cout << " " << setw(3) << i << "   " << setw(2) << m->getSocketId(i) <<
                    "     " << getExecUsage(cstates1[i], cstates2[i]) <<
                    "   " << getIPC(cstates1[i], cstates2[i]) <<
                    "   " << getRelativeFrequency(cstates1[i], cstates2[i]) <<
                    "   " << unit_format(getL2CacheMisses(cstates1[i], cstates2[i])) <<
                    "    " << getL2CacheHitRatio(cstates1[i], cstates2[i]) <<
                    "     " << temp_format(cstates2[i].getThermalHeadroom()) <<
                    "\n";
            }
        }
        if (show_socket_output)
        {
            if(!(m->getNumSockets() == 1 && cpu_model==PCM::ATOM))
            {
                cout << "-------------------------------------------------------------------------------------------------------------------" << "\n";
                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i <<
                    "     " << getExecUsage(sktstate1[i], sktstate2[i]) <<
                    "   " << getIPC(sktstate1[i], sktstate2[i]) <<
                    "   " << getRelativeFrequency(sktstate1[i], sktstate2[i]) <<
                    "    " << getActiveRelativeFrequency(sktstate1[i], sktstate2[i]) <<
                    "    " << unit_format(getL3CacheMisses(sktstate1[i], sktstate2[i])) <<
                    "   " << unit_format(getL2CacheMisses(sktstate1[i], sktstate2[i])) <<
                    "    " << getL3CacheHitRatio(sktstate1[i], sktstate2[i]) <<
                    "    " << getL2CacheHitRatio(sktstate1[i], sktstate2[i]) <<
                    "    " << getCyclesLostDueL3CacheMisses(sktstate1[i], sktstate2[i]) <<
                    "    " << getCyclesLostDueL2CacheMisses(sktstate1[i], sktstate2[i]);
                    if (!(m->memoryTrafficMetricsAvailable()))
                       cout << "     N/A     N/A";
                   else
                       cout << "    " << getBytesReadFromMC(sktstate1[i], sktstate2[i]) / double(1024ULL * 1024ULL * 1024ULL) <<
                               "    " << getBytesWrittenToMC(sktstate1[i], sktstate2[i]) / double(1024ULL * 1024ULL * 1024ULL);
                    cout << "     " << temp_format(sktstate2[i].getThermalHeadroom()) << "\n";
                }
            }
        }
        cout << "-------------------------------------------------------------------------------------------------------------------" << "\n";

        if (show_system_output)
        {
            if (cpu_model != PCM::ATOM)
            {
                cout << " TOTAL  *     " << getExecUsage(sstate1, sstate2) <<
                "   " << getIPC(sstate1, sstate2) <<
                "   " << getRelativeFrequency(sstate1, sstate2) <<
                "    " << getActiveRelativeFrequency(sstate1, sstate2) <<
                "    " << unit_format(getL3CacheMisses(sstate1, sstate2)) <<
                "   " << unit_format(getL2CacheMisses(sstate1, sstate2)) <<
                "    " << getL3CacheHitRatio(sstate1, sstate2) <<
                "    " << getL2CacheHitRatio(sstate1, sstate2) <<
                "    " << getCyclesLostDueL3CacheMisses(sstate1, sstate2) <<
                "    " << getCyclesLostDueL2CacheMisses(sstate1, sstate2);
                if (!(m->memoryTrafficMetricsAvailable()))
                    cout << "     N/A     N/A";
                else
                    cout << "    " << getBytesReadFromMC(sstate1, sstate2) / double(1024ULL * 1024ULL * 1024ULL) <<
                    "    " << getBytesWrittenToMC(sstate1, sstate2) / double(1024ULL * 1024ULL * 1024ULL);
                cout << "     N/A\n";
            }
            else
                cout << " TOTAL  *     " << getExecUsage(sstate1, sstate2) <<
                "   " << getIPC(sstate1, sstate2) <<
                "   " << getRelativeFrequency(sstate1, sstate2) <<
                "   " << unit_format(getL2CacheMisses(sstate1, sstate2)) <<
                "    " << getL2CacheHitRatio(sstate1, sstate2) <<
                "     N/A\n";
        }

        if (show_system_output)
        {
            cout << "\n" << " Instructions retired: " << unit_format(getInstructionsRetired(sstate1, sstate2)) << " ; Active cycles: " << unit_format(getCycles(sstate1, sstate2)) << " ; Time (TSC): " << unit_format(getInvariantTSC(cstates1[0], cstates2[0])) << "ticks ; C0 (active,non-halted) core residency: "<< (getCoreC0Residency(sstate1, sstate2)*100.)<<" %\n";
	    cout << "\n" << " C3 core residency: "<< (getCoreC3Residency(sstate1, sstate2)*100.)<<" %; C6 core residency: "<< (getCoreC6Residency(sstate1, sstate2)*100.)<<" %; C7 core residency: "<< (getCoreC7Residency(sstate1, sstate2)*100.)<<" %\n";
            cout << " C2 package residency: "<< (getPackageC2Residency(sstate1, sstate2)*100.)<<" %; C3 package residency: "<< (getPackageC3Residency(sstate1, sstate2)*100.)<<" %; C6 package residency: "<< (getPackageC6Residency(sstate1, sstate2)*100.)<<" %; C7 package residency: "<< (getPackageC7Residency(sstate1, sstate2)*100.)<<" %\n";
            cout << "\n" << " PHYSICAL CORE IPC                 : " << getCoreIPC(sstate1, sstate2) << " => corresponds to " << 100. * (getCoreIPC(sstate1, sstate2) / double(m->getMaxIPC())) << " % utilization for cores in active state";
            cout << "\n" << " Instructions per nominal CPU cycle: " << getTotalExecUsage(sstate1, sstate2) << " => corresponds to " << 100. * (getTotalExecUsage(sstate1, sstate2) / double(m->getMaxIPC())) << " % core utilization over time interval" << "\n";
        }

        if (show_socket_output)
        {
            if (m->getNumSockets() > 1) // QPI info only for multi socket systems
            {
                cout << "\n" << "Intel(r) QPI data traffic estimation in bytes (data traffic coming to CPU/socket through QPI links):" << "\n" << "\n";


                const uint32 qpiLinks = (uint32)m->getQPILinksPerSocket();

                cout << "              ";
                for (uint32 i = 0; i < qpiLinks; ++i)
                    cout << " QPI" << i << "    ";

                if (m->qpiUtilizationMetricsAvailable())
                {
                    cout << "| ";
                    for (uint32 i = 0; i < qpiLinks; ++i)
                        cout << " QPI" << i << "  ";
                }

                cout << "\n" << "----------------------------------------------------------------------------------------------" << "\n";


                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i << "     ";
                    for (uint32 l = 0; l < qpiLinks; ++l)
                        cout << unit_format(getIncomingQPILinkBytes(i, l, sstate1, sstate2)) << "   ";

                    if (m->qpiUtilizationMetricsAvailable())
                    {
                        cout << "|  ";
                        for (uint32 l = 0; l < qpiLinks; ++l)
                            cout << setw(3) << int(100. * getIncomingQPILinkUtilization(i, l, sstate1, sstate2)) << "%   ";
                    }

                    cout << "\n";
                }
            }
        }

        if (show_system_output)
        {
            cout << "----------------------------------------------------------------------------------------------" << "\n";

            if (m->getNumSockets() > 1) // QPI info only for multi socket systems
                cout << "Total QPI incoming data traffic: " << unit_format(getAllIncomingQPILinkBytes(sstate1, sstate2)) << "     QPI data traffic/Memory controller traffic: " << getQPItoMCTrafficRatio(sstate1, sstate2) << "\n";
        }

        if (show_socket_output)
        {
            if (m->getNumSockets() > 1 && (m->outgoingQPITrafficMetricsAvailable())) // QPI info only for multi socket systems
            {
                cout << "\n" << "Intel(r) QPI traffic estimation in bytes (data and non-data traffic outgoing from CPU/socket through QPI links):" << "\n" << "\n";


                const uint32 qpiLinks = (uint32)m->getQPILinksPerSocket();

                cout << "              ";
                for (uint32 i = 0; i < qpiLinks; ++i)
                    cout << " QPI" << i << "    ";


                cout << "| ";
                for (uint32 i = 0; i < qpiLinks; ++i)
                    cout << " QPI" << i << "  ";


                cout << "\n" << "----------------------------------------------------------------------------------------------" << "\n";


                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i << "     ";
                    for (uint32 l = 0; l < qpiLinks; ++l)
                        cout << unit_format(getOutgoingQPILinkBytes(i, l, sstate1, sstate2)) << "   ";

                    cout << "|  ";
                    for (uint32 l = 0; l < qpiLinks; ++l)
                        cout << setw(3) << int(100. * getOutgoingQPILinkUtilization(i, l, sstate1, sstate2)) << "%   ";

                    cout << "\n";
                }

                cout << "----------------------------------------------------------------------------------------------" << "\n";
                cout << "Total QPI outgoing data and non-data traffic: " << unit_format(getAllOutgoingQPILinkBytes(sstate1, sstate2)) << "\n";
            }
        }
	if (show_socket_output)
        {
            if(m->packageEnergyMetricsAvailable())
            {
                cout << "\n";
		cout << "----------------------------------------------------------------------------------------------" << "\n";
                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i << " package consumed "<< getConsumedJoules(sktstate1[i],sktstate2[i])<<" Joules\n";
                }
                cout << "----------------------------------------------------------------------------------------------" << "\n";
                cout << " TOTAL:                    "<<getConsumedJoules(sstate1, sstate2) <<" Joules\n";
            }
            if(m->dramEnergyMetricsAvailable())
            {
                cout << "\n";
		cout << "----------------------------------------------------------------------------------------------" << "\n";
                for (uint32 i = 0; i < m->getNumSockets(); ++i)
                {
                    cout << " SKT   " << setw(2) << i << " DIMMs consumed "<< getDRAMConsumedJoules(sktstate1[i],sktstate2[i])<<" Joules\n";
                }
                cout << "----------------------------------------------------------------------------------------------" << "\n";
                cout << " TOTAL:                  "<<getDRAMConsumedJoules(sstate1, sstate2) <<" Joules\n";
            }
        }

#endif 