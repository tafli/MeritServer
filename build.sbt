name          := """MeritServer"""
organization  := "tafli"
version       := "0.0.1"
scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion       = "2.4.3"
  val akkaHttpVersion   = "10.0.1"
  val scalaTestVersion  = "3.0.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "org.scalatest"     %% "scalatest" % scalaTestVersion % "test"
  )
}

enablePlugins(DockerPlugin)

parallelExecution in Test := false