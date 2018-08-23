val ScalatraVersion = "2.6.3"

organization := "com.hexacta"

name := "Sparklatra"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.0"

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion  ,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test" ,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime" ,
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container" ,
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"  ,
  "com.sun.jersey" % "jersey-core" % "1.19.4" ,
  "com.sun.jersey" % "jersey-server" % "1.19.4" ,
  "org.apache.spark" %% "spark-core" % "2.3.1"
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)