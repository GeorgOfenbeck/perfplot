import sbt._
import java.io.File

object PerfPlotBuild extends Build {
  
  val ScalaVersion = "2.10.0-RC5"

  lazy val PerfPlot = Project("PerfPlot", file("."))


}

