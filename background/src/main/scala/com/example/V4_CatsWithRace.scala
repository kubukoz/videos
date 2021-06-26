package com.example

import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import scala.concurrent.duration._

object CatsWithRace extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val supervisor: IO[Unit] = {

      val startSupervisor = IO.println("Starting background process".supervisorMessage)
      val finishSupervisor = IO.println("Finishing supervisor".supervisorMessage).delayBy(500.millis)

      startSupervisor *>
        (process race finishSupervisor)
    }.map(_.merge)

    supervisor.start.flatMap(_.join) *>
      IO.println("After supervisor") *>
      IO.println("Not printing anymore!").delayBy(500.millis)
  }.as(ExitCode.Success)

}
