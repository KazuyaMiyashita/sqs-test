import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.2"

  lazy val typesafeConfig = "com.typesafe" % "config" % "1.4.1"

  val AkkaVersion     = "2.6.8"
  val AkkaHttpVersion = "10.2.0"
  lazy val akkaDependencies = Seq(
    "com.typesafe.akka"  %% "akka-actor-typed"       % AkkaVersion,
    "com.typesafe.akka"  %% "akka-stream"            % AkkaVersion,
    "com.typesafe.akka"  %% "akka-stream-typed"      % AkkaVersion,
    "com.typesafe.akka"  %% "akka-http"              % AkkaHttpVersion,
    "com.typesafe.akka"  %% "akka-http-xml"          % AkkaHttpVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "2.0.2",
    "ch.qos.logback"     % "logback-classic"         % "1.2.3"
  )

  lazy val dbDependencies = Seq(
    "org.typelevel"           %% "cats-core"           % "2.2.0",
    "org.typelevel"           %% "cats-effect"         % "2.2.0",
    "org.playframework.anorm" %% "anorm"               % "2.6.7",
    "mysql"                   % "mysql-connector-java" % "8.0.22",
    typesafeConfig
  )

  val CirceVersion = "0.12.3"
  lazy val circeDependencies = Seq(
    "io.circe" %% "circe-core"    % CirceVersion,
    "io.circe" %% "circe-generic" % CirceVersion,
    "io.circe" %% "circe-parser"  % CirceVersion
  )

  lazy val awsSdk = "software.amazon.awssdk" % "aws-sdk-java" % "2.15.32"

}
