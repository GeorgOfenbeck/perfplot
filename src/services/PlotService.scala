/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59
 */
package roofline
package services


import java.io.PrintStream
import roofline.plot._

class PlotService {

  val plotWidth = 14.337
  val plotHeight = plotWidth * 16.0 / 28.0

  val bMargin = 0.1
  val tMargin = 0.93
  val lMargin = 0.12
  val rMargin = 0.95

  val lwTic = 2
  val lwMTic = 1
  val lwMaxBound = 2
  val lwBound = 2
  val lwLine = 2

  val pointTypes = ( 5, 7, 9, 11, 13 )
  val lineColors = ( "black", "red", "green", "blue", "#FFFF00" )


  def plot (plot: SimplePlot) {
    if (plot.values.isEmpty)
      return

    val outputfile = new PrintStream(plot.outputName + ".data")
    for (value <- plot.values)
      outputfile.printf("%e\n",double2Double(value))
    outputfile.close()

    //write gnuplot files
    {
      val output = new PrintStream(plot.outputName + ".gnuplot")
      preparePlot2D(output,plot)
      output.printf("plot '%s.data' notitle with linespoints \n", plot.outputName)
      output.close()
    }
    val arg  =  plot.outputName + ".gnuplot"
    CommandService.rungnuplot(arg)
  }



  def preparePlot(output : PrintStream, plot : Plot)
  {
    //set the ouput
    output.printf("set terminal pdf color size %ecm,%ecm font 'Gill Sans, 4'\n",  double2Double(plotWidth), double2Double(plotHeight))
    output.printf("set output '%s.pdf'\n", plot.outputName)
    // set the title
    output.printf("set title '%s' font 'Gill Sans, 8'\n", plot.title)

    // disable border
    output.println("unset border")

    // set the point size
    output.println("set pointsize 0.25")

    // add gray background
    output.println("set object 1 rectangle from graph 0,0 to graph 1,1 behind fillcolor rgb\"#E0E0E0\" lw 0")

    // add white grid
    output.printf(
      "set grid xtics ytics mxtics mytics lt -1 lw %d linecolor rgb\"#FFFFFF\",lt -1 lw %d linecolor rgb\"#FFFFFF\"\n",
      int2Integer(lwTic), int2Integer(lwMTic))

    // set the margins
    output.printf("set bmargin at screen %e\n", double2Double(bMargin))
    output.printf("set tmargin at screen %e\n", double2Double(tMargin))
    output.printf("set lmargin at screen %e\n", double2Double(lMargin))
    output.printf("set rmargin at screen %e\n", double2Double(rMargin))
  }

  def preparePlot2D(output : PrintStream, plot: Plot2D)
  {
    preparePlot(output, plot)

    //place the legend
    val keyposition = plot.keyPosition

    //TODO: Configuration

    keyposition match {
      case KeyPosition.BottomLeft => 		output.println("set key left bottom")
      case KeyPosition.BottomRight =>   output.println("set key right bottom")
      case KeyPosition.TopLeft =>       output.println("set key left top")
      case KeyPosition.TopRight =>      output.println("set key right top")
      case KeyPosition.NoKey =>         output.println("unset key")
    }

    //set logarithmic axes
    if (plot.logX) output.println("set log x")
    if (plot.logY) output.println("set log y")

    //set the scaling
    //TODO: range from System Service





    output.printf("set xrange [%s:%s]\n",
      if (plot.xMin.isInfinite) "" else plot.xMin.toString,
      if (plot.xMax.isInfinite) "" else plot.xMax.toString)
    output.printf("set yrange [%s:%s]\n",
      if (plot.yMin.isInfinite) "" else plot.yMin.toString,
      if (plot.yMax.isInfinite) "" else plot.yMax.toString)


    output.printf("set xlabel '%s [%s]' font 'Gill Sans, 6'\n",
      plot.xLabel, plot.xUnit)
    output.printf("set ylabel '%s [%s]' font 'Gill Sans, 6'\n",
      plot.yLabel, plot.yUnit)

  }

  /**
   * plot a roofline plot
   */
  def plot (plot : PerformancePlot) {
    //print data file
    val outputFile = new PrintStream(plot.outputName + ".data")


    {
      var i = 0
      for (serie <- plot.series)
      {
        outputFile.printf("# [Median %d]\n", int2Integer(i))
        for (point <- serie.points)
        {

        }
      }

    }
    outputFile.close()

    //write gnuplot file
    val output = new PrintStream(plot.outputName + ".gnuplot")
    preparePlot2D(output, plot)


    val plotLines = scala.collection.mutable.MutableList[String]()

    //build the peak performance lines
    var first : Boolean = true
    for ((name,peak) <- plot.peakperformances)
    {
      val (lineColor, lw) = if (first)
      {
        first = false
        // set the color of the first line
        ("rgb\"black\"",lwMaxBound)
      }
      else
        ("rgb\"#B0B0B0\"",lwBound)

      // generate the string

      plotLines += String.format(
        "%e notitle with lines lc %s lw %d",
        double2Double(peak.value), lineColor, int2Integer(lw))
    }

    //print the peak performance lines
    for ((name,peak) <- plot.peakperformances)
      output.printf(
        "set label '%s (%.2g F/C)' at graph 1,first %e right offset -1,graph 0.015\n",
          name, double2Double(peak.value), double2Double(peak.value))



    output.println("plot \\")
    first = true
    for (line <- plotLines)
    {
      if (first) first = false else output.print(",\\\n")
      output.print(line) // + )
    }
    output.close()


    val arg  =  plot.outputName + ".gnuplot"
    CommandService.rungnuplot(arg)
  }




}
