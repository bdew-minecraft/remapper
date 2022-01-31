ThisBuild / version := "1.0"
ThisBuild / organization := "net.bdew"
ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "remapper",
    assembly / mainClass := Some("net.bdew.remapper.Main"),
  )

libraryDependencies += "org.scalameta" %% "scalameta" % "4.4.33"