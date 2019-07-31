package demo

import cats.implicits._
import cats.effect._

trait JDBC[F[_]] {
  def execute: F[Unit]
}

object JDBC {

  def mock[F[_]: ContextShift: Sync](blocker: Blocker): JDBC[F] = new JDBC[F] {

    def execute: F[Unit] = blocker.delay {
      println("Executing...")
      Thread.sleep(1000)
    }
  }
}

object BlockerDemo extends IOApp {

  val jdbc: Resource[IO, JDBC[IO]] = Blocker[IO].map { blocker =>
    JDBC.mock[IO](blocker)
  }

  def run(args: List[String]): IO[ExitCode] = jdbc.use(_.execute).as(ExitCode.Success)
}
