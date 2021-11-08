import cats.effect.IO
import cats.Defer

object ProperRepeat {

  def usingFlatMap[A](ioa: IO[A]): IO[Nothing] =
    ioa.flatMap(_ => usingFlatMap(ioa))

  def usingDefer[A](ioa: IO[A]): IO[Unit] =
    ioa *> IO.defer(usingDefer(ioa))

  def usingFix[A](ioa: IO[A]): IO[Unit] =
    Defer[IO].fix[Unit] { self =>
      ioa *> self
    }

  def usingLazyArg[A](ioa: IO[A]): IO[Unit] =
    ioa >> usingLazyArg(ioa)

  //def usingBetterMonadicFor

}
