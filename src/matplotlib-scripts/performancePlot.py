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
import matplotlib.ticker

   
background_color = '#eeeeee' 
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


X_MIN=10
X_MAX=1000000
Y_MIN=0
Y_MAX=1
LOG_X=1
LOG_Y=0
TITLE="TITLE"
X_LABEL="Problem Size [Complex Doubles]"
Y_LABEL="Performance [Flop/Cycle]"
OUTPUT_FILE="errorPlot.pdf"

fig = plt.figure()
# Returns the Axes instance
ax = fig.add_subplot(111)
ffont = {'family':'sans-serif','fontsize':10,'weight':'bold'}
ax.set_xticklabels(ax.get_xticks(),ffont)
ax.set_yticklabels(ax.get_yticks(),ffont)

#Log scale
if LOG_Y:  ax.set_yscale('log')
if LOG_X: ax.set_xscale('log')

#formatting:
ax.set_title(TITLE,fontsize=14,fontweight='bold')
ax.set_xlabel(X_LABEL, fontsize=12)
ax.set_ylabel(Y_LABEL, fontsize=12)


#x-y range
ax.axis([X_MIN,X_MAX,Y_MIN,Y_MAX])

#TODO: When log scale, axis labels have a different font...
ticks_font = matplotlib.font_manager.FontProperties(family='Helvetica', style='normal', size=12, weight='normal', stretch='normal')
if LOG_X:
	ax.xaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
	#ax.xaxis.major.formatter.set_powerlimits((-3, 6)) 
	for label in ax.get_xticklabels() :
		label.set_fontproperties(ticks_font)

if LOG_X:
        ax.yaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
        #ax.yaxis.major.formatter.set_powerlimits((-3, 6)) 
        for label in ax.get_yticklabels() :
                label.set_fontproperties(ticks_font)

# Load the data --- At the moment, false data, need to figure our 
# how to read the data depending on the format
x1=[0.5,0.6,0.7,0.8,0.5,0.7]
x2=[0.5,0.6,0.7,0.8,0.5,0.7]
x3=[0.5,0.5,0.5,0.5,0.5,0.5]
mediansX = [100.0,1000.0,10000.0]
mediansY = [0.65,0.65,0.5]

# Plot a line between the medians of each dataset
ax.plot(mediansX, mediansY, '-', lw=1,color='black', label="serie 1" )


#Percentile boxes
locs, labels = xticks() 
rectanglesWidths = [x/3 for x in mediansX]
bp = ax.boxplot([x1,x2,x3], positions=mediansX, widths = rectanglesWidths)
setp(bp['medians'], color='black')
setp(bp['fliers'], color='black')
setp(bp['whiskers'], color='black', linestyle= '-')
setp(bp['boxes'], color='black')
setp(bp['caps'], color='black')

#Restore xticks location --> Needed to locate boxes in arbitrary positions
xticks(locs)


ax.legend(numpoints=1, loc='best',fontsize =6,frameon = False )

#save file
fig.savefig(OUTPUT_FILE, dpi=250,  bbox_inches='tight')
