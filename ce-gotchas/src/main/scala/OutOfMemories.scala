import cats.effect.IO

object OutOfMemories {

  //FlatMap(ioa, _ => Map(repeatForever(ioa), _ => ()))
  def repeatForever[A](ioa: IO[A]): IO[Unit] =
    for {
      _ <- ioa
      _ <- repeatForever(ioa)
    } yield ()

}
