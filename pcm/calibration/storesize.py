import sys
import os.path
import csv 

if __name__ == "__main__":
    
    flopfile = open("flop.txt", 'a')
    tscfile = open("tsc.txt", 'a')
    
    if os.path.exists("size.txt"):
        sizefile = open("size.txt", 'a')
        sizefile.write(" " + sys.argv[1])
    else:
        sizefile = open("size.txt", 'w')
        sizefile.write(sys.argv[1])
    sizefile.close()
    
    runsfile = open("runs.txt", 'r')
    cumflopfile = open("Custom_ev2_core3.txt", 'r')
    cumtscfile = open("TSC_core_3.txt", 'r')

    runsreader = csv.reader(runsfile, delimiter=' ')
    cumflopreader = csv.reader(cumflopfile, delimiter=' ')
    cumtscreader = csv.reader(cumtscfile, delimiter=' ')
    
    runs = [ float(v) for v in runsreader.next() if v != '' ]
    cumflop = [ float(v) for v in cumflopreader.next() if v != '' ]
    cumtsc = [ float(v) for v in cumtscreader.next() if v != '' ]

    flop = [ f/r for f,r in zip(cumflop, runs) ]
    tsc = [ t/r for t,r in zip(cumtsc, runs) ]
    
    flopfile.write(" ".join( [str(f) for f in flop] ) + "\n")
    tscfile.write(" ".join( [str(t) for t in tsc] ) + "\n")
    
    flopfile.close()
    tscfile.close()
    runsfile.close()
    cumflopfile.close()
    cumtscfile.close()