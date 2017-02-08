name := """cogs-sdk"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.5.10"
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.5.10"
libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.16"
libraryDependencies += "com.typesafe.akka" % "akka-stream_2.11" % "2.4.16"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.7"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
