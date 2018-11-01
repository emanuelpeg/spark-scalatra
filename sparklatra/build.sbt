val ScalatraVersion = "2.6.3"

organization := "com.hexacta"

name := "Sparklatra"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

resolvers += Classpaths.typesafeReleases
resolvers += "Cloudera Repository" at "https://repository.cloudera.com/content/repositories/releases/"

libraryDependencies ++= Seq(
  "org.scalatra" %% "scalatra" % ScalatraVersion  ,
  "org.scalatra" %% "scalatra-scalatest" % ScalatraVersion % "test" ,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime" ,
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.9.v20180320" % "container" ,
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"  ,
  "com.sun.jersey" % "jersey-core" % "1.19.4" ,
  "com.sun.jersey" % "jersey-server" % "1.19.4" ,
  "org.apache.spark" % "spark-core_2.11" % "2.3.1",
 // "org.apache.hbase" % "hbase-spark" % "2.0.0-alpha4" exclude("com.fasterxml.jackson.module", "jackson-module-scala_2.10"),
  "org.apache.hbase" % "hbase-common" % "2.1.0" exclude("com.fasterxml.jackson.core", "jackson-databind"),
  "org.apache.hbase" % "hbase-client" % "2.1.0" exclude("com.fasterxml.jackson.core", "jackson-databind")
)

enablePlugins(SbtTwirl)
enablePlugins(ScalatraPlugin)