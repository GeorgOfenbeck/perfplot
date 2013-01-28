name := "PerfPlot"

version := "1.1"

organization := "ETHZ"

resolvers += ScalaToolsSnapshots

scalaVersion := "2.10.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test-src")

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"
//libraryDependencies += "org.scalatest" %% "scalatest" % "1.6.1" % "test"
//libraryDependencies += "org.scala-lang" % "scala-actors" % ScalaVersion // for ScalaTest

//libraryDependencies += scalaTest

// tests are not thread safe
parallelExecution in Test := false

// disable publishing of main docs
publishArtifact in (Compile, packageDoc) := false

// continuations
autoCompilerPlugins := true

fork in run := true
