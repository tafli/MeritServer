name := """MeritServer"""
organization := "tafli"
scalaVersion := "2.12.1"

version := "0.1.0"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaVersion      = "2.4.16"
  val akkaHttpVersion  = "10.0.1"
  val scalaTestVersion = "3.0.1"

  Seq(
    "com.typesafe.akka" %% "akka-actor"           % akkaVersion,
    "com.typesafe.akka" %% "akka-agent"           % akkaVersion,
    "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion,
    "org.scalatest"     %% "scalatest"            % scalaTestVersion % "test"
  )
}

parallelExecution in Test := false

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerUpdateLatest := true
