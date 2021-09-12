val compilerPlugins = List(
  compilerPlugin("com.kubukoz" %% "better-tostring" % "0.3.5" cross CrossVersion.full)
)

val commonSettings = Seq(
  scalaVersion := "2.13.6",
  fork in Test := true,
  libraryDependencies ++= compilerPlugins,
  scalacOptions -= "-Xfatal-warnings",
)

val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true)
