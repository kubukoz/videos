package com.example

import cats.effect._
import cats.implicits._
import cats.effect.Console.io._
import scala.concurrent.duration._
import scala.util.Random

object CatsMainWrong extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val rollFailure = IO(Random.nextBoolean()).ifM(
      ifTrue = IO.raiseError(new Throwable("oops!")),
      ifFalse = IO.unit
    )

    val supervisor: IO[Unit] = for {
      _  <- putStrLn("Starting background process".supervisorMessage)
      bg <- process.start
      _  <- IO.sleep(500.millis)
      //                                                 _  <- rollFailure
      _ <- putStrLn("Finishing supervisor".supervisorMessage)
      //                                                 _  <- bg.cancel
    } yield ()

    val supervisorSuccess = supervisor.background.use(_.attempt.map(_.isRight)) <* putStrLn("After supervisor")

    supervisorSuccess.ifM(
      ifTrue = putStrLn("Not printing anymore!").delayBy(500.millis),
      ifFalse = putStrLn("Still printing because of fiber leak!").delayBy(500.millis)
    )
  }.as(ExitCode.Success)
}
