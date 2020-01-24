def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.0"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.1")
)

val commonSettings = Seq(
  scalaVersion := "2.13.1",
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings") ++ Seq(
    "-Ymacro-annotations"
  )),
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "pprint" % "0.5.6",
    "co.fs2" %% "fs2-core" % "2.2.1",
    "dev.profunktor" %% "console4cats" % "0.8.1"
  ) ++ compilerPlugins
)

val fs2Streams =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
