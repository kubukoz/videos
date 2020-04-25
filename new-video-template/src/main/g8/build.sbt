def crossPlugin(x: sbt.librarymanagement.ModuleID) =
  compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.0"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.3"),
  crossPlugin("com.kubukoz" % "better-tostring" % "0.2.0")
)

val commonSettings = Seq(
  scalaVersion := "2.13.1",
  scalacOptions --= Seq(
    "-Xfatal-warnings" //because I really dislike this flag in development
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "2.1.3",
    "dev.profunktor" %% "console4cats" % "0.8.1"
  ) ++ compilerPlugins
)

val $name;format="camel"$ =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
