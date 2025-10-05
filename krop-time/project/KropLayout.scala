// A simple plugin that sets the directory layout to the Krop standard. This
// removes the excessive indirection the Maven standard uses. Inspired by
// PlayLayoutPlugin.

import sbt._
import sbt.Keys._

import play.twirl.sbt.Import.TwirlKeys

object KropLayout extends AutoPlugin {
  // Must be explicitly enabled
  override def trigger = noTrigger

  override def projectSettings = Seq(
    Compile / resourceDirectory := baseDirectory.value / "resources",
    Compile / scalaSource := baseDirectory.value / "src",
    Test / scalaSource := baseDirectory.value / "test",
    Compile / javaSource := baseDirectory.value / "src",
    Test / javaSource := baseDirectory.value / "test"
  )
}
