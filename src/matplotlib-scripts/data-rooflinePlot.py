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


X_MIN=0.01
X_MAX=300.0
Y_MIN=0.01
Y_MAX=20.0
PEAK_PERF=1.0
PEAK_BW=2.2
ASPECT_RATIO=0.618
LOG_X=1
LOG_Y=1
OUTPUT_FILE="data-rooflinePlot.pdf"
TITLE="TITLE"
X_LABEL="Operational Intensity [Flop/Byte]"
Y_LABEL="Performance [Flop/Cycle]"

fig = plt.figure()
# Returns the Axes instance
ax = fig.add_subplot(111, aspect = ASPECT_RATIO)
ffont = {'family':'sans-serif','fontsize':10,'weight':'bold'}
ax.set_xticklabels(ax.get_xticks(),ffont)
ax.set_yticklabels(ax.get_yticks(),ffont)

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


#labels = ['', '0.1','1', '10', '100', '1000']
#xlocs, xlabels = xticks()
#nXLabels = len(xlabels)
#new_xlabels = [x.format(xlocs[i]) for i,x  in enumerate(labels[0:nXLabels])]
#xticks(xlocs, new_xlabels)
#ylocs, ylabels = yticks()
#nYLabels = len(ylabels)
#new_ylabels = [x.format(ylocs[i]) for i,x  in enumerate(labels[0:nYLabels])]
#yticks(ylocs, new_ylabels)


x, y ,xerr_low, xerr_high, yerr_low, yerr_high  = loadtxt('data-roofline.dat',unpack=True)
#xerr_low, xerr_high, yerr_low, yerr_high = loadtxt('error.dat',unpack=True)
xerr_low = x - xerr_low
xerr_high = xerr_high - x
yerr_low = y - yerr_low
yerr_high = yerr_high - y

#ax.errorbar(x, y, yerr=[yerr_low, yerr_high], xerr=[xerr_low, xerr_high], marker='.',
#           color='r',
#           ecolor='r')
          # label="series 1",
          # linestyle='-')

#ax.plot(x, y,
#           color='r',
#           label="series 1",
#           linestyle='-')



pp = []
colors = ['r', 'b', 'g']
#for i in len(x):
p =ax.plot(x, y, '-', color='%s' % colors[0],label="series 1")
pp.append(p[0])
ax.errorbar(x, y, yerr=[yerr_low, yerr_high], xerr=[xerr_low, xerr_high], color='%s' % colors[0])  


ax.legend(pp,numpoints=1, loc='best',fontsize =6,frameon = False )



#Anotate 
# TODO: make it if anotate
ax.annotate('Variant 0',
         xy=(x[0], y[0]), xycoords='data',
         xytext=(+3, +1), textcoords='offset points', fontsize=8)

ax.annotate('Variant 1',
         xy=(x[1],y[1]), xycoords='data',
         xytext=(+3, +1), textcoords='offset points', fontsize=8)



#Peak performance line and text
ax.axhline(y=PEAK_PERF, linewidth=1, color='black')
ax.text(X_MAX/10.0, PEAK_PERF+0.1, "Peak Performance ("+str(PEAK_PERF)+" F/C)", fontsize=8)


#BW line and text
x = np.linspace(X_MIN, X_MAX, X_MAX)
y = x*PEAK_BW 
ax.plot(x, y, linewidth=1, color='black')


l2 = array((0.01,0.01))
angle = 45*(ASPECT_RATIO+0.1)
trans_angle = gca().transData.transform_angles(array((angle,)),
                                               l2.reshape((1,2)))[0]

ax.text(X_MIN,X_MIN*PEAK_BW+0.1,'MemLoad ('+str(PEAK_BW)+' B/C)',fontsize=8,
           rotation=trans_angle)


#save file
fig.savefig(OUTPUT_FILE, dpi=250,  bbox_inches='tight')
