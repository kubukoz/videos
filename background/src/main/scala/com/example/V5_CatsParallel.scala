package com.example

import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import org.http4s.blaze.server.BlazeServerBuilder

object CatsParallel extends IOApp {
  import Text._

  val server = BlazeServerBuilder[IO](ExecutionContext.global).bindHttp(8080, "0.0.0.0").resource

  def consumer(tag: String) = Commons.consumer[IO] { case msg =>
    IO.println(tag + ": " + msg.text)
  }

  def run(args: List[String]): IO[ExitCode] =
    server.use(_ => IO.never /* IO.sleep(2.seconds) */ ).as(ExitCode.Success)
}
