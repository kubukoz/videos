val root = project
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-client" % "0.23.10",
      "org.http4s" %% "http4s-circe" % "0.23.10",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.0" % Test,
      compilerPlugin("org.polyvariant" %% "better-tostring" % "0.3.14" cross CrossVersion.full),
    ),
    scalaVersion := "3.1.1",
  )
