def crossPlugin(x: sbt.librarymanagement.ModuleID) = compilerPlugin(x cross CrossVersion.full)

val compilerPlugins = List(
  compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  crossPlugin("org.typelevel" % "kind-projector" % "0.11.0"),
  crossPlugin("com.github.cb372" % "scala-typed-holes" % "0.1.1")
)

val commonSettings = Seq(
  scalaVersion := "2.13.1",
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings") ++ Seq(
    "-Ymacro-annotations",
    "-Yimports:" ++ List(
      "scala",
      "scala.Predef",
      "cats",
      "cats.implicits",
      "cats.effect",
      "cats.effect.implicits",
      "cats.effect.concurrent"
    ).mkString(",")
  )),
  fork in Test := true,
  updateOptions := updateOptions.value.withGigahorse(false),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.1.0",
    "org.typelevel" %% "cats-mtl-core" % "0.7.0",
    "org.typelevel" %% "cats-effect" % "2.0.0",
    "org.typelevel" %% "cats-tagless-macros" % "0.10",
    "org.typelevel" %% "cats-effect-laws" % "2.0.0" % Test,
    "org.typelevel" %% "cats-laws" % "2.1.0",
    "org.typelevel" %% "cats-testkit-scalatest" % "1.0.0-RC1" % Test,
    "dev.profunktor" %% "console4cats" % "0.8.1",
    "dev.profunktor" %% "redis4cats-effects" % "0.9.1",
    "dev.profunktor" %% "redis4cats-log4cats" % "0.9.1",
    "io.chrisdavenport" %% "log4cats-noop" % "1.0.1"
  ) ++ compilerPlugins
)

val taglessFinal =
  project.in(file(".")).settings(commonSettings).settings(skip in publish := true)
