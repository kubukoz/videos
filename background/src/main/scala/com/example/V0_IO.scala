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

  val runParallelWaitBoth = p1.parProduct(p2)

  val runParallelForgetOne = for {
    _ <- p1.start
    _ <- p2
  } yield ()

  def run(args: List[String]): IO[ExitCode] = IO.never
}
