package refs_extensible

import cats.effect.IO
import cats.effect.IOApp

object refs_extensible extends IOApp.Simple {

  def build(): IO[History] = History.build {

    def go: History.Builder[Unit] = History.length.flatMap {
      case i if i >= 5 => History.print("done")
      case i           => History.print(s"appending ${i}") *> History.append(i) *> go
    }

    go
  }

  def run: IO[Unit] = build().debug().void

}

case class History private (log: Vector[Int]) {
  private[History] def append(i: Int): History = copy(log = log.appended(i))
}

object History {

  trait HistoryOps {
    def length: IO[Int]
    def append(i: Int): IO[Unit]
    def print(s: String): IO[Unit]
  }

  type Builder[A] = HistoryOps ?=> IO[A]

  // no monad!

  val length: Builder[Int] = summon[HistoryOps].length

  def print(s: String): Builder[Unit] = summon[HistoryOps].print(s)
  def append(i: Int): Builder[Unit] = summon[HistoryOps].append(i)

  def build(b: Builder[Unit]): IO[History] = IO
    .ref(History(Vector.empty))
    .flatTap { ref =>
      b(
        using new HistoryOps {
          def length: IO[Int] = ref.get.map(_.log.size)
          def append(i: Int): IO[Unit] = ref.update(history => history.append(i))
          def print(s: String): IO[Unit] = IO.println(s)
        }
      )
    }
    .flatMap(_.get)

}
