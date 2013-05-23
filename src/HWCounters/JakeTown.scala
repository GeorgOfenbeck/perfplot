package HWCounters

/**
 * Georg Ofenbeck
 First created:
 * Date: 11/02/13
 * Time: 10:53 
 */

case class Counter(EventNum: String, Umask: String, name: String, Description: String, Comment: String)
{
  def getEventNr = Integer.parseInt(EventNum.substring(0,EventNum.size-1),16).toLong
  def getUmask = Integer.parseInt(Umask.substring(0,Umask.size-1),16).toLong

}


object JakeTown {

   //= "(" & CHAR(34) & A2 & CHAR(34) & "," & CHAR(34) & B2 & CHAR(34) & "," & CHAR(34) & C2 & CHAR(34) & "," & CHAR(34) & D2 & CHAR(34) & "," & CHAR(34) & E2 & CHAR(34) & ")"
  //==  "Counter" &"(" & CHAR(34) & A210 & CHAR(34) & "," & CHAR(34) & B210 & CHAR(34) & "," & CHAR(34) & C210 & CHAR(34) & "," & CHAR(34) & D210 & CHAR(34) & "," & CHAR(34) & E210 & CHAR(34) & "),"
  //=  "Counter" &"(" & CHAR(34) & "B7H" & CHAR(34) & "," & CHAR(34) & "01H" & CHAR(34) & "," & CHAR(34) & C210 & CHAR(34) & "," & CHAR(34) & D210 & CHAR(34) & "," & CHAR(34) & E210 & CHAR(34) & "),"
  val counters = Array (
  Counter("03H","02H","LD_BLOCKS.STORE_FORWARD","loads blocked by overlapping with store buffer that cannot be forwarded .",""),
  Counter("03H","08H","LD_BLOCKS.NO_SR","# of Split loads blocked due to resource not available.",""),
  Counter("03H","10H","LD_BLOCKS.ALL_BLOCK","Number of cases where any load is blocked but has no DCU miss.",""),
  Counter("05H","01H","MISALIGN_MEM_REF.LOADS","Speculative cache-line split load uops dispatched to L1D.",""),
  Counter("05H","02H","MISALIGN_MEM_REF.STORES","Speculative cache-line split Store-address uops dispatched to L1D.",""),
  Counter("07H","01H","LD_BLOCKS_PARTIAL.ADDRESS_ALIAS","False dependencies in MOB due to partial compare on address.",""),
  Counter("07H","08H","LD_BLOCKS_PARTIAL.ALL_STA_BLOCK","The number of times that load operations are temporarily blocked because of older stores, with addresses that are not yet known. A load operation may incur more than one block of this type.",""),
  Counter("08H","01H","DTLB_LOAD_MISSES.MISS_CAUSES_A_WALK","Misses in all TLB levels that cause a page walk of any page size.",""),
  Counter("08H","02H","DTLB_LOAD_MISSES.WALK_COMPLETED","Misses in all TLB levels that caused page walk completed of any size.",""),
  Counter("08H","04H","DTLB_LOAD_MISSES.WALK_DURATION","Cycle PMH is busy with a walk.",""),
  Counter("08H","10H","DTLB_LOAD_MISSES.STLB_HIT","Number of cache load STLB hits. No page walk.",""),
  Counter("0DH","03H","INT_MISC.RECOVERY_CYCLES","Cycles waiting to recover after Machine Clears or JEClear. Set Cmask= 1.","Set Edge to count occurrences"),
  Counter("0DH","40H","INT_MISC.RAT_STALL_CYCLES","Cycles RAT external stall is sent to IDQ for this thread.",""),
  Counter("0EH","01H","UOPS_ISSUED.ANY","Increments each cycle the # of Uops issued by the RATtoRS. Set Cmask = 1, Inv = 1, Any= 1to count stalled cycles of this core.","Set Cmask = 1, Inv = 1to count stalled cycles"),
  Counter("10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed.",""),
  Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
  Counter("10H","20H","FP_COMP_OPS_EXE.SSE_FP_SCALAR_SINGLE","Counts number of SSE* single precision FP scalar uops executed.",""),
  Counter("10H","40H","FP_COMP_OPS_EXE.SSE_PACKED_SINGLE","Counts number of SSE* single precision FP packed uops executed.",""),
  Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
  Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.",""),
  Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
  Counter("14H","01H","ARITH.FPU_DIV_ACTIVE","Cycles that the divider is active, includes INT and FP. Set 'edge =1, cmask=1' to count the number of divides.",""),
  Counter("17H","01H","INSTS_WRITTEN_TO_IQ.INSTS","Counts the number of instructions written into the IQ every cycle.",""),
  Counter("24H","01H","L2_RQSTS.DEMAND_DATA_RD_HIT","Demand Data Read requests that hit L2 cache.",""),
  Counter("24H","03H","L2_RQSTS.ALL_DEMAND_DATA_RD","Counts any demand and L1 HW prefetch data load requests to L2.",""),
  Counter("24H","04H","L2_RQSTS.RFO_HITS","Counts the number of store RFO requests that hit the L2 cache.",""),
  Counter("24H","08H","L2_RQSTS.RFO_MISS","Counts the number of store RFO requests that miss the L2 cache.",""),
  Counter("24H","0CH","L2_RQSTS.ALL_RFO","Counts all L2 store RFO requests.",""),
  Counter("24H","10H","L2_RQSTS.CODE_RD_HIT","Number of instruction fetches that hit the L2 cache.",""),
  Counter("24H","20H","L2_RQSTS.CODE_RD_MISS","Number of instruction fetches that missed the L2 cache.",""),
  Counter("24H","30H","L2_RQSTS.ALL_CODE_RD","Counts all L2 code requests.",""),
  Counter("24H","40H","L2_RQSTS.PF_HIT","Requests from L2 Hardware prefetcher that hit L2.",""),
  Counter("24H","80H","L2_RQSTS.PF_MISS","Requests from L2 Hardware prefetcher that missed L2.",""),
  Counter("24H","C0H","L2_RQSTS.ALL_PF","Any requests from L2 Hardware prefetchers.",""),
  Counter("27H","01H","L2_STORE_LOCK_RQSTS.MISS","RFOs that miss cache lines.",""),
  Counter("27H","04H","L2_STORE_LOCK_RQSTS.HIT_ E","RFOs that hit cache lines in E state.",""),
  Counter("27H","08H","L2_STORE_LOCK_RQSTS.HIT_ M","RFOs that hit cache lines in M state.",""),
  Counter("27H","0FH","L2_STORE_LOCK_RQSTS.ALL","RFOs that access cache lines in any state.",""),
  Counter("28H","01H","L2_L1D_WB_RQSTS.MISS","Not rejected writebacks from L1D to L2 cache lines that missed L2.",""),
  Counter("28H","02H","L2_L1D_WB_RQSTS.HIT_S","Not rejected writebacks from L1D to L2 cache lines in S state.",""),
  Counter("28H","04H","L2_L1D_WB_RQSTS.HIT_E","Not rejected writebacks from L1D to L2 cache lines in E state.",""),
  Counter("28H","08H","L2_L1D_WB_RQSTS.HIT_M","Not rejected writebacks from L1D to L2 cache lines in M state.",""),
  Counter("28H","0FH","L2_L1D_WB_RQSTS.ALL","Not rejected writebacks from L1D to L2 cache.",""),
  Counter("2EH","4FH","LONGEST_LAT_CACHE.REFERENCE","This event counts requests originating from the core that reference a cache line in the last level cache.","see Table 19-1"),
  Counter("2EH","41H","LONGEST_LAT_CACHE.MISS","This event counts each cache miss condition for references to the last level cache.","see Table 19-1"),
  Counter("3CH","00H","CPU_CLK_UNHALTED.THREAD_P","Counts the number of thread cycles while the thread is not in a halt state. The thread enters the halt state when it is running the HLT instruction. The core frequency may change from time to time due to power or thermal throttling.","see Table 19-1"),
  Counter("3CH","01H","CPU_CLK_THREAD_UNHALTED.REF_XCLK","Increments at the frequency of XCLK (100 MHz) when not halted.","see Table 19-1"),
  Counter("48H","01H","L1D_PEND_MISS.PENDING","Increments the number of outstanding L1D misses every cycle. Set Cmaks = 1 and Edge =1 to count occurrences.","PMC2 only; Set Cmask = 1 to count cycles."),
  Counter("49H","01H","DTLB_STORE_MISSES.MISS_CAUSES_A_WALK","Miss in all TLB levels causes an page walk of any page size (4K/2M/4M/1G).",""),
  Counter("49H","02H","DTLB_STORE_MISSES.WALK_COMPLETED","Miss in all TLB levels causes a page walk that completes of any page size (4K/2M/4M/1G).",""),
  Counter("49H","04H","DTLB_STORE_MISSES.WALK_DURATION","Cycles PMH is busy with this walk.",""),
  Counter("49H","10H","DTLB_STORE_MISSES.STLB_HIT","Store operations that miss the first TLB level but hit the second and do not cause page walks.",""),
  Counter("4CH","01H","LOAD_HIT_PRE.SW_PF","Not SW-prefetch load dispatches that hit fill buffer allocated for S/W prefetch.",""),
  Counter("4CH","02H","LOAD_HIT_PRE.HW_PF","Not SW-prefetch load  dispatches that hit fill buffer allocated for H/W prefetch.",""),
  Counter("4EH","02H","HW_PRE_REQ.DL1_MISS","Hardware Prefetch requests that miss the L1D cache. A request is being counted each time it access the cache & miss it, including if a block is applicable or if hit the Fill Buffer for example.","This accounts for both L1 streamer and IP-based (IPP) HW prefetchers."),
  Counter("51H","01H","L1D.REPLACEMENT","Counts the number of lines brought into the L1 data cache.",""),
  Counter("51H","02H","L1D.ALLOCATED_IN_M","Counts the number of allocations of modified L1D cache lines.",""),
  Counter("51H","04H","L1D.EVICTION","Counts the number of modified lines evicted from the L1 data cache due to replacement.",""),
  Counter("51H","08H","L1D.ALL_M_REPLACEMENT","Cache lines in M state evicted out of L1D due to Snoop HitM or dirty line replacement.",""),
  Counter("59H","20H","PARTIAL_RAT_STALLS.FLAGS_ MERGE_UOP","Increments the number of flags-merge uops in flight each cycle.Set Cmask = 1 to count cycles.",""),
  Counter("59H","40H","PARTIAL_RAT_STALLS.SLOW_ LEA_WINDOW","Cycles with at least one slow LEA uop allocated.",""),
  Counter("59H","80H","PARTIAL_RAT_STALLS.MUL_SINGLE_UOP","Number of Multiply packed/scalar single precision uops allocated.",""),
  Counter("5BH","0CH","RESOURCE_STALLS2.ALL_FL_ EMPTY","Cycles stalled due to free list empty.","PMC0-3 only regardless HTT"),
  Counter("5BH","0FH","RESOURCE_STALLS2.ALL_PRF_CONTROL","Cycles stalled due to control structures full for physical registers.",""),
  Counter("5BH","40H","RESOURCE_STALLS2.BOB_FULL","Cycles Allocator is stalled due Branch Order Buffer.",""),
  Counter("5BH","4FH","RESOURCE_STALLS2.OOO_RSRC","Cycles stalled due to out of order resources full.",""),
  Counter("5CH","01H","CPL_CYCLES.RING0","Unhalted core cycles when the thread is in ring 0.","Use Edge to count transition"),
  Counter("5CH","02H","CPL_CYCLES.RING123","Unhalted core cycles when the thread is not in ring 0.",""),
  Counter("5EH","01H","RS_EVENTS.EMPTY_CYCLES","Cycles the RS is empty for the thread.",""),
  Counter("60H","01H","OFFCORE_REQUESTS_OUTSTANDING.DEMAND_DATA_RD","Offcore outstanding Demand Data Read transactions in SQ to uncore. Set Cmask=1 to count cycles.",""),
  Counter("60H","04H","OFFCORE_REQUESTS_OUTSTANDING.DEMAND_RFO","Offcore outstanding RFO store transactions in SQ to uncore. Set Cmask=1 to count cycles.",""),
  Counter("60H","08H","OFFCORE_REQUESTS_OUTSTANDING.ALL_DATA_RD","Offcore outstanding cacheable data read transactions in SQ to uncore. Set Cmask=1 to count cycles.",""),
  Counter("63H","01H","LOCK_CYCLES.SPLIT_LOCK_UC_LOCK_DURATION","Cycles in which the L1D and L2 are locked, due to a UC lock or split lock.",""),
  Counter("63H","02H","LOCK_CYCLES.CACHE_LOCK_DURATION","Cycles in which the L1D is locked.",""),
  Counter("79H","02H","IDQ.EMPTY","Counts cycles the IDQ is empty.",""),
  Counter("79H","04H","IDQ.MITE_UOPS","Increment each cycle # of uops delivered to IDQ from MITE path. Set Cmask = 1 to count cycles.","Can combine Umask 04H and 20H"),
  Counter("79H","08H","IDQ.DSB_UOPS","Increment each cycle. # of uops delivered to IDQ from DSB path. Set Cmask = 1 to count cycles.","Can combine Umask 08H and 10H"),
  Counter("79H","10H","IDQ.MS_DSB_UOPS","Increment each cycle # of uops delivered to IDQ when MS busy by DSB. Set Cmask = 1 to count cycles MS is busy. Set Cmask=1 and Edge =1 to count MS activations.","Can combine Umask 08H and 10H"),
  Counter("79H","20H","IDQ.MS_MITE_UOPS","Increment each cycle # of uops delivered to IDQ when MS is busy by MITE. Set Cmask = 1 to count cycles.","Can combine Umask 04H and 20H"),
  Counter("79H","30H","IDQ.MS_UOPS","Increment each cycle # of uops delivered to IDQ from MS by either DSB or MITE. Set Cmask = 1 to count cycles.","Can combine Umask 04H, 08H and 30H"),
  Counter("80H","02H","ICACHE.MISSES","Number of Instruction Cache, Streaming Buffer and Victim Cache Misses. Includes UC accesses.",""),
  Counter("85H","01H","ITLB_MISSES.MISS_CAUSES_A_WALK","Misses in all ITLB levels that cause page walks.",""),
  Counter("85H","02H","ITLB_MISSES.WALK_COMPLETED","Misses in all ITLB levels that cause completed page walks.",""),
  Counter("85H","04H","ITLB_MISSES.WALK_DURATION","Cycle PMH is busy with a walk.",""),
  Counter("85H","10H","ITLB_MISSES.STLB_HIT","Number of cache load STLB hits. No page walk.",""),
  Counter("87H","01H","ILD_STALL.LCP","Stalls caused by changing prefix length of the instruction.",""),
  Counter("87H","04H","ILD_STALL.IQ_FULL","Stall cycles due to IQ is full.",""),
  Counter("88H","01H","BR_INST_EXEC.COND","Qualify conditional near branch instructions executed, but not necessarily retired.","Must combine with umask 40H, 80H"),
  Counter("88H","02H","BR_INST_EXEC.DIRECT_JMP","Qualify all unconditional near branch instructions excluding calls and indirect branches.","Must combine with umask 80H"),
  Counter("88H","04H","BR_INST_EXEC.INDIRECT_JMP_ NON_CALL_RET","Qualify executed indirect near branch instructions that are not calls nor returns.","Must combine with umask 80H"),
  Counter("88H","08H","BR_INST_EXEC.RETURN_NEAR","Qualify indirect near branches that have a return mnemonic.","Must combine with umask 80H"),
  Counter("88H","10H","BR_INST_EXEC.DIRECT_NEAR_ CALL","Qualify unconditional near call branch instructions, excluding non call branch, executed.","Must combine with umask 80H"),
  Counter("88H","20H","BR_INST_EXEC.INDIRECT_NEAR_CALL","Qualify indirect near calls, including both register and memory indirect, executed.","Must combine with umask 80H"),
  Counter("88H","40H","BR_INST_EXEC.NONTAKEN","Qualify non-taken near branches executed.","Applicable to umask 01H only"),
  Counter("88H","80H","BR_INST_EXEC.TAKEN","Qualify taken near branches executed. Must combine with 01H,02H, 04H, 08H, 10H, 20H.",""),
  Counter("88H","FFH","BR_INST_EXEC.ALL_BRANCHES","Counts all near executed branches (not necessarily retired).",""),
  Counter("89H","01H","BR_MISP_EXEC.COND","Qualify conditional near branch instructions mispredicted.","Must combine with umask 40H, 80H"),
  Counter("89H","04H","BR_MISP_EXEC.INDIRECT_JMP_NON_CALL_RET","Qualify mispredicted indirect near branch instructions that are not calls nor returns.","Must combine with umask 80H"),
  Counter("89H","08H","BR_MISP_EXEC.RETURN_NEAR","Qualify mispredicted indirect near branches that have a return mnemonic.","Must combine with umask 80H"),
  Counter("89H","10H","BR_MISP_EXEC.DIRECT_NEAR_CALL","Qualify mispredicted unconditional near call branch instructions, excluding non call branch, executed.","Must combine with umask 80H"),
  Counter("89H","20H","BR_MISP_EXEC.INDIRECT_NEAR_CALL","Qualify mispredicted indirect near calls, including both register and memory indirect, executed.","Must combine with umask 80H"),
  Counter("89H","40H","BR_MISP_EXEC.NONTAKEN","Qualify mispredicted non-taken near branches executed,.","Applicable to umask 01H only"),
  Counter("89H","80H","BR_MISP_EXEC.TAKEN","Qualify mispredicted taken near branches executed. Must combine with 01H,02H, 04H, 08H, 10H, 20H",""),
  Counter("89H","FFH","BR_MISP_EXEC.ALL_BRANCHES","Counts all near executed branches (not necessarily retired).",""),
  Counter("9CH","01H","IDQ_UOPS_NOT_DELIVERED.CORE","Count number of non-delivered uops to RAT per thread.","Use Cmask to qualify uop b/w"),
  Counter("A1H","01H","UOPS_DISPATCHED_PORT.PORT_0","Cycles which a Uop is dispatched on port 0.",""),
  Counter("A1H","02H","UOPS_DISPATCHED_PORT.PORT_1","Cycles which a Uop is dispatched on port 1.",""),
  Counter("A1H","04H","UOPS_DISPATCHED_PORT.PORT_2_LD","Cycles which a load uop is dispatched on port 2.",""),
  Counter("A1H","08H","UOPS_DISPATCHED_PORT.PORT_2_STA","Cycles which a store address uop is dispatched on port 2.",""),
  Counter("A1H","0CH","UOPS_DISPATCHED_PORT.PORT_2","Cycles which a Uop is dispatched on port 2.",""),
  Counter("A1H","10H","UOPS_DISPATCHED_PORT.PORT_3_LD","Cycles which a load uop is dispatched on port 3.",""),
  Counter("A1H","20H","UOPS_DISPATCHED_PORT.PORT_3_STA","Cycles which a store address uop is dispatched on port 3.",""),
  Counter("A1H","30H","UOPS_DISPATCHED_PORT.PORT_3","Cycles which a Uop is dispatched on port 3.",""),
  Counter("A1H","40H","UOPS_DISPATCHED_PORT.PORT_4","Cycles which a Uop is dispatched on port 4.",""),
  Counter("A1H","80H","UOPS_DISPATCHED_PORT.PORT_5","Cycles which a Uop is dispatched on port 5.",""),
  Counter("A2H","01H","RESOURCE_STALLS.ANY","Cycles Allocation is stalled due to Resource Related reason.",""),
  Counter("A2H","02H","RESOURCE_STALLS.LB","Counts the cycles of stall due to lack of load buffers.",""),
  Counter("A2H","04H","RESOURCE_STALLS.RS","Cycles stalled due to no eligible RS entry available.",""),
  Counter("A2H","08H","RESOURCE_STALLS.SB","Cycles stalled due to no store buffers available. (not including draining form sync).",""),
  Counter("A2H","10H","RESOURCE_STALLS.ROB","Cycles stalled due to re-order buffer full.",""),
  Counter("A2H","20H","RESOURCE_STALLS.FCSW","Cycles stalled due to writing the FPU control word.",""),
  Counter("A2H","40H","RESOURCE_STALLS.MXCSR","Cycles stalled due to the MXCSR register rename occurring to close to a previous MXCSR rename.",""),
  Counter("A2H","80H","RESOURCE_STALLS.OTHER","Cycles stalled while execution was stalled due to other resource issues.",""),
  Counter("A3H","02H","CYCLE_ACTIVITY.CYCLES_L1D_ PENDING","Cycles with pending L1 cache miss loads.Set AnyThread to count per core.","PMC2 only"),
  Counter("A3H","01H","CYCLE_ACTIVITY.CYCLES_L2_PENDING","Cycles with pending L2 miss loads. Set AnyThread to count per core.",""),
  Counter("A3H","04H","CYCLE_ACTIVITY.CYCLES_NO_ DISPATCH","Cycles of dispatch stalls. Set AnyThread to count per core.","PMC0-3 only"),
  Counter("ABH","01H","DSB2MITE_SWITCHES.COUNT","Number of DSB to MITE switches.",""),
  Counter("ABH","02H","DSB2MITE_SWITCHES.PENALTY_CYCLES","Cycles DSB to MITE switches caused delay.",""),
  Counter("ACH","02H","DSB_FILL.OTHER_CANCEL","Cases of cancelling valid DSB fill not because of exceeding way limit.",""),
  Counter("ACH","08H","DSB_FILL.EXCEED_DSB_LINES","DSBFill encountered > 3 DSB lines.",""),
  Counter("ACH","0AH","DSB_FILL.ALL_CANCEL","Cases of cancelling valid Decode Stream Buffer (DSB) fill not because of exceeding way limit.",""),
  Counter("AEH","01H","ITLB.ITLB_FLUSH","Counts the number of ITLB flushes, includes 4k/2M/4M pages.",""),
  Counter("B0H","01H","OFFCORE_REQUESTS.DEMAND_DATA_RD","Demand data read requests sent to uncore.",""),
  Counter("B0H","04H","OFFCORE_REQUESTS.DEMAND_RFO","Demand RFO read requests sent to uncore, including regular RFOs, locks, ItoM.",""),
  Counter("B0H","08H","OFFCORE_REQUESTS.ALL_DATA_RD","Data read requests sent to uncore (demand and prefetch).",""),
  Counter("B1H","01H","UOPS_DISPATCHED.THREAD","Counts total number of uops to be dispatched per- thread each cycle. Set Cmask = 1, INV =1 to count stall cycles.","PMC0-3 only regardless HTT"),
  Counter("B1H","02H","UOPS_DISPATCHED.CORE","Counts total number of uops to be dispatched per- core each cycle.","Do not need to set ANY"),
  Counter("B2H","01H","OFFCORE_REQUESTS_BUFFER.SQ_FULL","Offcore requests buffer cannot take more entries for this thread core.",""),
  Counter("B6H","01H","AGU_BYPASS_CANCEL.COUNT","Counts executed load operations with all the following traits: 1. addressing of the format [base + offset], 2. the offset is between 1 and 2047, 3. the address specified in the base register is in one page and the address [base+offset] is in another page.",""),
  Counter("BDH","01H","TLB_FLUSH.DTLB_THREAD","DTLB flush attempts of the thread-specific entries.",""),
  Counter("BDH","20H","TLB_FLUSH.STLB_ANY","Count number of STLB flush attempts.",""),
  Counter("BFH","05H","L1D_BLOCKS.BANK_CONFLICT_CYCLES","Cycles when dispatched loads are cancelled due to L1D bank conflicts with other load ports.","cmask=1"),
  Counter("C0H","00H","INST_RETIRED.ANY_P","Number of instructions at retirement.","See Table 19-1"),
  Counter("C0H","01H","INST_RETIRED.ALL","Precise instruction retired event with HW to reduce effect of PEBS shadow in IP distribution.","PMC1 only; Must quiesce other PMCs."),
  Counter("C1H","02H","OTHER_ASSISTS.ITLB_MISS_RETIRED","Instructions that experienced an ITLB miss.",""),
  Counter("C1H","08H","OTHER_ASSISTS.AVX_STORE","Number of assists associated with 256-bit AVX store operations.",""),
  Counter("C1H","10H","OTHER_ASSISTS.AVX_TO_SSE","Number of transitions from AVX-256 to legacy SSE when penalty applicable.",""),
  Counter("C1H","20H","OTHER_ASSISTS.SSE_TO_AVX","Number of transitions from SSE to AVX-256 when penalty applicable.",""),
  Counter("C2H","01H","UOPS_RETIRED.ALL","Counts the number of micro-ops retired, Use cmask=1 and invert to count active cycles or stalled cycles.","Supports PEBS"),
  Counter("C2H","02H","UOPS_RETIRED.RETIRE_SLOTS","Counts the number of retirement slots used each cycle.",""),
  Counter("C3H","02H","MACHINE_CLEARS.MEMORY_ORDERING","Counts the number of machine clears due to memory order conflicts.",""),
  Counter("C3H","04H","MACHINE_CLEARS.SMC","Counts the number of times that a program writes to a code section.",""),
  Counter("C3H","20H","MACHINE_CLEARS.MASKMOV","Counts the number of executed AVX masked load operations that refer to an illegal address range with the mask bits set to 0.",""),
  Counter("C4H","00H","BR_INST_RETIRED.ALL_BRANCHES","Branch instructions at retirement.","See Table 19-1"),
  Counter("C4H","01H","BR_INST_RETIRED.CONDITIONAL","Counts the number of conditional branch instructions retired.","Supports PEBS"),
  Counter("C4H","02H","BR_INST_RETIRED.NEAR_CALL","Direct and indirect near call instructions retired.",""),
  Counter("C4H","04H","BR_INST_RETIRED.ALL_BRANCHES","Counts the number of branch instructions retired.",""),
  Counter("C4H","08H","BR_INST_RETIRED.NEAR_RETURN","Counts the number of near return instructions retired.",""),
  Counter("C4H","10H","BR_INST_RETIRED.NOT_TAKEN","Counts the number of not taken branch instructions retired.",""),
  Counter("C4H","20H","BR_INST_RETIRED.NEAR_TAKEN","Number of near taken branches retired.",""),
  Counter("C4H","40H","BR_INST_RETIRED.FAR_BRANCH","Number of far branches retired.",""),
  Counter("C5H","00H","BR_MISP_RETIRED.ALL_BRANCHES","Mispredicted branch instructions at retirement.","See Table 19-1"),
  Counter("C5H","01H","BR_MISP_RETIRED.CONDITIONAL","Mispredicted conditional branch instructions retired.","Supports PEBS"),
  Counter("C5H","02H","BR_MISP_RETIRED.NEAR_CALL","Direct and indirect mispredicted near call instructions retired.",""),
  Counter("C5H","04H","BR_MISP_RETIRED.ALL_BRANCHES","Mispredicted macro branch instructions retired.",""),
  Counter("C5H","10H","BR_MISP_RETIRED.NOT_TAKEN","Mispredicted not taken branch instructions retired.",""),
  Counter("C5H","20H","BR_MISP_RETIRED.TAKEN","Mispredicted taken branch instructions retired.",""),
  Counter("CAH","02H","FP_ASSIST.X87_OUTPUT","Number of X87 assists due to output value.",""),
  Counter("CAH","04H","FP_ASSIST.X87_INPUT","Number of X87 assists due to input value.",""),
  Counter("CAH","08H","FP_ASSIST.SIMD_OUTPUT","Number of SIMDFP assists due to output values.",""),
  Counter("CAH","10H","FP_ASSIST.SIMD_INPUT","Number of SIMDFP assists due to input values.",""),
  Counter("CAH","1EH","FP_ASSIST.ANY","Cycles with any input/output SSE* or FP assists.",""),
  Counter("CCH","20H","ROB_MISC_EVENTS.LBR_INSERTS","Count cases of saving new LBR records by hardware.",""),
  Counter("CDH","01H","MEM_TRANS_RETIRED.LOAD_ LATENCY","Sample loads with specified latency threshold. PMC3 only.","Specify threshold in MSR 0x3F6"),
  Counter("CDH","02H","MEM_TRANS_RETIRED.PRECISE_STORE","Sample stores and collect precise store operation via PEBS record. PMC3 only.","See Section 18.8.4.3"),

  // GO: These dont work alone -
  /*Counter("D0H","01H","MEM_UOP_RETIRED.LOADS","Qualify retired memory uops that are loads. Combine with umask 10H, 20H, 40H, 80H.","Supports PEBS. PMC0-3 only regardless HTT."),
  Counter("D0H","02H","MEM_UOP_RETIRED.STORES","Qualify retired memory uops that are stores. Combine with umask 10H, 20H, 40H, 80H.",""),
  Counter("D0H","10H","MEM_UOP_RETIRED.STLB_MIS S","Qualify retired memory uops with STLB miss. Must combine with umask 01H, 02H, to produce counts.",""),
  Counter("D0H","20H","MEM_UOP_RETIRED.LOCK","Qualify retired memory uops with lock. Must combine with umask 01H, 02H, to produce counts.",""),
  Counter("D0H","40H","MEM_UOP_RETIRED.SPLIT","Qualify retired memory uops with line split. Must combine with umask 01H, 02H, to produce counts.",""),
  Counter("D0H","80H","MEM_UOP_RETIRED.ALL","Qualify any retired memory uops. Must combine with umask 01H, 02H, to produce counts.",""),*/

  Counter("D0H","11H","MEM_UOP_RETIRED.LOADS.STLB_MISS","Qualify retired memory uops that are loads. Combine with umask 10H, 20H, 40H, 80H.","Supports PEBS. PMC0-3 only regardless HTT."),
  Counter("D0H","12H","MEM_UOP_RETIRED.STORES.STLB_MISS","Qualify retired memory uops that are stores. Combine with umask 10H, 20H, 40H, 80H.",""),

  Counter("D0H","21H","MEM_UOP_RETIRED.LOADS.LOCK","Qualify retired memory uops that are loads. Combine with umask 10H, 20H, 40H, 80H.","Supports PEBS. PMC0-3 only regardless HTT."),
  Counter("D0H","22H","MEM_UOP_RETIRED.STORES.LOCK","Qualify retired memory uops that are stores. Combine with umask 10H, 20H, 40H, 80H.",""),

  Counter("D0H","41H","MEM_UOP_RETIRED.LOADS.SPLIT","Qualify retired memory uops that are loads. Combine with umask 10H, 20H, 40H, 80H.","Supports PEBS. PMC0-3 only regardless HTT."),
  Counter("D0H","42H","MEM_UOP_RETIRED.STORES.SPLIT","Qualify retired memory uops that are stores. Combine with umask 10H, 20H, 40H, 80H.",""),

  Counter("D0H","81H","MEM_UOP_RETIRED.LOADS.ALL","Qualify retired memory uops that are loads. Combine with umask 10H, 20H, 40H, 80H.","Supports PEBS. PMC0-3 only regardless HTT."),
  Counter("D0H","82H","MEM_UOP_RETIRED.STORES.ALL","Qualify retired memory uops that are stores. Combine with umask 10H, 20H, 40H, 80H.",""),


  Counter("D1H","01H","MEM_LOAD_UOPS_RETIRED.L 1_HIT","Retired load uops with L1 cache hits as data sources.","Supports PEBS. PMC0-3 only regardless HTT"),
  Counter("D1H","02H","MEM_LOAD_UOPS_RETIRED.L 2_HIT","Retired load uops with L2 cache hits as data sources.",""),
  Counter("D1H","04H","MEM_LOAD_UOPS_RETIRED.LLC_HIT","Retired load uops which data sources were data hits in LLC without snoops required.","Supports PEBS"),
  Counter("D1H","20H","MEM_LOAD_UOPS_RETIRED.LLC_MISS","Retired load uops which data sources were data missed LLC (excluding unknown data source).","Supports PEBS"),
  Counter("D1H","40H","MEM_LOAD_UOPS_RETIRED.HIT_LFB","Retired load uops which data sources were load uops missed L1 but hit FB due to preceding miss to the same cache line with data not ready.",""),
  Counter("D4H","02H","MEM_LOAD_UOPS_MISC_RETIRED.LLC_MISS","Retired load uops with unknown information as data source in cache serviced the load.","Supports PEBS. PMC0-3 only regardless HTT"),
  Counter("E6H","01H","BACLEARS.ANY","Counts the number of times the front end is re- steered, mainly when the BPU cannot provide a correct prediction and this is corrected by other branch handling mechanisms at the front end.",""),
  Counter("F0H","01H","L2_TRANS.DEMAND_DATA_RD","Demand Data Read requests that access L2 cache.",""),
  Counter("F0H","02H","L2_TRANS.RFO","RFO requests that access L2 cache.",""),
  Counter("F0H","04H","L2_TRANS.CODE_RD","L2 cache accesses when fetching instructions.",""),
  Counter("F0H","08H","L2_TRANS.ALL_PF","L2 or LLC HW prefetches that access L2 cache.","including rejects"),
  Counter("F0H","10H","L2_TRANS.L1D_WB","L1D writebacks that access L2 cache.",""),
  Counter("F0H","20H","L2_TRANS.L2_FILL","L2 fill requests that access L2 cache.",""),
  Counter("F0H","40H","L2_TRANS.L2_WB","L2 writebacks that access L2 cache.",""),
  Counter("F0H","80H","L2_TRANS.ALL_REQUESTS","Transactions accessing L2 pipe.",""),
  Counter("F1H","01H","L2_LINES_IN.I","L2 cache lines in I state filling L2.","Counting does not cover rejects."),
  Counter("F1H","02H","L2_LINES_IN.S","L2 cache lines in S state filling L2.","Counting does not cover rejects."),
  Counter("F1H","04H","L2_LINES_IN.E","L2 cache lines in E state filling L2.","Counting does not cover rejects."),
  Counter("F1H","07H","L2_LINES_IN.ALL","L2 cache lines filling L2.","Counting does not cover rejects."),
  Counter("F2H","01H","L2_LINES_OUT.DEMAND_CLEAN","Clean L2 cache lines evicted by demand.",""),
  Counter("F2H","02H","L2_LINES_OUT.DEMAND_DIRTY","Dirty L2 cache lines evicted by demand.",""),
  Counter("F2H","04H","L2_LINES_OUT.PF_CLEAN","Clean L2 cache lines evicted by L2 prefetch.",""),
  Counter("F2H","08H","L2_LINES_OUT.PF_DIRTY","Dirty L2 cache lines evicted by L2 prefetch.",""),
  Counter("F2H","0AH","L2_LINES_OUT.DIRTY_ALL","Dirty L2 cache lines filling the L2.","Counting does not cover rejects."),
  Counter("F4H","10H","SQ_MISC.SPLIT_LOCK","Split locks in SQ.",""),

   Counter("D3H","01H","MEM_LOAD_UOPS_LLC_MISS_ RETIRED.LOCAL_DRAM","Retired load uops which data sources were data missed LLC but serviced by local DRAM.","Supports PEBS"),
   Counter("D3H","04H","MEM_LOAD_UOPS_LLC_MISS_ RETIRED.REMOTE_DRAM","Retired load uops which data sources were data missed LLC but serviced by remote DRAM.","Supports PEBS"),

   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_CODE_RD.LLC_MISS.ANY_RESPONSE_N","","0x3FFFC00004"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_CODE_RD.LLC_MISS.LOCAL_DRAM_N","","0x600400004"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_CODE_RD.LLC_MISS.REMOTE_DRAM_N","","0x67F800004"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_CODE_RD.LLC_MISS.REMOTE_HIT_FWD_N","","0x87F800004"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_CODE_RD.LLC_MISS.REMOTE_HITM_N","","0x107FC00004"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.ANY_DRAM_N","","0x67FC00001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.ANY_RESPONSE_N","","0x3F803C0001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.LOCAL_DRAM_N","","0x600400001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.REMOTE_DRAM_N","","0x67F800001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.REMOTE_HIT_FWD_N","","0x87F800001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.DEMAND_DATA_RD.LLC_MISS.REMOTE_HITM_N","","0x107FC00001"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_CODE_RD.LLC_MISS.ANY_RESPONSE_N","","0x3F803C0040"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_DATA_RD.LLC_MISS.ANY_DRAM_N","","0x67FC00010"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_DATA_RD.LLC_MISS.ANY_RESPONSE_N","","0x3F803C0010"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_DATA_RD.LLC_MISS.LOCAL_DRAM_N","","0x600400010"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_DATA_RD.LLC_MISS.REMOTE_DRAM_N","","0x67F800010"),
   Counter("B7H","01H","OFFCORE_RESPONSE.PF_L2_DATA_RD.LLC_MISS.REMOTE_HIT_FWD_N","","0x87F800010"),
   Counter("B7H","01H","Custom_all","",     "0x3F80400FFF"),
   Counter("B7H","01H","DMND_DATA_RD","",   "0x3F80400001"),
   Counter("B7H","01H","DMND_RFO","",       "0x3F80400002"),
   Counter("B7H","01H","DMND_IFETCH","",    "0x3F80400004"),
   Counter("B7H","01H","WB","",             "0x3F80400008"),
   Counter("B7H","01H","PF_DATA_RD","",     "0x3F80400010"),
   Counter("B7H","01H","PF_RFO","",         "0x3F80400020"),
   Counter("B7H","01H","PF_IFETCH","",      "0x3F80400040"),
   Counter("B7H","01H","PF_LLC_DATA_RD","", "0x3F80400080"),
   Counter("B7H","01H","PF_LLC_RFO","",     "0x3F80400100"),
   Counter("B7H","01H","PF_IFETCH","",      "0x3F80400200"),
   Counter("B7H","01H","BUS_LOCKS","",      "0x3F80400400"),
   Counter("B7H","01H","STRM_ST","",        "0x3F80400800"),
   Counter("B7H","01H","OTHER","",          "0x3F80408000")
   )


