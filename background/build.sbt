def crossPlugin(x: sbt.librarymanagement.ModuleID) =
  compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.13.0"),
  crossPlugin("com.kubukoz" % "better-tostring" % "0.3.2"),
)

val commonSettings = Seq(
  scalaVersion := "2.13.6",
  scalacOptions --= Seq(
    "-Xfatal-warnings"
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  fork in Test := true,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.1.1",
    "org.http4s" %% "http4s-blaze-server" % "0.23.0-RC1",
  ) ++ compilerPlugins,
)

val root =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
