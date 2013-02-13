//------------------------------------------------------------------------------
//  Auxiliary data structures and info to facilitate getting
//  processor information with cpuid.
//  Mostly obatined from
//  http://www.opensource.apple.com/source/xnu/xnu-1456.1.26/osfmk/i386/cpuid.c
//  and the "Intel® Processor Identification and the CPUID Instruction", May 2012
//------------------------------------------------------------------------------

#ifndef CPUID_INFO_HEADER
#define CPUID_INFO_HEADER

#include <stdint.h>
/*
 * These multipliers are used to encode 1*K .. 64*M in a 16 bit size field
 */
#define	K	(1024)
#define	M	(2048)


/*
 * Leaf 2 cache descriptor encodings.
 */
typedef enum {
	_NULL_,		/* NULL (empty) descriptor */
	CACHE,		/* Cache */
	TLB,		/* TLB */
	STLB,		/* Shared second-level unified TLB */
	PREFETCH	/* Prefetch size */
} cpuid_leaf2_desc_type_t;



typedef enum {
	NA,		/* Not Applicable */
	FULLY,		/* Fully-associative */
	TRACE,		/* Trace Cache (P4 only) */
	INST,		/* Instruction TLB */
	DATA,		/* Data TLB */
	L1,		/* L1 (unified) cache */
	L1_INST,	/* L1 Instruction cache */
	L1_DATA,	/* L1 Data cache */
	L2,		/* L2 (unified) cache */
	L3,		/* L3 (unified) cache */
	L2_2LINESECTOR,	/* L2 (unified) cache with 2 lines per sector */
	L3_2LINESECTOR,	/* L3(unified) cache with 2 lines per sector */
	SMALL,		/* Small page TLB */
	LARGE,		/* Large page TLB */
	BOTH		/* Small and Large page TLB */
} cpuid_leaf2_qualifier_t;

typedef struct cpuid_cache_descriptor {
	uint8_t		value;		/* descriptor code */
	uint8_t		type;		/* cpuid_leaf2_desc_type_t */
	uint8_t		level;		/* level of cache/TLB hierachy */
	uint8_t		ways;		/* wayness of cache */
	uint32_t	size;		/* cachesize or TLB pagesize */
	uint16_t	entries;	/* number of TLB entries or linesize */
} cpuid_cache_descriptor_t;



typedef struct tlb_info {
	uint32_t	size;		/* cachesize or TLB pagesize */
	uint16_t	entries;	/* number of TLB entries or linesize */
} tlb_info_t;



