import sys
import os.path
import csv
import shlex
import subprocess

if __name__ == "__main__":
    
    affinity = sys.argv[3]
    serie = sys.argv[2]
    size = sys.argv[1]

    flopfile = open("flop_"+serie+".txt", 'a')
    tscfile = open("tsc_"+serie+".txt", 'a')
    
    if os.path.exists("size_"+serie+".txt"):
        sizefile = open("size_"+serie+".txt", 'a')
        sizefile.write(" " + sys.argv[1])
    else:
        sizefile = open("size_"+serie+".txt", 'w')
        sizefile.write(size)
    sizefile.close()
    
    runsfile = open("nrruns.txt", 'r')
    
    cumflopScalarfile = open("Custom_ev0_core" + affinity + ".txt", 'r')
    cumflopSSEfile    = open("Custom_ev1_core" + affinity + ".txt", 'r')
    cumflopAVXfile    = open("Custom_ev2_core" + affinity + ".txt", 'r')
    
    cumtscfiles = []
    for i in range(4):
        cumtscfiles.append(open("TSC_core_" + str(i) + ".txt", 'r'))

    runsreader = csv.reader(runsfile, delimiter=' ')
    cumflopScalarreader = csv.reader(cumflopScalarfile, delimiter=' ')
    cumflopSSEreader = csv.reader(cumflopSSEfile, delimiter=' ')
    cumflopAVXreader = csv.reader(cumflopAVXfile, delimiter=' ')
    cumtscreaders = []
    for i in range(4):
        cumtscreaders.append(csv.reader(cumtscfiles[i], delimiter=' '))
    cumtscrows = []
    for i in range(4):
        cumtscrows.append(cumtscreaders[i].next())
        
    runs = [ float(v) for v in runsreader.next() if v != '' ]
    cumflopScalar = [ float(v) for v in cumflopScalarreader.next() if v != '' ]
    cumflopSSE = [ float(v) for v in cumflopSSEreader.next() if v != '' ]
    cumflopAVX = [ float(v) for v in cumflopAVXreader.next() if v != '' ]
    cumtsc = [ max(map(lambda v: float(v), vs)) for vs in zip(*cumtscrows) if all(map(lambda v: v != "", vs)) ]
    
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
    for i in range(4):
        cumtscfiles[i].close()
    
    foldername = serie + "-" + size
    if not os.path.exists(foldername):
        os.mkdir(foldername)
    
    mv = "mv"
    print "* Storing dump files into " + foldername 
    args = shlex.split(mv + " log.txt " + foldername + "/log.txt")
    subprocess.call(args)
    args = shlex.split(mv + " nrruns.txt " + foldername + "/nrruns.txt")
    subprocess.call(args)
    for i in range(3):
        filename = "Custom_ev" + str(i) + "_core" + affinity + ".txt"
        args = shlex.split(mv + " " + filename + " " + foldername + "/" + filename)
        subprocess.call(args)
    for i in range(4):
        args = shlex.split(mv + " TSC_core_" + str(i) + ".txt " + foldername + "/")
        subprocess.call(args)
