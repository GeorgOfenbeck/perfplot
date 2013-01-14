/**
 * Georg Ofenbeck
 First created:
 * Date: 17/12/12
 * Time: 09:59
 */

import org.scalatest.Suite

import roofline.plot._
import roofline.services._

class TestPlotting extends Suite{


  def test_RooflinePlot()
  {
    import roofline.quantities._
    val myplot = new RooflinePlot(
      List(
        ("first",Performance(flops(8),Cycles(1))),
        ("second",Performance(flops(2),Cycles(1)))
      ),
        List(
        ("firstB",  Throughput(TransferredBytes(10), Cycles(1))),
        ("secondB", Throughput(TransferredBytes(5), Cycles(1)))
        )

    )
    myplot.outputName = "TestRooflinePlot"
    myplot.title = "Title"
    myplot.xLabel = "xLabel"
    myplot.yLabel = "yLabel"

    myplot.xUnit = "xUnit"
    myplot.yUnit = "yUnit"


    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(myplot)
  }
/*
  def test_SimplePlot()
  {
    val myplot = new SimplePlot

    myplot.outputName = "TestSimplePlot"
    myplot.title = "Title"
    myplot.xLabel = "xLabel"
    myplot.yLabel = "yLabel"

    myplot.xUnit = "xUnit"
    myplot.yUnit = "yUnit"

    myplot(3.0)

    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(myplot)

  }



  def test_PerformancePlot()
  {
    import roofline.quantities._
    val myplot = new PerformancePlot(List(),List(
          ("first",Performance(flops(8),Cycles(1))),
          ("second",Performance(flops(2),Cycles(1)))


    ))
    myplot.outputName = "TestPerformancePlot"
    myplot.title = "Title"
    myplot.xLabel = "xLabel"
    myplot.yLabel = "yLabel"

    myplot.xUnit = "xUnit"
    myplot.yUnit = "yUnit"


    val ps = new PlotService
    System.out.println("Calling plot")
    ps.plot(myplot)
  }     */
}
