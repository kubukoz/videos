package demo

import cats.effect.Blocker
import cats.effect.ContextShift
import java.nio.file.Paths
import cats.effect.IO

class Fs2Demo(blocker: Blocker)(implicit cs: ContextShift[IO]) {
  val bytes: fs2.Stream[IO, Byte] = fs2.io.file.readAll[IO](Paths.get("build.sbt"), blocker, 4096)
}