  val flops_single = (Array(
    Counter("10H","20H","FP_COMP_OPS_EXE.SSE_FP_SCALAR_SINGLE","Counts number of SSE* single precision FP scalar uops executed.",""),
    Counter("10H","40H","FP_COMP_OPS_EXE.SSE_PACKED_SINGLE","Counts number of SSE* single precision FP packed uops executed.",""),
    Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.",""),
    Counter("10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed.","")
  ),
    List(1,4,8,0)) //this is the multiplication mask for calculating flops


  val flops_double = (Array(
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
    Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.",""),
    Counter("10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed.","")
  ),
    List(1,2,4,0)) //this is the multiplication mask for calculating flops

}


object Westmere {
    //GO: Note that we cannot do mixed measurements with single and double precision with these counters!
    val flops_double = (Array(
    Counter("10H","20H","FP_COMP_OPS_EXE.SSE_FP_SCALAR","Counts number of SSE FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED","Counts number of SSE FP packed uops executed.",""),
    Counter("10H","40H","FP_COMP_OPS_EXE.SSE_SINGLE_PRECISION","Counts number of SSE* FP single precision uops executed.",""),
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_DOUBLE_PRECISION","Counts number of SSE* FP double precision uops executed.","")
  ),
    List(1,2,0,0)) //this is the multiplication mask for calculating flops

