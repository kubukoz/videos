def crossPlugin(x: sbt.librarymanagement.ModuleID) =
  compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin(
    "com.olegpy" %% "better-monadic-for" % "0.3.1"
  ),
  crossPlugin(
    "org.typelevel" % "kind-projector" % "0.11.0"
  ),
  crossPlugin(
    "com.github.cb372" % "scala-typed-holes" % "0.1.5"
  ),
  crossPlugin("com.kubukoz" % "better-tostring" % "0.2.4")
)

val commonSettings = Seq(
  scalaVersion := "2.13.3",
  scalacOptions --= Seq(
    "-Xfatal-warnings" //because I really dislike this flag in development
  ),
  scalacOptions ++= Seq(
    "-Ymacro-annotations"
  ),
  fork in Test := true,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "2.2.0",
    "org.typelevel" %% "cats-laws" % "2.2.0",
    "org.http4s" %% "http4s-client" % "0.21.7",
    "org.http4s" %% "http4s-dsl" % "0.21.7",
    "org.http4s" %% "http4s-circe" % "0.21.7",
    "io.circe" %% "circe-generic" % "0.13.0",
    "dev.profunktor" %% "console4cats" % "0.8.1"
  ) ++ compilerPlugins
)

val parallel =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true)
