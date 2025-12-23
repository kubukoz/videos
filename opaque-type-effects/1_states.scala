package states

import cats.Monad
import cats.data.IndexedStateT
import cats.data.StateT
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all.*

object states extends IOApp.Simple {

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

  opaque type Builder[A] = StateT[IO, History, A]

  given Monad[Builder] = IndexedStateT.catsDataMonadForIndexedStateT

  val length: Builder[Int] = StateT.inspect(_.log.size)

  def print(s: String): Builder[Unit] = StateT.liftF(IO.println(s))
  def append(i: Int): Builder[Unit] = StateT.modify(_.append(i))

  def build(b: Builder[Unit]): IO[History] = b.runS(History(Vector.empty))

}
