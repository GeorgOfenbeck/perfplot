#General imports:

import sys
import os
import math
from matplotlib import rc
rc('text', usetex=True) # this is if you want to use latex to print text. If you do you can create strings that go on labels or titles like this for example (with an r in front): r"$n=$ " + str(int(n))
from numpy import *
from pylab import *
import random
from matplotlib.font_manager import FontProperties
import matplotlib.pyplot as plt
import matplotlib.lines as lns
from scipy import stats



background_color =(0.85,0.85,0.85) #'#C0C0C0'    
grid_color = 'white' #FAFAF7'
matplotlib.rc('axes', facecolor = background_color)
matplotlib.rc('axes', edgecolor = grid_color)
matplotlib.rc('axes', linewidth = 1.2)
matplotlib.rc('axes', grid = True )
matplotlib.rc('axes', axisbelow = True)
matplotlib.rc('grid',color = grid_color)
matplotlib.rc('grid',linestyle='-' )
matplotlib.rc('grid',linewidth=0.7 )
matplotlib.rc('xtick.major',size =0 )
matplotlib.rc('xtick.minor',size =0 )
matplotlib.rc('ytick.major',size =0 )
matplotlib.rc('ytick.minor',size =0 )
matplotlib.rc('font', family='serif')

X_MIN=0.01
X_MAX=500.0
Y_MIN=0.01
Y_MAX=20.0
PEAK_PERF=8.0
PEAK_BW=2.2
ASPECT_RATIO=0.618
LOG_X=1
LOG_Y=1
OUTPUT_FILE="data-rooflinePlot.pdf"
TITLE="TITLE"
X_LABEL="Operational Intensity [Flop/Byte]"
Y_LABEL="Performance [Flop/Cycle]"
ANNOTATE_POINTS=1
series = ['MVM']
colors=[(0.6,0.011,0.043), (0.258, 0.282, 0.725),(0.2117, 0.467, 0.216),'#CC0033' ,'#FFFF00' ]
fig = plt.figure()
# Returns the Axes instance
ax = fig.add_subplot(111, aspect = ASPECT_RATIO)
#ffont = {'family':'sans-serif','fontsize':10,'weight':'bold'}
#ax.set_xticklabels(ax.get_xticks(),ffont)
#ax.set_yticklabels(ax.get_yticks(),ffont)

#Log scale
if LOG_Y: ax.set_yscale('log')
if LOG_X: ax.set_xscale('log')

#formatting:
ax.set_title(TITLE,fontsize=14,fontweight='bold')
ax.set_xlabel(X_LABEL, fontsize=12)
ax.set_ylabel(Y_LABEL, fontsize=12)


#x-y range
ax.axis([X_MIN,X_MAX,Y_MIN,Y_MAX])

#TODO: When log scale, axis labels have a different font...
#ticks_font = matplotlib.font_manager.FontProperties(family='Helvetica', style='normal', size=12, weight='normal', stretch='normal')
#if LOG_X:
#        ax.xaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
        #ax.xaxis.major.formatter.set_powerlimits((-3, 6)) 
#        for label in ax.get_xticklabels() :
#                label.set_fontproperties(ticks_font)

#if LOG_X:
#        ax.yaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
        #ax.yaxis.major.formatter.set_powerlimits((-3, 6)) 
#        for label in ax.get_yticklabels() :
#                label.set_fontproperties(ticks_font)



# Manually adjust xtick/ytick labels when log scale
if LOG_X:
    locs, labels = xticks()
    minloc =int(log10(X_MIN))
    maxloc =int(log10(X_MAX) +1)
    newlocs = []
    newlabels = []
    for i in range(minloc,maxloc):
        newlocs.append(10**i)
        # Do not plot the first label, it is ugly in the corner
        if i==minloc:
			newlabels.append('')
        elif 10**i <= 100:
            newlabels.append(str(10**i))
        else:
            newlabels.append(r'$10^ %d$' %i)
    xticks(newlocs, newlabels)

if LOG_Y:
    locs, labels = yticks()
    minloc =int(log10(Y_MIN))
    maxloc =int(log10(Y_MAX) +1)
    newlocs = []
    newlabels = []
    for i in range(minloc,maxloc):
		newlocs.append(10**i)
		if i==minloc:
			newlabels.append('')
		elif 10**i <= 100:
			newlabels.append(str(10**i))
		else:
			newlabels.append(r'$10^ %d$' %i)
    yticks(newlocs, newlabels)