static cpuid_cache_descriptor_t intel_cpuid_leaf2_descriptor_table[] = {
  //	----------------------------------------------------------------
  //	value	type	level		ways	(page size/size)	(entries/line size)
  //	----------------------------------------------------------------
	{ 0x00,	_NULL_,	NA,		NA,	NA,	NA  },
	{ 0x01,	TLB,	INST,		4,	4*K,	32  },
	{ 0x02,	TLB,	INST,		FULLY,	4*M,	2   },
	{ 0x03,	TLB,	DATA,		4,	4*K,	64  },
	{ 0x04,	TLB,	DATA,		4,	4*M,	8   },
	{ 0x05,	TLB,	DATA,		4,	4*M,	32  },
	{ 0x06,	CACHE,	L1_INST,	4,	8*K,	32  },
	{ 0x08,	CACHE,	L1_INST,	4,	16*K,	32  },
	{ 0x09,	CACHE,	L1_INST,	4,	32*K,	64  },
	{ 0x0A,	CACHE,	L1_DATA,	2,	8*K,	32  },
	{ 0x0B,	TLB,	INST,		4,	4*M,	4   },
	{ 0x0C,	CACHE,	L1_DATA,	4,	16*K,	32  },
	{ 0x0D,	CACHE,	L1_DATA,	4,	16*K,	64  },
	{ 0x0E,	CACHE,	L1_DATA,	6,	24*K,	64  },
	{ 0x21,	CACHE,	L2,		8,	256*K,	64  },
	{ 0x22,	CACHE,	L3_2LINESECTOR,	4,	512*K,	64  },
	{ 0x23,	CACHE,	L3_2LINESECTOR, 8,	1*M,	64  },
	{ 0x25,	CACHE,	L3_2LINESECTOR,	8,	2*M,	64  },
	{ 0x29,	CACHE,	L3_2LINESECTOR, 8,	4*M,	64  },
	{ 0x2C,	CACHE,	L1_DATA,	8,	32*K,	64  },
	{ 0x30,	CACHE,	L1_INST,	8,	32*K,	64  },
	{ 0x40,	CACHE,	L2,		NA,	0,	NA  },
	{ 0x41,	CACHE,	L2,		4,	128*K,	32  },
	{ 0x42,	CACHE,	L2,		4,	256*K,	32  },
	{ 0x43,	CACHE,	L2,		4,	512*K,	32  },
	{ 0x44,	CACHE,	L2,		4,	1*M,	32  },
	{ 0x45,	CACHE,	L2,		4,	2*M,	32  },
	{ 0x46,	CACHE,	L3,		4,	4*M,	64  },
	{ 0x47,	CACHE,	L3,		8,	8*M,	64  },
	{ 0x48,	CACHE,	L2,		12, 	3*M,	64  },
	{ 0x49,	CACHE,	L2,		16,	4*M,	64  },
	{ 0x4A,	CACHE,	L3,		12, 	6*M,	64  },
	{ 0x4B,	CACHE,	L3,		16,	8*M,	64  },
	{ 0x4C,	CACHE,	L3,		12, 	12*M,	64  },
	{ 0x4D,	CACHE,	L3,		16,	16*M,	64  },
	{ 0x4E,	CACHE,	L2,		24,	6*M,	64  },
	{ 0x4F,	TLB,	INST,		NA,	4*K,	32  },
	{ 0x50,	TLB,	INST,		NA,	4*M,	64  },
	{ 0x51,	TLB,	INST,		NA,	4*M,	128 },
	{ 0x52,	TLB,	INST,		NA,	4*M,	256 },
	{ 0x55,	TLB,	INST,		FULLY,	4*M,	7   },
	{ 0x56,	TLB,	DATA,		4,	4*M,	16  },
	{ 0x57,	TLB,	DATA,		4,	4*K,	16  },
	{ 0x59,	TLB,	DATA,		FULLY,	4*K,	16  },
	{ 0x5A,	TLB,	DATA,		4,	4*M,	32  },
	{ 0x5B,	TLB,	DATA,		NA,	4*M,	64  },
	{ 0x5C,	TLB,	DATA,		NA,	4*M,	128 },
	{ 0x5D,	TLB,	DATA,		NA,	4*M,	256 },
	{ 0x60,	CACHE,	L1,		8,	16*K,	64  },
	{ 0x66,	CACHE,	L1,		4,	8*K,	64  },
  { 0x67,	CACHE,	L1,		4,	16*K,	64  },
  { 0x68,	CACHE,	L1,		4,	32*K,	64  },
	{ 0x70,	CACHE,	TRACE,		8,	12*K,	NA  },
	{ 0x71,	CACHE,	TRACE,		8,	16*K,	NA  },
	{ 0x72,	CACHE,	TRACE,		8,	32*K,	NA  },
  { 0x76,	TLB,	INST,		FULLY,	4*M,	8},
	{ 0x78,	CACHE,	L2,		4,	1*M,	64  },
	{ 0x79,	CACHE,	L2_2LINESECTOR,	8,	128*K,	64  },
	{ 0x7A,	CACHE,	L2_2LINESECTOR,	8,	256*K,	64  },
	{ 0x7B,	CACHE,	L2_2LINESECTOR,	8,	512*K,	64  },
	{ 0x7C,	CACHE,	L2_2LINESECTOR,	8,	1*M,	64  },
	{ 0x7D,	CACHE,	L2,		8,	2*M,	64  },
	{ 0x7F,	CACHE,	L2,		2,	512*K,	64  },
	{ 0x80,	CACHE,	L2,		8,	512*K,	64  },
	{ 0x82,	CACHE,	L2,		8,	256*K,	32  },
	{ 0x83,	CACHE,	L2,		8,	512*K,	32  },
	{ 0x84,	CACHE,	L2,		8,	1*M,	32  },
	{ 0x85,	CACHE,	L2,		8,	2*M,	32  },
	{ 0x86,	CACHE,	L2,		4,	512*K,	64  },
	{ 0x87,	CACHE,	L2,		8,	1*M,	64  },
	{ 0xB0,	TLB,	INST,		4,	4*K,	128 },
	{ 0xB1,	TLB,	INST,		4,	4*M,	8   },
	{ 0xB2,	TLB,	INST,		4,	4*K,	64  },
	{ 0xB3,	TLB,	DATA,		4,	4*K,	128 },
	{ 0xB4,	TLB,	DATA,		4,	4*K,	256 },
	{ 0xBA,	TLB,	DATA,		4,	4*M,	64  },
  { 0xC0,	TLB,	DATA,		4,	4*M,	8  },
	{ 0xCA,	STLB,	DATA,		4,	4*M,	512 },
	{ 0xD0,	CACHE,	L3,		4,	512*K,	64  },
	{ 0xD1,	CACHE,	L3,		4,	1*M,	64  },
	{ 0xD2,	CACHE,	L3,		4,	2*M,	64  },
	{ 0xD6,	CACHE,	L3,		8,	1*M,	64  },
	{ 0xD7,	CACHE,	L3,		8,	2*M,	64  },
	{ 0xD8,	CACHE,	L3,		8,	4*M,	64  },
	{ 0xDC,	CACHE,	L3,		12, 	1536*K,	64  },
	{ 0xDD,	CACHE,	L3,		12, 	3*M,	64  },
	{ 0xDE,	CACHE,	L3,		12, 	6*M,	64  },
	{ 0xE2,	CACHE,	L3,		16,	2*M,	64  },
	{ 0xE3,	CACHE,	L3,		16,	4*M,	64  },
	{ 0xE4,	CACHE,	L3,		16,	8*M,	64  },
  { 0xEA,	CACHE,	L3,		24,	12*M,	64  },
  { 0xEB,	CACHE,	L3,		24,	18*M,	64  },
  { 0xEC,	CACHE,	L3,		24,	24*M,	64  },
	{ 0xF0,	PREFETCH, NA,		NA,	64,	NA  },
	{ 0xF1,	PREFETCH, NA,		NA,	128,	NA  }
};

#endif
