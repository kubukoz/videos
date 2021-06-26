package com.example

import cats.effect._
import cats.implicits._
import scala.concurrent.duration._

object CatsMain extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val supervisor: IO[Unit] = {
      IO.println("Starting background process".supervisorMessage) *>
        process.background.use { _ =>
          IO.sleep(500.millis) *>
            IO.println("Finishing supervisor".supervisorMessage)
        }
    }

    supervisor.start.flatMap(_.join) *>
      IO.println("After supervisor") *>
      IO.println("Not printing anymore!").delayBy(500.millis)
  }.as(ExitCode.Success)

}
