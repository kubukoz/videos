val compilerPlugins = List(
  compilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val commonSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions ++= Options.all,
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "dev.profunktor" %% "console4cats" % "0.8.0"
  ) ++ compilerPlugins
)

val traverse =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
