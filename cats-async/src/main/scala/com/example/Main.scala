package com.example

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import cats.effect.Resource
import cats.effect.Blocker
import cats.implicits._
import java.util.concurrent.Executors
import scala.concurrent.duration._

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    // Window.make.use(new WindowMain(_).run) *>
    IO(println("Hello"))
  }.as(ExitCode.Success)
}

class WindowMain(window: Window) {

  def sleep(d: FiniteDuration): IO[Unit] =
    ???

  val run: IO[Unit] = ???

  // val run2: IO[Unit] = ???

  def runAsync(cb: => Unit): Unit =
    ???
}
