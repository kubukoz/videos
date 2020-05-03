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
import java.util.concurrent.atomic.AtomicReference

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    Window.make.use(new WindowMain(_).run) *>
      IO(println("Hello"))
  }.as(ExitCode.Success)
}

class WindowMain(window: Window) {

  def sleep(d: FiniteDuration): IO[Unit] = IO.cancelable[Unit] { cb =>
    val cancelHook = window.setTimeout(
      () => cb(Right(())),
      d
    )

    IO(cancelHook())
  }

  val run: IO[Unit] = {
    sleep(1.second) *>
      IO(println("hello")) *>
      sleep(2.seconds) *>
      IO(println("world"))
  }

  val run2: IO[Unit] = IO.cancelable[Unit] { cb =>
    val hook = runAsync(cb(Right(())))

    IO(hook())
  }

  def runAsync(cb: => Unit): Window.Cancelable = {
    val cancelable = new AtomicReference[Window.Cancelable](() => ())

    val canc1 = window.setTimeout(() => {
      println("hello")

      val canc2 = window.setTimeout(() => {
        println("world")
        cb
      }, 2.seconds)

      cancelable.set(canc2)
    }, 1.second)

    cancelable.set(canc1)

    () => cancelable.get()()
  }
}
