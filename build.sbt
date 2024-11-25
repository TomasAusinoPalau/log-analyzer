import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"

Test / parallelExecution := false


lazy val root = (project in file("."))
  .settings(
    name := "log-analyzer",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.scalamock" %% "scalamock" % "5.2.0" % Test
    )

  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
