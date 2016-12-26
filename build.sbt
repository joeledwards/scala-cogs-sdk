name := """cogs-sdk"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % "2.5.10"
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.5.10"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"
