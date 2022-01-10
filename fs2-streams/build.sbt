def crossPlugin(x: sbt.librarymanagement.ModuleID) =
  compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.13.2"),
)

val commonSettings = Seq(
  scalaVersion := "2.13.7",
  scalacOptions --= Seq(
    "-Xfatal-warnings", //because I really dislike this flag in development
    "-Wunused:imports" //because we'll start with unused imports
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "co.fs2" %% "fs2-core" % "2.2.2",
    "dev.profunktor" %% "console4cats" % "0.8.1"
  ) ++ compilerPlugins
)

val fs2Streams =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
