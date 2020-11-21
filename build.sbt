import Dependencies._

ThisBuild / scalaVersion := "2.13.3"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val commonSettings = Seq(
  scalacOptions ++= "-deprecation" :: "-feature" :: "-Xlint" :: Nil,
  scalacOptions in (Compile, console) ~= { _.filterNot(_ == "-Xlint") },
  scalafmtOnCompile := true
)

lazy val root = (project in file("."))
  .settings(
    name := "sqs-test",
    libraryDependencies += scalaTest % Test
  )

lazy val api = (project in file("api"))
  .settings(
    name := "api",
    libraryDependencies += awsSdk,
    libraryDependencies ++= circeDependencies,
    libraryDependencies ++= akkaDependencies,
    libraryDependencies += scalaTest % Test
  )
  .dependsOn(db)

lazy val db = (project in file("db"))
  .settings(
    name := "db",
    libraryDependencies ++= dbDependencies,
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
