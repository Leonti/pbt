ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

scalacOptions += "-Ypartial-unification"

lazy val root = (project in file("."))
  .settings(
    name := "pbt",
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5" % Test,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
