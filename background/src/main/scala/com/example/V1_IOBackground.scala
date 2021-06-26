package com.example

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import scala.concurrent.duration._

object IOBackground extends IOApp.Simple {

  def proc(latch: IO[Unit], reverseLatch: Int => IO[Unit]) = {
    IO.println("starting") *>
      IO.sleep(100.millis) *>
      IO.println("started") *>
      reverseLatch(10) *>
      latch *>
      IO.sleep(1.second) *>
      IO.pure(42)
  }.guaranteeCase { outcome =>
    IO.println(s"completed with $outcome")
  }

  def run: IO[Unit] =
    IO.deferred[Unit].flatMap { promise =>
      IO.deferred[Int].flatMap { num =>
        proc(promise.get, num.complete(_).void)
          .background
          .use { join =>
            IO.println("main started") *>
              num.get.flatMap(IO.println(_)) *>
              IO.sleep(1.second) *>
              promise.complete(()) *>
              IO.println("background unblocked") *>
              join
          }
          // .timeout(200.millis)
          .attempt
          .void
      }
    }

}
