val compilerPlugins = List(
  compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.17" cross CrossVersion.full)
)

val commonSettings = Seq(
  scalaVersion := "3.7.2",
  fork in Test := true,
  libraryDependencies ++= compilerPlugins,
  scalacOptions -= "-Xfatal-warnings",
)

val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true)
