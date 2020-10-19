package com.example

import zio._
import zio.console._
import zio.clock._
import zio.duration._

object ZIOMain extends zio.App {
  import Text._
  import zio.interop.catz._

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    val process = Commons.process[RIO[Clock, *]].onInterrupt(putStrLn("Interrupted main process".supervisorMessage))

    val supervisor: ZIO[Console with Clock, Nothing, Unit] = for {
      _ <- putStrLn("Starting background process".supervisorMessage)
      _ <- process.fork
      _ <- putStrLn("Finishing supervisor".supervisorMessage).delay(500.millis)
    } yield ()

    supervisor.fork.flatMap { supervisorFiber =>
      supervisorFiber.join
    } *>
      putStrLn("After supervisor") *>
      putStrLn("Not printing anymore!").delay(500.millis)
  }.as(ExitCode.success)
}
