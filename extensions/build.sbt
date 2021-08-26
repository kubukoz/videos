val root = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.2.3"
    ),
    scalacOptions ++= Seq(
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:params",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Ywarn-value-discard"
    )
  )
