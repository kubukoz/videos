package com.example

import fs2.Stream
import fs2.concurrent.Queue
import cats.effect.Resource
import cats.effect.Concurrent
import cats.effect.implicits._
import cats.implicits._
import cats.effect.IO
import cats.effect.ExitCode
import cats.effect.IOApp
import cats.effect.Fiber
import cats.effect.concurrent.Deferred

trait ManagedBackground[F[_]] {
  //completes with a fiber when the job is dequeued
  def apply[A](program: F[A]): F[Fiber[F, A]]
}

object ManagedBackground {

  trait Job[F[_]] {
    type Result
    def run: F[Result]
    def registerFiber(fiber: Fiber[F, Result]): F[Unit]
  }

  object Job {

    def fromEffect[F[_], A](program: F[A])(deff: Deferred[F, Fiber[F, A]]): Job[F] = new Job[F] {
      type Result = A

      val run: F[A] = program
      def registerFiber(fiber: Fiber[F, A]): F[Unit] = deff.complete(fiber)
    }
  }

  def fromQueue[F[_]: Concurrent](q: Queue[F, Job[F]])(maxConcurrent: Int): Resource[F, ManagedBackground[F]] = {
    val man: ManagedBackground[F] = new ManagedBackground[F] {
      def apply[A](program: F[A]): F[Fiber[F, A]] = Deferred[F, Fiber[F, A]].flatMap { promise =>
        val job = Job.fromEffect(program)(promise)

        q.enqueue1(job) *> promise.get
      }
    }

    //follow the train...
    def runOneJob(job: Job[F]): Stream[F, Unit] = {

      def register(fiber: Fiber[F, job.Result]): F[Unit] = Deferred[F, Unit].flatMap { userCanceled =>
        //Fake fiber to make sure that user cancelation completes the effect, which finalizes the supervising stream (which actually cancels the fiber)
        val newFiber = Fiber[F, job.Result](
          fiber.join,
          userCanceled.complete(())
        )

        val awaitFiber = fiber.join.race(userCanceled.get)

        job.registerFiber(newFiber) *> awaitFiber.void
      }

      Stream.supervise(job.run).evalMap(register)
    }

    q.dequeue.map(runOneJob).parJoin(maxConcurrent).compile.drain.background.as(man)
  }

  def unbounded[F[_]: Concurrent]: Resource[F, ManagedBackground[F]] =
    Resource.liftF(Queue.unbounded[F, Job[F]]).flatMap(fromQueue[F](_)(Int.MaxValue))
}

import cats.effect.Console.io._
import scala.concurrent.duration._
import scala.util.Random

object ManagedBackgroundMain extends IOApp {
  import Text._

  def run(args: List[String]): IO[ExitCode] = {
    val process = Commons.process[IO]

    val supervisor: IO[Unit] =
      ManagedBackground.unbounded[IO].use { start =>
        for {
          _                               <- putStrLn("Starting background process".supervisorMessage)
          (processBg: Fiber[IO, Nothing]) <- start(process)
          _                               <- IO.sleep(500.millis)
          _                               <- putStrLn("Finishing supervisor".supervisorMessage)
        } yield ()
      }

    val supervisorSuccess = supervisor.background.use(identity) <* putStrLn("After supervisor")

    supervisorSuccess *> putStrLn("Not printing anymore!").delayBy(500.millis)
  }.as(ExitCode.Success)
}
