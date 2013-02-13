import sys
import os.path
import csv 

if __name__ == "__main__":
    
    serie = sys.argv[2]
    
    flopfile = open("flop_"+serie+".txt", 'a')
    tscfile = open("tsc_"+serie+".txt", 'a')
    
    if os.path.exists("size_"+serie+".txt"):
        sizefile = open("size_"+serie+".txt", 'a')
        sizefile.write(" " + sys.argv[1])
    else:
        sizefile = open("size_"+serie+".txt", 'w')
        sizefile.write(sys.argv[1])
    sizefile.close()
    
    runsfile = open("nrruns.txt", 'r')
    cumflopScalarfile = open("Custom_ev0_core3.txt", 'r')
    cumflopSSEfile    = open("Custom_ev1_core3.txt", 'r')
    cumflopAVXfile    = open("Custom_ev2_core3.txt", 'r')
    cumtscfile = open("TSC_core_3.txt", 'r')

    runsreader = csv.reader(runsfile, delimiter=' ')
    cumflopScalarreader = csv.reader(cumflopScalarfile, delimiter=' ')
    cumflopSSEreader = csv.reader(cumflopSSEfile, delimiter=' ')
    cumflopAVXreader = csv.reader(cumflopAVXfile, delimiter=' ')
    cumtscreader = csv.reader(cumtscfile, delimiter=' ')
    
    runs = [ float(v) for v in runsreader.next() if v != '' ]
    cumflopScalar = [ float(v) for v in cumflopScalarreader.next() if v != '' ]
    cumflopSSE = [ float(v) for v in cumflopSSEreader.next() if v != '' ]
    cumflopAVX = [ float(v) for v in cumflopAVXreader.next() if v != '' ]
    cumtsc = [ float(v) for v in cumtscreader.next() if v != '' ]

    flop = [ (fs+2*fsse+4*favx)/r for fs, fsse, favx, r in zip(cumflopScalar, cumflopSSE, cumflopAVX, runs) ]
    tsc = [ t/r for t,r in zip(cumtsc, runs) ]
    
    flopfile.write(" ".join( [str(f) for f in flop] ) + "\n")
    tscfile.write(" ".join( [str(t) for t in tsc] ) + "\n")
    
    flopfile.close()
    tscfile.close()
    runsfile.close()
    cumflopScalarfile.close()
    cumflopSSEfile.close()
    cumflopAVXfile.close()
    cumtscfile.close()