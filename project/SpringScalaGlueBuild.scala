import sbt._
import sbt.Keys._

object SpringScalaGlueBuild extends Build {

	val spring = "org.springframework"
	val springVersion = "3.2.0.RELEASE"

  lazy val springScalaGlue = Project(
    id = "spring-scala-glue",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "spring-scala-glue",
      organization := "uk.ac.warwick",
      version := "1.2",
      scalaVersion := "2.10.0",
      libraryDependencies ++= Seq(
      	spring % "spring-beans" % springVersion,
        spring % "spring-context" % springVersion,

        "org.scalatest" %% "scalatest" % "2.0.M6-SNAP8" % "test"
      )
    )
  )
}
