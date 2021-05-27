package demo

import fs2.io.file.Files
import java.nio.file.Paths

class Fs2Demo[F[_]: Files] {
  val bytes: fs2.Stream[F, Byte] = Files[F].readAll(Paths.get("build.sbt"), 4096)
}
