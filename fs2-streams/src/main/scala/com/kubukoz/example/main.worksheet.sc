import cats.effect.Concurrent
import fs2.Pipe
import cats.effect.ExitCase
import cats.effect.concurrent.Deferred
import cats.effect.ContextShift
import scala.concurrent.ExecutionContext
import cats.effect.Timer
import cats.effect.IO
import cats.implicits._
import fs2.Stream
import fs2.Pull
import scala.concurrent.duration._

implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

implicit class ShowValues[A](stream: Stream[IO, A]) {

  // Split the stream at N elements, if the tail has any more values a "more" message is emitted.
  private def limitedElements[F[_]](n: Long): Pipe[F, String, String] =
    _.map(_.toString).pull.take(n).flatMap(_.flatTraverse(_.pull.uncons1).flatMap(_.as(Pull.output1("... (more)")).sequence_)).stream

  private def maxTime[F[_]: Timer: Concurrent](time: FiniteDuration): Pipe[F, String, String] =
    s =>
      Stream.eval(Deferred[F, Option[String]]).flatMap { extraElement =>
        s.onFinalizeCase {
            case ExitCase.Canceled => extraElement.complete("(timed out)".some)
            case _                 => extraElement.complete(none)
          }
          .interruptAfter(5.seconds) ++ Stream.evals(extraElement.get)
      }

  def showValues =
    stream.map(_.toString).through(limitedElements(20)).through(maxTime(5.seconds)).compile.toList.unsafeRunSync().mkString(", ")
}

Stream.evals(IO(List(1, 2, 3))).showValues
Stream.evals(IO(List(1, 2))).showValues

Stream.iterate(0)(_ + 1).showValues
Stream.iterateEval(0)(s => IO.sleep(10.millis).as(s + 1)).showValues
