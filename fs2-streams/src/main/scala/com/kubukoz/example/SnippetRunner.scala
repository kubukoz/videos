package com.kubukoz.example

import fs2.Stream
import fs2.Pipe
import fs2.Pull
import cats.effect.Temporal
import cats.implicits._
import scala.concurrent.duration._
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import scala.util.Random
import cats.effect.kernel.Deferred
import cats.effect.kernel.Resource
import cats.effect.std
import cats.effect.unsafe.IORuntime

object SnippetRunner {

  // // Split the stream at N elements, if the tail has any more values a "more" message is emitted.
  def limitedElements[F[_]](n: Long): Pipe[F, String, String] =
    _.map(_.toString).pull
      .take(n)
      .flatMap(_.flatTraverse(_.pull.uncons1))
      .flatMap(_.as(Pull.output1(s"... (more than $n)")).sequence_)
      .stream

  // // Interrupts the stream after the given time, emitting an extra element if it doesn't terminate normally.
  def maxTime[F[_]: Temporal](
      time: FiniteDuration
  ): Pipe[F, String, String] =
    s =>
      Stream.eval(Deferred[F, Option[String]]).flatMap { extraElement =>
        s.onFinalizeCase {
          case Resource.ExitCase.Canceled =>
            extraElement.complete(s"(timed out after $time)".some).void
          case _ => extraElement.complete(none).void
        }.interruptAfter(time) ++ Stream.evals(extraElement.get)
      }

  implicit class ShowValues[A](stream: Stream[IO, A]) {
    def debugged(tag: String): Stream[IO, A] =
      stream.debug(a => s"$a: $tag", System.out.println)

    //A utility for worksheets that allows showing a couple values of a stream produced within reasonable time.
    def showValues(implicit rt: IORuntime): String = {
      val oldOut = System.out
      try {
        System
          .setOut(Console.out)

        stream
          .map(_.toString)
          .through(limitedElements(10))
          .through(maxTime(3.seconds))
          .compile
          .toList
          .unsafeRunSync()
          .mkString(", ")
      } finally {
        System.setOut(oldOut)
      }
    }
  }

  val randomInt: IO[Int] = std.Random.scalaUtilRandom[IO].flatMap(_.nextInt)
}
