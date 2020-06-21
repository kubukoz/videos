def crossPlugin(x: sbt.librarymanagement.ModuleID) =
  compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.0"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.3"),
  crossPlugin("com.kubukoz" % "better-tostring" % "0.2.2")
)

val commonSettings = Seq(
  scalaVersion := "2.13.2",
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
    "org.http4s" %% "http4s-blaze-server" % "0.21.4",
    "dev.zio" %% "zio" % "1.0.0-RC21",
    "dev.zio" %% "zio-interop-cats" % "2.1.3.0-RC16",
    "dev.profunktor" %% "console4cats" % "0.8.1"
  ) ++ compilerPlugins
)

val background =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
