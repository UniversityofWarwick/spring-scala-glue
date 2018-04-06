name := "spring-scala-glue"
organization := "uk.ac.warwick"
version := "1.7"
scalaVersion := "2.12.5"
crossScalaVersions := Seq("2.11.11", "2.12.5")

val spring = "org.springframework"
val springVersion = "4.3.0.RELEASE"

val WarwickSnapshots: MavenRepository = "Nexus Snapshots" at "https://mvn.elab.warwick.ac.uk/nexus/content/repositories/snapshots"
val WarwickReleases: MavenRepository = "Nexus Releases" at "https://mvn.elab.warwick.ac.uk/nexus/content/repositories/releases"

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

      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % "test"
    ),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishTo := {
      if (isSnapshot.value) Some(WarwickSnapshots)
      else Some(WarwickReleases)
    }
  )