# Load the data 

pp = []
ss=[]
for serie,i in zip(series,range(len(series))):
	
	nCycles = []
    	file_in = open('tsc_'+serie+'.txt','r')
    	lines = file_in.readlines()
    	for line in lines:
        	split_line = line.rstrip('\n').split(' ')
        	nCycles.append(split_line)

    	file_in.close()

	nFLOPS = []
    	file_in = open('flop_'+serie+'.txt','r')
    	lines = file_in.readlines()
    	for line in lines:
            	split_line = line.rstrip('\n').split(' ')
            	nFLOPS.append(split_line)

    	file_in.close()
	
	bytesTransferred = []
        file_in = open('bytes_transferred_'+serie+'.txt','r')
        lines = file_in.readlines()
        for line in lines:
                split_line = line.rstrip('\n').split(' ')
                bytesTransferred.append(split_line)

        file_in.close()

    	yData =[]
    	for f,c in zip(nFLOPS,nCycles):
        	yData.append([float(vf)/float(vc) for vf, vc in zip(f,c) if vf != '' and vc != ''])

	xData =[]
    	for f,b in zip(nFLOPS,bytesTransferred):
        	xData.append([float(vf)/float(vb) for vf, vb in zip(f,b) if vf != '' and vb != '' and float(vb)!= 0])

	x=[]
	xerr_low=[]
	xerr_high = []
	yerr_high = []
	y = []
	yerr_low = []

	for xDataItem in xData:
		x.append(stats.scoreatpercentile(xDataItem, 50))
		xerr_low.append(stats.scoreatpercentile(xDataItem, 25))
		xerr_high.append(stats.scoreatpercentile(xDataItem, 75))	
	
	for yDataItem in yData:
		y.append(stats.scoreatpercentile(yDataItem, 50))
		yerr_low.append(stats.scoreatpercentile(yDataItem, 25))
		yerr_high.append(stats.scoreatpercentile(yDataItem, 75)) 

	xerr_low = [a - b for a, b in zip(x, xerr_low)] 
	xerr_high = [a - b for a, b in zip(xerr_high, x)]
	yerr_low = [a - b for a, b in zip(y, yerr_low)]
	yerr_high = [a - b for a, b in zip(yerr_high, y)]

	#print x
	#print xerr_low
	#print xerr_high
	#print y
	#print yerr_low
	#print yerr_high

	p, =ax.plot(x, y, '-', color=colors[i],label=serie)
	pp.append(p)
	ss.append(serie);
	ax.errorbar(x, y, yerr=[yerr_low, yerr_high], xerr=[xerr_low, xerr_high], color=colors[i])  

	# Read sizes	
	sizes = []
	file_in = open('size_'+serie+'.txt','r')
	lines = file_in.readlines()
	for line in lines:
		split_line = line.rstrip('\n').split(' ')
		sizes.append(split_line)

	file_in.close()

	if ANNOTATE_POINTS:
		ax.annotate(sizes[0][0],
        xy=(x[0], y[0]), xycoords='data',
        xytext=(+3, +1), textcoords='offset points', fontsize=8)

		ax.annotate(sizes[0][len(sizes[0])-1],
        xy=(x[len(x)-1],y[len(y)-1]), xycoords='data',
        xytext=(+3, +1), textcoords='offset points', fontsize=8)

ax.legend(pp,ss, numpoints=1, loc='best',fontsize =6,frameon = False )





#Peak performance line and text
ax.axhline(y=PEAK_PERF, linewidth=1, color='black')
ax.text(X_MAX/10.0, PEAK_PERF+0.1, "Peak Performance ("+str(PEAK_PERF)+" F/C)", fontsize=8)


#BW line and text
x = np.linspace(X_MIN, X_MAX, X_MAX)
y = x*PEAK_BW 
ax.plot(x, y, linewidth=1, color='black')


l2 = array((0.01,0.01))
angle = 45*(ASPECT_RATIO+0.05)
trans_angle = gca().transData.transform_angles(array((angle,)),
                                               l2.reshape((1,2)))[0]

ax.text(X_MIN,X_MIN*PEAK_BW+0.1,'MemLoad ('+str(PEAK_BW)+' B/C)',fontsize=8,
           rotation=trans_angle)


#save file
fig.savefig(OUTPUT_FILE, dpi=250,  bbox_inches='tight')
