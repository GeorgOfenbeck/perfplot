#General imports:

import sys
import os
import math
from matplotlib import rc
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
#matplotlib.rc('font', family='sans-serif')
matplotlib.rc('font',**{'family':'sans-serif','sans-serif':['Helvetica']})
## for Palatino and other serif fonts use:
#rc('font',**{'family':'serif','serif':['Palatino']})
matplotlib.rc('text', usetex=True)


X_MIN=0.1
X_MAX=1024
Y_MIN=0
Y_MAX=2
LOG_X=1
LOG_Y=0
TITLE="TITLE"
X_LABEL="Problem Size [Complex Doubles]"
Y_LABEL="Performance [Flop/Cycle]"
OUTPUT_FILE="performancePlot.pdf"
PLOT_STATS=1
colors=['black', '#000066','#336600','#CC0033' ,'#FFFF00' ]
series = ['MKL', 'MMM']


fig = plt.figure()
ax = fig.add_subplot(111)

#TODO: fix the font familiy of the tick labels. Everythong I have tried so far does not work...
#ffont = {'family':'sans-serif','fontsize':10,'weight':'bold'}
#ax.set_xticklabels(ax.get_xticks(),ffont)
#ax.set_yticklabels(ax.get_yticks(),ffont)


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
if LOG_X:
	ax.xaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
	#ax.xaxis.major.formatter.set_powerlimits((-3, 6)) 
#	for label in ax.get_xticklabels() :
#		label.set_fontproperties(ticks_font)

if LOG_Y:
        ax.yaxis.set_major_formatter(matplotlib.ticker.ScalarFormatter())
        #ax.yaxis.major.formatter.set_powerlimits((-3, 6)) 
#        for label in ax.get_yticklabels() :
#                label.set_fontproperties(ticks_font)






# Load the data 

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



	yData =[]
	for f,c in zip(nFLOPS,nCycles):
		yData.append([float(vf)/float(vc) for vf, vc in zip(f,c) if vf != '' and vc != ''])



	xData = []
	file_in = open('size_'+serie+'.txt','r')
	lines = file_in.readlines()
	for line in lines:
        	split_line = line.rstrip('\n').split(' ')
        	xData.append(split_line)

	file_in.close()

	xData = [float(x) for x in xData[0]]

	if PLOT_STATS:	
		"""
		Percentile boxes
		On each box, the central mark is the median, the edges of the box are the lower hinge (defined as the 25th percentile
		) and the upper hinge (the 75th percentile), the whiskers extend to the most extreme data points not considered outliers,
	 	this ones are plotted individually.	
		"""
		locs, labels = xticks() 
		if LOG_X:
			rectanglesWidths = [float(x)/3 for x in xData]
			bp = ax.boxplot(yData, positions=xData, widths = rectanglesWidths)
		else:
			bp = ax.boxplot(yData, positions=xData)
		setp(bp['medians'], color='none')
		setp(bp['fliers'], color=colors[i],marker='None')
		setp(bp['whiskers'], color=colors[i], linestyle= '-')
		setp(bp['boxes'], color=colors[i])
		setp(bp['caps'], color=colors[i])

		#Restore xticks location --> Needed to locate boxes in arbitrary positions
		xticks(locs)


		# Plot a line between the medians of each dataset => need to get medians calculated by boxplot
		medians = range(len(xData))
		for j in range(len(xData)):
			med = bp['medians'][j]
			medianX = []
  			medianY = []
  			for k in range(2):
    	  			medianX.append(med.get_xdata()[k])
      				medianY.append(med.get_ydata()[k])
      				medians[j] = medianY[0]	


		ax.plot(xData, medians, '-', lw=1,color=colors[i], marker='o', markeredgecolor=colors[i], markersize=4, label=serie)

	else:
		ax.plot(xData, yData, '-', lw=1,color=colors[i], marker='o', markeredgecolor=colors[i], markersize=4, label=serie)	
	
	# end of if-else

#end for loop


ax.legend(numpoints=1, loc='best',fontsize =6,frameon = False )

#save file
fig.savefig(OUTPUT_FILE, dpi=250,  bbox_inches='tight')
