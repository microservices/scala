enablePlugins(JavaAppPackaging)

name := "oms-scala-template"
organization := "microservices"
version := "0.1"
scalaVersion := "2.11.12"
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
mainClass in Compile := Some("MyApp")

libraryDependencies ++= {
  val akkaHttpVersion = "10.0.10"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  )
}