  val flops_single = (Array(
    Counter("10H","20H","FP_COMP_OPS_EXE.SSE_FP_SCALAR","Counts number of SSE FP scalar uops executed.",""),
    Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED","Counts number of SSE FP packed uops executed.",""),
    Counter("10H","40H","FP_COMP_OPS_EXE.SSE_SINGLE_PRECISION","Counts number of SSE* FP single precision uops executed.",""),
    Counter("10H","80H","FP_COMP_OPS_EXE.SSE_DOUBLE_PRECISION","Counts number of SSE* FP double precision uops executed.","")
  ),
    List(1,4,0,0)) //this is the multiplication mask for calculating flops

}


object IvyBridge {

    val flops = Array (
        Counter("10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed.",""),
        Counter("10H","80H","FP_COMP_OPS_EXE.SSE_SCALAR_DOUBLE","Counts number of SSE* double precision FP scalar uops executed.",""),
        Counter("10H","10H","FP_COMP_OPS_EXE.SSE_FP_PACKED_DOUBLE","Counts number of SSE* double precision FP packed uops executed.",""),
        Counter("11H","02H","SIMD_FP_256.PACKED_DOUBLE","Counts 256-bit packed double-precision floating- point instructions.","")
    )

    val single_flops = Array (
        Counter("10H","01H","FP_COMP_OPS_EXE.X87","Counts number of X87 uops executed.",""),
        Counter("10H","20H","FP_COMP_OPS_EXE.SSE_FP_SCALAR_SINGLE","Counts number of SSE* single precision FP scalar uops executed.",""),
        Counter("10H","40H","FP_COMP_OPS_EXE.SSE_PACKED_SINGLE","Counts number of SSE* single precision FP packed uops executed.",""),
        Counter("11H","01H","SIMD_FP_256.PACKED_SINGLE","Counts 256-bit packed single-precision floating- point instructions.","")
    )

