import cats.effect.IO

object StackOverflows {

  def repeatForever[A](ioa: IO[A]): IO[Unit] =
    ioa *> repeatForever(ioa)

}
