package com.kubukoz.example

import fs2.Stream
import fs2.Pipe
import fs2.Pull
import cats.effect.Timer
import cats.effect.Concurrent
import cats.effect.concurrent.Deferred
import cats.effect.ExitCase
import cats.implicits._
import scala.concurrent.duration._
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode

object SnippetRunner extends IOApp {

  // Split the stream at N elements, if the tail has any more values a "more" message is emitted.
  def limitedElements[F[_]](n: Long): Pipe[F, String, String] =
    _.map(_.toString)
      .pull
      .take(n)
      .flatMap(_.flatTraverse(_.pull.uncons1))
      .flatMap(_.as(Pull.output1(s"... (more than $n)")).sequence_)
      .stream

  // Interrupts the stream after the given time, emitting an extra element if it doesn't terminate normally.
  def maxTime[F[_]: Timer: Concurrent](time: FiniteDuration): Pipe[F, String, String] =
    s =>
      Stream.eval(Deferred[F, Option[String]]).flatMap { extraElement =>
        s.onFinalizeCase {
            case ExitCase.Canceled =>
              extraElement.complete(s"(timed out after $time)".some)
            case _ => extraElement.complete(none)
          }
          .interruptAfter(time) ++ Stream.evals(extraElement.get)
      }

  implicit class ShowValues[A](stream: Stream[IO, A]) {

    //A utility for worksheets that allows showing a couple values of a stream produced within reasonable time.
    def showValues =
      stream
        .map(_.toString)
        .through(limitedElements(10))
        .through(maxTime(3.seconds))
        .compile
        .toList
        .unsafeRunSync()
        .mkString(", ")
  }

  //expose these for easier scripting
  implicit val publicTimer = timer
  implicit val publicShift = contextShift

  def run(args: List[String]): IO[ExitCode] = IO.unit.as(ExitCode.Success)
}