    val tlbs = Array (
        Counter("08H","81H","DTLB_LOAD_MISSES.MISS_CAUSES_A_WALK","Misses in all TLB levels that cause a page walk of any page size.",""),
        Counter("5FH","01H","TLB_ACCESS.LOAD_STLB_HIT","Number of cache load STLB hits. No page walk.",""),
        Counter("49H","01H","DTLB_STORE_MISSES.MISS_CAUSES_A_WALK","Miss in all TLB levels causes an page walk of any page size (4K/2M/4M/1G).",""),
        Counter("49H","10H","DTLB_STORE_MISSES.STLB_HIT","Store operations that miss the first TLB level but hit the second and do not cause page walks.","")
    )

    val offcore_rd = Array  (
        Counter("B7H","01H","OFFCORE_RESPONSE.ALL_DATA_RD.LLC_MISS.DRAM_N","","0x300400091"),
        Counter("BBH","01H","OFFCORE_RESPONSE.ALL_RFO.LLC_MISS.DRAM_N","","0x300400122")
    )

    val uncore_rd_wr = Array (
        Counter("81H","80H","UNC_ARB_TRK_REQUEST.EVICTIONS","Counts the number of LLC evictions allocated.",""),
        Counter("34H","88H","UNC_CBO_CACHE_LOOKUP.ANY.STATE_I","LLC lookup request that access cache and found line in I-state.","")
    )
}
