import sbt._
import sbt.Keys._

object SpringScalaGlueBuild extends Build {

	val spring = "org.springframework"
	val springVersion = "3.1.2.RELEASE"

  lazy val springScalaGlue = Project(
    id = "spring-scala-glue",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "Spring Scala Glue",
      organization := "uk.ac.warwick",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      libraryDependencies ++= Seq(
      	spring % "spring-beans" % springVersion,
        spring % "spring-context" % springVersion,

        "org.scalatest" %% "scalatest" % "1.6.1" % "test"
      )
    )
  )
}
