ThisBuild / scalaVersion := "3.4.2"
ThisBuild / organization := "com.example"

val interface = project

val printerPlugin = project
  .dependsOn(interface)
  .settings(name := "printer-plugin")

val app = project
  .dependsOn(interface)
  .settings(
    fork := true,
    libraryDependencies ++= Seq(
      "io.get-coursier" % "coursier_2.13" % "2.1.10"
    ),
  )

val root = project
  .in(file("."))
  .aggregate(interface, printerPlugin, app)
  .settings(
    publish / skip := true
  )
