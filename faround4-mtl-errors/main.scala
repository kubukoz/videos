import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Ref
import cats.mtl.Handle
import cats.mtl.Raise
import cats.mtl.syntax.all.*
import cats.syntax.all.*

object demo extends IOApp.Simple {

  def run: IO[Unit] =
    IterationState
      .run {
        IterationState.increment.replicateA_(4) *>
          // IterationState.toggle *>
          IterationState.increment.replicateA_(6)
      }
      .debug()
      .void

}

case class IterationState private (iterations: Int, hasToggled: Boolean)

object IterationState {
  type Builder[A] = Ref[IO, IterationState] ?=> Raise[IO, Error] ?=> IO[A]

  private val ref: Ref[IO, IterationState] ?=> Ref[IO, IterationState] =
    summon[Ref[IO, IterationState]]

  def toggle: Builder[Unit] = ref.update(_.copy(hasToggled = true))

  // if toggle isn't called in the first 5 iterations, increment fails
  def increment: Builder[Unit] = ref.get.flatMap {
    case IterationState(hasToggled = hasToggled, iterations = iterations) =>
      if (iterations >= 5 && !hasToggled)
        Error().raise
      else
        ref.update(s => s.copy(iterations = s.iterations + 1))
  }

  def run[A](builder: Builder[Unit]): IO[Int] = IO
    .ref(IterationState(iterations = 0, hasToggled = false))
    .flatMap { case (ref @ given Ref[IO, IterationState]) =>
      Handle
        .allow {
          builder
            *> ref.get.map(_.iterations)
        }
        .rescue { case Error() => IO.pure(-1) }
    }

  case class Error()
}
