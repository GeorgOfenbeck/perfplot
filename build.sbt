name := "PerfPlot"

version := "1.0"

organization := "ETHZ"

resolvers += ScalaToolsSnapshots

//scalaVersion := ScalaVersion

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test-src")

//libraryDependencies += "org.scala-lang" % "scala-actors" % ScalaVersion // for ScalaTest

//libraryDependencies += scalaTest

// tests are not thread safe
parallelExecution in Test := false

// disable publishing of main docs
publishArtifact in (Compile, packageDoc) := false

// continuations
autoCompilerPlugins := true

fork in run := true
