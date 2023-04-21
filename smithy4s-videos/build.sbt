val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.10",
    libraryDependencies ++= Seq(
      "com.disneystreaming.smithy4s" %% "smithy4s-core" % smithy4sVersion.value,
      "com.disneystreaming.smithy4s" %% "smithy4s-http4s" % smithy4sVersion.value,
      "org.http4s" %% "http4s-ember-server" % "0.23.18",
    ),
    fork := true,
  )
  .enablePlugins(Smithy4sCodegenPlugin)
