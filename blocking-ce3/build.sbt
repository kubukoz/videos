val compilerPlugins = List(
  compilerPlugin("org.typelevel" % "kind-projector" % "0.13.0" cross CrossVersion.full),
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  compilerPlugin("com.kubukoz" %% "better-tostring" % "0.3.2" cross CrossVersion.full),
)

val commonSettings = Seq(
  scalaVersion := "2.13.6",
  fork in Test := true,
  name := "blocking",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.1.1",
    "co.fs2" %% "fs2-io" % "3.0.4",
  ) ++ compilerPlugins,
  scalacOptions -= "-Xfatal-warnings",
)

val blocker =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(skip in publish := true)
