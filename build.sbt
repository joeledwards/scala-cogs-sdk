name := """cogs-sdk"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.5.10"
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.5.10"
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.16"
libraryDependencies += "com.typesafe.akka" % "akka-stream_2.11" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.7"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.22"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.7"
libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
