val root = project
  .in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      compilerPlugin("org.polyvariant" %% "better-tostring" % "0.3.14" cross CrossVersion.full),
      "org.typelevel" %% "cats-core" % "2.7.0",
    ),
    scalaVersion := "2.13.7",
    scalacOptions -= "-Xfatal-warnings",
  )
