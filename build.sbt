name := "spring-scala-glue"
organization := "uk.ac.warwick"
version := "1.8-SNAPSHOT"
scalaVersion := "2.13.0-M5"
crossScalaVersions := Seq("2.11.12", "2.12.8", "2.13.0-M5")

val spring = "org.springframework"
val springVersion = "4.3.0.RELEASE"

val WarwickSnapshots: MavenRepository = "Nexus Snapshots" at "https://mvn.elab.warwick.ac.uk/nexus/repository/public-snapshots"
val WarwickReleases: MavenRepository = "Nexus Releases" at "https://mvn.elab.warwick.ac.uk/nexus/repository/public-releases"

val repositorySettings = Seq(
  credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
  publishTo := {
    if (isSnapshot.value) Some(WarwickSnapshots)
    else Some(WarwickReleases)
  }
)

lazy val root = (project in file("."))
  .settings(
    libraryDependencies ++= Seq(
      spring % "spring-beans" % springVersion,
      spring % "spring-context" % springVersion,

      "org.scalatest" %% "scalatest" % "3.0.6-SNAP6" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % "test"
    ),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo := {
      if (isSnapshot.value) Some(WarwickSnapshots)
      else Some(WarwickReleases)
    },
    // Fix publishing on SBT 1.x
    // https://github.com/sbt/sbt/issues/3570
    updateOptions := updateOptions.value.withGigahorse(false)
  )
