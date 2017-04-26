name := "logoon"

version := "0.0.0"

scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "org.scalatest"            %% "scalatest"       % "3.0.1" % Test,
  "org.apache.logging.log4j" %  "log4j-api"       % "2.8.2" % Test,
  "org.apache.logging.log4j" %  "log4j-core"      % "2.8.2" % Test,
  "ch.qos.logback"           %  "logback-classic" % "1.2.3" % Test
)

parallelExecution in Test := false