import cats.effect._

object Main extends IOApp.Simple {
  def run: IO[Unit] = IO.println("hello world")
}
