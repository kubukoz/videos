package com.example

import cats.effect._
import cats.implicits._
import cats.effect.Console.io._
import scala.concurrent.duration._

object CatsMain extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val supervisor: IO[Unit] = {
      putStrLn("Starting background process".supervisorMessage) *>
        process.background.use { _ =>
          IO.sleep(500.millis) *>
            putStrLn("Finishing supervisor".supervisorMessage)
        }
    }

    supervisor.start.flatMap(_.join) *>
      putStrLn("After supervisor") *>
      putStrLn("Not printing anymore!").delayBy(500.millis)
  }.as(ExitCode.Success)
}
