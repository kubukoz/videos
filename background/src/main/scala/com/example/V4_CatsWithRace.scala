package com.example

import cats.effect._
import cats.implicits._
import cats.effect.Console.io._
import cats.effect.implicits._
import scala.concurrent.duration._

object CatsWithRace extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val supervisor: IO[Unit] = {

      val startSupervisor = putStrLn("Starting background process".supervisorMessage)
      val finishSupervisor = putStrLn("Finishing supervisor".supervisorMessage).delayBy(500.millis)

      startSupervisor *>
        (process race finishSupervisor)
    }.map(_.merge)

    supervisor.start.flatMap(_.join) *>
      putStrLn("After supervisor") *>
      putStrLn("Not printing anymore!").delayBy(500.millis)
  }.as(ExitCode.Success)
}
