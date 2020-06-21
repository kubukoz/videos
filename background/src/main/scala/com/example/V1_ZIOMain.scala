package com.example

import zio._
import zio.console._
import zio.clock._
import zio.duration._

object ZIOMain extends zio.App {
  import Text._
  import zio.interop.catz._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val process = Commons.process[ZIO[Clock, Throwable, *]]

    val supervisor: ZIO[Console with Clock, Nothing, Unit] = for {
      _ <- putStrLn("Starting background process".supervisorMessage)
      _ <- process.fork
      _ <- ZIO.sleep(500.millis)
      _ <- putStrLn("Finishing supervisor".supervisorMessage)
    } yield ()

    supervisor.fork.flatMap { supervisorFiber =>
      supervisorFiber.join
    } *>
      putStrLn("After supervisor") *>
      putStrLn("Not printing anymore!").delay(500.millis)
  }.as(ExitCode.success)
}
