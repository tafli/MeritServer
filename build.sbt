name := """MeritServer"""
organization := "tafli"
scalaVersion := "2.12.1"

version := "0.3.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion         = "2.4.16"
  val akkaHttpVersion     = "10.0.1"
  val akkaHttpCorsVersion = "0.1.10"
  val scalaTestVersion    = "3.0.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka" %% "akka-agent"           % akkaVersion,
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
    "ch.megard"         %% "akka-http-cors"       % akkaHttpCorsVersion,
    "org.scalatest"     %% "scalatest"            % scalaTestVersion % "test"
  )
}

parallelExecution in Test := false

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerUpdateLatest := true
