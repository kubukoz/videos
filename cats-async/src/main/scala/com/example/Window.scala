package com.example

import scala.concurrent.duration.FiniteDuration
import cats.effect.Resource
import java.util.concurrent.Executors
import cats.effect.IO
import java.util.concurrent.TimeUnit

//JS-like window object. Impure APIs
trait Window {
  def setTimeout(task: Runnable, timeout: FiniteDuration): Window.CancelHook
}

object Window {
  type CancelHook = () => Unit

  val make: Resource[IO, Window] = Resource.make(IO(Executors.newScheduledThreadPool(2)))(tp => IO(tp.shutdownNow())).map {
    scheduler => (task, timeout) =>
      val scheduled = scheduler.schedule(task, timeout.toNanos, TimeUnit.NANOSECONDS)

      () => scheduled.cancel(false)
  }
}
