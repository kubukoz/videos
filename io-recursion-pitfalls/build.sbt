val compilerPlugins = List(
  compilerPlugin("org.polyvariant" % "better-tostring" % "0.3.11" cross CrossVersion.full)
  // compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
)

val root =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.7",
      fork in Test := true,
      scalacOptions -= "-Xfatal-warnings",
      libraryDependencies ++= compilerPlugins,
      libraryDependencies ++= List(
        "org.typelevel" %% "cats-effect" % "3.2.9",
        "com.kubukoz" %% "debug-utils" % "1.1.3",
      ),
      skip in publish := true,
    )
