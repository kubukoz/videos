package refs

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref
import cats.syntax.all.*

object refs extends IOApp.Simple {

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

  opaque type Builder[A] = ReaderT[IO, Ref[IO, History], A]

  given Monad[Builder] = ReaderT.catsDataMonadForKleisli

  val length: Builder[Int] = ReaderT(_.get.map(_.log.size))

  def print(s: String): Builder[Unit] = ReaderT.liftF(IO.println(s))
  def append(i: Int): Builder[Unit] = ReaderT(_.update(_.append(i)))

  def liftIO[A](io: IO[A]): Builder[A] = ReaderT.liftF(io)

  def build(b: Builder[Unit]): IO[History] = IO
    .ref(History(Vector.empty))
    .flatMap { ref =>
      b.run(ref) *> ref.get
    }

}
