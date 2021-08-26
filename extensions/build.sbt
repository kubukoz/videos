val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.2.3"
    )
  )
