val compilerPlugins = List(
  compilerPlugin("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full),
  compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8"),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

val commonSettings = Seq(
  scalaVersion := "2.12.8",
  scalacOptions ++= Options.all,
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "1.4.0",
    "co.fs2" %% "fs2-io" % "1.0.5",
    "dev.profunktor" %% "console4cats" % "0.7.0",
    "org.scalatest" %% "scalatest" % "3.0.8" % Test
  ) ++ compilerPlugins
)

val resource =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
