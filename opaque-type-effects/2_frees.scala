package frees

import cats.Monad
import cats.arrow.FunctionK
import cats.effect.IO
import cats.effect.IOApp
import cats.free.Free
import cats.syntax.all.*

object frees extends IOApp.Simple {

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

  private enum Alg[A] {
    case Length extends Alg[Int]
    case Print(s: String) extends Alg[Unit]
    case Append(i: Int) extends Alg[Unit]
  }

  opaque type Builder[A] = Free[Alg, A]

  given Monad[Builder] = Free.catsFreeMonadForFree

  val length: Builder[Int] = Free.liftF(Alg.Length)
  def print(s: String): Builder[Unit] = Free.liftF(Alg.Print(s))
  def append(i: Int): Builder[Unit] = Free.liftF(Alg.Append(i))

  def build(b: Builder[Unit]): IO[History] = IO
    .ref(History(Vector.empty))
    .flatMap { ref =>
      b.foldMap(
        new FunctionK[Alg, IO] {
          def apply[A](fa: Alg[A]): IO[A] =
            fa match {
              case Alg.Length    => ref.get.map(_.log.size)
              case Alg.Append(i) => ref.update(_.append(i))
              case Alg.Print(s)  => IO.println(s)
            }
        }
      ) *> ref.get
    }

}
