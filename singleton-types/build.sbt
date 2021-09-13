val compilerPlugins = List(
  compilerPlugin("com.kubukoz" %% "better-tostring" % "0.3.5" cross CrossVersion.full)
)

val commonSettings = Seq(
  scalaVersion := "2.13.6",
  fork in Test := true,
  libraryDependencies ++= compilerPlugins,
  scalacOptions -= "-Xfatal-warnings",
)

val scala3 = project
  .settings(commonSettings)
  .settings(scalaVersion := "3.0.1")

val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      libraryDependencies ++= List(
        "com.chuusai" %% "shapeless" % "2.3.7",
        "eu.timepit" %% "singleton-ops" % "0.5.2",
      ),
      skip in publish := true,
    )
