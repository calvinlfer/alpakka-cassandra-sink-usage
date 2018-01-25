name := "alpakka-cassandra-runthrough"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka"   %% "akka-stream"                    % "2.5.9",
  "com.typesafe.akka"   %% "akka-slf4j"                     % "2.5.9",
  "com.lightbend.akka"  %% "akka-stream-alpakka-cassandra"  % "0.16",
  "ch.qos.logback"       % "logback-classic"                % "1.2.3"
)