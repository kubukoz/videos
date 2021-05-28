package demo

import cats.effect._

trait JDBC[F[_]] {
  def execute: F[Unit]
}

object JDBC {

  def mock[F[_]: Sync]: JDBC[F] = new JDBC[F] {

    def execute: F[Unit] = Sync[F].blocking {
      println("Executing...")
      Thread.sleep(1000)
    }

  }

  // you can do IO.blocking directly and it doesn't require any imports (just IO)!
  val mockIO: JDBC[IO] = new JDBC[IO] {

    def execute: IO[Unit] =
      IO.blocking {
        println("Executing...")
        Thread.sleep(1000000)
      }.start
        .flatMap(_.cancel)

  }

}

object BlockerDemo extends IOApp.Simple {

  val jdbc: JDBC[IO] = JDBC.mock[IO]

  def run: IO[Unit] = jdbc.execute
}
