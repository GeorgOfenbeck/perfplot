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

  val pointTypes = List( 5, 7, 9, 11, 13 )
  val lineColors = List( "black", "red", "green", "blue", "#FFFF00" )


  def plot (plot: OpsPlot)
  {
    var first : Boolean = true
    //first generate the .data files
    val outputFile = new PrintStream(plot.outputName + ".data")

    //calculate the minimum
    val all = plot.series.map(singles => singles.series.map (point => point.getOperations).toList)

    val minimums : List[Double] = all.transpose.map(x => x.min)
    for (serie <- plot.series)
    {
      val i = plot.series.indexOf(serie)
      outputFile.printf("# [Median %d]\n", int2Integer(i));
      for (point <- serie.series)
      {
        val j = serie.series.indexOf(point)
        outputFile.printf("%e %e\n",
          double2Double(point.problemSize),
          double2Double(point.getOperations/ minimums(j))
          )
        //point.point._2.value,
        //point.point._1.value)
      }
      outputFile.printf("\n\n");
    }
    outputFile.close()

    val output = new PrintStream(plot.outputName + ".gnuplot")
    preparePlot2D(output,plot)
    //output.printf("set xtics scale 0 (%s)\n",getLo)
    val plotLines = scala.collection.mutable.MutableList[String]()

    for (mserie <- plot.series)
    {
      val i = plot.series.indexOf(mserie)
      plotLines += String.format("'%s.data' index '[Median %d]' title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
        plot.outputName,
        int2Integer(i),
        mserie.name,
        int2Integer(lwLine),
        int2Integer(pointTypes(i)),
        lineColors(i)
      )

    }


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

  def plot (plot: RooflinePlot)
  {
    //first generate the .data files
    val outputFile = new PrintStream(plot.outputName + ".data")
    for (serie <- plot.series)
    {
      val i = plot.series.indexOf(serie)
      outputFile.printf("# [Median %d]\n", int2Integer(i));
      for (point <- serie.series)
      {
        outputFile.printf("%e %e\n",
          double2Double(point.getMedianIntensity),
          double2Double(point.getMedianPerformance))
          //point.point._2.value,
          //point.point._1.value)
      }
      outputFile.printf("\n\n");
    }

    outputFile.close()





    val output = new PrintStream(plot.outputName + ".gnuplot")
    preparePlot2D(output,plot)

    //output.printf("set xtics scale 0 (%s)\n",getLo)



    val plotLines = scala.collection.mutable.MutableList[String]()
      //----------------------------------------------------------------------------------------------
      //build the peak performance lines

    var first : Boolean = true
    for ((name,peak) <- plot.peakPerformances)
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
    for ((name,peak) <- plot.peakBandwidths)
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
        "%e*x notitle with lines lc %s lw %d",
        double2Double(peak.value), lineColor, int2Integer(lw))
    }



  //print the peak performance lines
    for ((name,peak) <- plot.peakPerformances)
      output.printf(
        "set label '%s (%.2g F/C)' at graph 1,first %e right offset -1,graph 0.015\n",
        name, double2Double(peak.value), double2Double(peak.value))

    val performance = 30


    val aspectRatio = (plotHeight/plotWidth) * (tMargin-bMargin)/(rMargin-lMargin)
    val opIntensityRatio = plot.xMax / plot.xMin
    val performanceRatio = plot.yMax / plot.yMin
    val angle = Math.toDegrees(Math.atan(aspectRatio * Math.log(opIntensityRatio)/ Math.log(performanceRatio)))


    //print the peak performance lines
    for ((name,peak) <- plot.peakBandwidths)
    {
      val performance = 48*0.85
      val bandwidth = peak.value
      val opIntens = 20.0


      val borderOpIntens = performance/bandwidth
      val borderPerformance = opIntens * bandwidth

      if (borderOpIntens < opIntens) {
        // we hit the top of the graph
        output.printf(
          "set label '%s (%.2g B/C)' at first %e , first %e right offset 0,graph 0.02 rotate by %e\n",
          name, double2Double(bandwidth), double2Double(borderOpIntens),
          double2Double(performance), double2Double(angle))
      }
      else {
        // we hit the right of the graph
        output.printf(
          "set label '%s (%.2g byte/cycle)' at graph 1, first %e right offset 0,graph -0.015 rotate by %e\n",
          name, double2Double(bandwidth), double2Double(borderPerformance), double2Double(angle))
      }
    }

    for (mserie <- plot.series)
    {
      val i = plot.series.indexOf(mserie)
      plotLines += String.format("'%s.data' index '[Median %d]' title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
        plot.outputName,
        int2Integer(i),
        mserie.name,
        int2Integer(lwLine),
        int2Integer(pointTypes(i)),
        lineColors(i)
      )

      // add label for the first and the last point
      output.printf(
        "set label \"%s\" at first %g,%g center nopoint offset graph 0,0.02 front\n",
        mserie.series.head.problemSize.toString,
        double2Double(mserie.series.head.getMedianIntensity),
        double2Double(mserie.series.head.getMedianPerformance))
      output.printf(
        "set label \"%s\" at first %g,%g center nopoint offset graph 0,0.02 front\n",
        mserie.series.last.problemSize.toString,
        double2Double(mserie.series.last.getMedianIntensity),
        double2Double(mserie.series.last.getMedianPerformance))

    }


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
   * plot a performance
   */
  def plot (plot : PerformancePlot) {
    var first : Boolean = true
    //first generate the .data files
    val outputFile = new PrintStream(plot.outputName + ".data")
    for (serie <- plot.series)
    {
      val i = plot.series.indexOf(serie)
      outputFile.printf("# [Median %d]\n", int2Integer(i));
      for (point <- serie.series)
      {
        outputFile.printf("%e %e\n",
          double2Double(point.problemSize),
          double2Double(point.getAVG)
        )
        //point.point._2.value,
        //point.point._1.value)
      }
      outputFile.printf("\n\n");
    }
    outputFile.close()

    val output = new PrintStream(plot.outputName + ".gnuplot")
    preparePlot2D(output,plot)
    //output.printf("set xtics scale 0 (%s)\n",getLo)
    val plotLines = scala.collection.mutable.MutableList[String]()

    first = true
    for ((name,peak) <- plot.peakPerformances)
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

    for (mserie <- plot.series)
    {
      val i = plot.series.indexOf(mserie)
      plotLines += String.format("'%s.data' index '[Median %d]' title '%s' with linespoints lw %d lt -1 pt %d lc rgb\"%s\"",
        plot.outputName,
        int2Integer(i),
        mserie.name,
        int2Integer(lwLine),
        int2Integer(pointTypes(i)),
        lineColors(i)
      )

    }


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
