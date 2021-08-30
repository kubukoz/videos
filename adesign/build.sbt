val compilerPlugins = List(
  compilerPlugin("com.kubukoz" %% "better-tostring" % "0.3.5" cross CrossVersion.full)
)

val commonSettings = Seq(
  scalaVersion := "3.0.1",
  fork in Test := true,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.6.1",
    "org.typelevel" %% "cats-effect" % "3.2.5",
  ) ++ compilerPlugins,
  scalacOptions -= "-Xfatal-warnings",
)

val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true)
