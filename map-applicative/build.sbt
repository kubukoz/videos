val compilerPlugins = List(
  compilerPlugin(("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full)),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val commonSettings = Seq(
  scalaVersion := "2.12.10",
  scalacOptions ++= Options.all,
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.6.0",
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "org.typelevel" %% "cats-testkit" % "2.0.0" % Test,
    "dev.profunktor" %% "console4cats" % "0.8.0"
  ) ++ compilerPlugins
)

val mapapplicative =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
