package com.example

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import scala.concurrent.duration._

object IOParallel extends IOApp {

  val work = IO.sleep(1000.millis)

  val p1 = work
  val p2 = work

  val runSequential = for {
    _ <- p1
    _ <- p2
  } yield ()

  val runParallelWaitBoth = p1.both(p2).void

  import cats.implicits._

  val runParallelForgetOne = for {
    fiber <- p1.start
    _     <- p2
  } yield ()

  def run(args: List[String]): IO[ExitCode] = runSequential.as(ExitCode.Success)
}
