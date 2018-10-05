name := "es-utility"

version := "0.1"

scalaVersion := "2.12.6"


resolvers += "elasticsearch-releases" at "https://artifacts.elastic.co/maven"

libraryDependencies ++= Seq(
 "org.apache.logging.log4j" % "log4j-to-slf4j" % "2.11.1",
  "org.slf4j" % "slf4j-api" % "1.7.24",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "org.elasticsearch.client" % "x-pack-transport" % "6.3.1",
  "com.unboundid" % "unboundid-ldapsdk" % "3.2.0",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.58",
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.58",
  "com.typesafe" % "config" % "1.3.2",
  "com.amazonaws" % "aws-java-sdk" % "1.11.385",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)
