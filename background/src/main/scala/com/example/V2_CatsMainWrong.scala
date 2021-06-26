package com.example

import cats.effect._
import cats.implicits._
import scala.concurrent.duration._
import scala.util.Random

object CatsMainWrong extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val rollFailure = IO(Random.nextBoolean()).ifM(
      ifTrue = IO.raiseError(new Throwable("oops!")),
      ifFalse = IO.unit,
    )

    val supervisor: IO[Unit] = for {
      _  <- IO.println("Starting background process".supervisorMessage)
      bg <- process.start
      _  <- IO.sleep(500.millis)
      //                                                 _  <- rollFailure
      _  <- IO.println("Finishing supervisor".supervisorMessage)
      //                                                 _  <- bg.cancel
    } yield ()

    val supervisorSuccess = supervisor.background.use(_.attempt.map(_.isRight)) <* IO.println("After supervisor")

    supervisorSuccess.ifM(
      ifTrue = IO.println("Not printing anymore!").delayBy(500.millis),
      ifFalse = IO.println("Still printing because of fiber leak!").delayBy(500.millis),
    )
  }.as(ExitCode.Success)

}
