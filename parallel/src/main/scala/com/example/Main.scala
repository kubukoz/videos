package com.example

import cats.effect._
import cats.Semigroupal
import cats.Applicative
import cats.Parallel
import cats.data.NonEmptyList
import java.time.Instant
import java.time.Duration
import scala.util.Random
import org.http4s.client.Client
import java.nio.file.Path
import java.net.InetSocketAddress
import fs2.Chunk
import fs2.io.tcp.Socket
import cats.kernel.Semigroup
import cats.Functor
import cats.Monad
import cats.data.Validated
import cats.FlatMap
import cats.Apply

trait Examples extends IOApp {

  object `1` {
    //

    type Either[+E, +A]

    //

    type IO[+A]

    //
  }

  object `2` {
    //go to
    com.example.`2`
  }

  object `3` {

    trait use[E] {
      val a: Either[E, Int]
      def f(i: Int): Either[E, String]

      a.flatMap(f)
    }

    object foo {

      sealed trait Either[E, A] {

        def flatMap[B](f: A => Either[E, B]): Either[E, B] =
          this match {
            case Left(e)  => Left(e)
            case Right(a) => f(a: A)
          }

      }

      case class Left[E, A](e: E) extends Either[E, A]
      case class Right[E, A](a: A) extends Either[E, A]
    }

  }

  class `4`[E] {
    //

    def independent(
      nameValidation: Either[E, String],
      ageValidation: Either[E, Int]
    ) = {
      case class Person(name: String, age: Int)

      val person: Either[E, Person] = ???
    }

  }

  trait `5`[E] {

    def merge[A, B](
      left: Either[E, A],
      right: Either[E, B]
    ): Either[List[E], (A, B)] = ???

    //not technically a tuple ;)

    def mergeList[A, B](
      eithers: List[Either[E, A]]
    ): Either[List[E], List[A]] = ???

  }

  object `6` {

    import cats.Semigroup
    import cats.syntax.semigroup._

    def combine[E: Semigroup, A, B](
      left: Either[E, A],
      right: Either[E, B]
    ): Either[E, (A, B)] =
      (left, right) match {
        case (Left(e1), Left(e2)) => Left(e1 |+| e2)
        case (Left(e), _)         => Left(e)
        case (_, Left(e))         => Left(e)
        case (Right(a), Right(b)) => Right((a, b))
      }

    def combineAll[E: Semigroup, A](
      eithers: List[Either[E, A]]
    ): Either[E, List[A]] =
      eithers.foldRight[Either[E, List[A]]](Right(Nil)) {
        combine(_, _).map {
          case (current, previous) => current :: previous
        }
      }

  }

  cats.data.Validated

  object `7` {
    //go to
    com.example.`7`
  }

  trait `8` {
    import cats.Semigroup
    import cats.data.Validated

    implicit def semigroupForValidated[E, B](
      implicit E: Semigroup[E],
      B: Semigroup[B]
    ): Semigroup[Validated[E, B]]

    implicit def applicativeForValidated[E](
      implicit E: Semigroup[E]
    ): Applicative[Validated[E, *]]

  }

  object `9` {
    //go to
    com.example.`9`
  }

  trait `10`[E] {

    import cats._
    import cats.implicits._

    import `6`.combine

    val nameValidation: Either[NonEmptyList[E], String]
    val ageValidation: Either[NonEmptyList[E], Int]

    case class Person(name: String, age: Int)

    val person: Either[NonEmptyList[E], Person] =
      (
        nameValidation.toValidated,
        ageValidation.toValidated
      ).mapN(Person).toEither

  }

  object `11` {
    import io.circe.generic.auto._
    import org.http4s.client.dsl.io._
    import org.http4s.Method._
    import org.http4s.implicits._
    import scala.concurrent.duration._
    import org.http4s.circe.CirceEntityCodec._

    val program = IO(println("hello world"))

    program *> program.delayBy(10.millis)

    case class CreateUser(name: String)

    def clientDemo(client: Client[IO]): IO[CreateUser] =
      client
        .expect[CreateUser](
          POST(
            body = CreateUser("admin"),
            uri = uri"https://httpbin.org/anything"
          )
        )

    trait Files[F[_]] {
      def bytes(path: Path): fs2.Stream[F, Byte]
    }

    object Files {

      def resource[F[_]: Sync: ContextShift](
        chunkSize: Int = 4096
      ): Resource[F, Files[F]] =
        Blocker[F].map(b => path => fs2.io.file.readAll[F](path, b, chunkSize))

    }

    def fileContents(
      path: Path,
      files: Files[IO]
    ): IO[String] =
      files
        .bytes(path)
        .through(fs2.text.utf8Decode[IO])
        .compile
        .string

    def sendBytes(
      bytes: List[Byte],
      socket: Socket[IO]
    ): IO[Unit] =
      socket.write(Chunk.seq(bytes))

  }

  object `12` {
    val get: IO[String] =
      IO(Random.nextString(length = 10))

    def print(a: String): IO[Unit] =
      IO(println(a))

    get.flatMap(print)
  }

  trait `13`[A, B] {

    val one: IO[A]
    val two: IO[B]

    // `two` starts after `one` completes!
    one.flatMap(_ => two)
  }

  object `14` {

    def concurrently[A, B](
      left: IO[A],
      right: IO[B]
    ): IO[(A, B)] =
      left.parProduct(right)

    def concurrently3[A, B, C](
      one: IO[A],
      two: IO[B],
      three: IO[C]
    ): IO[(A, (B, C))] =
      concurrently(
        one,
        concurrently(
          two,
          three
        )
      )

    def concurrentlyAll[A](
      ios: List[IO[A]]
    ): IO[List[A]] =
      ios.foldRight[IO[List[A]]](IO.pure(Nil)) { (a, b) =>
        concurrently(a, b).map {
          case (current, previous) => current :: previous
        }
      }

  }

  trait `15`[Hmm[_[_]]] {

    def merge[E: Semigroup, A, B](
      left: Either[E, A],
      right: Either[E, B]
    ): Either[E, (A, B)]

    def concurrently[A, B](
      left: IO[A],
      right: IO[B]
    ): IO[(A, B)]

    def hmm[F[_]: Hmm, A, B](
      left: F[A],
      right: F[B]
    ): F[(A, B)]

  }

  trait `16` {

    import cats.syntax.functor._

    abstract class Applicative[F[_]: Functor] {

      def zip[A, B](
        left: F[A],
        right: F[B]
      ): F[(A, B)]

    }

    def merge[E: Semigroup, A, B](
      left: Either[E, A],
      right: Either[E, B]
    ): Either[E, (A, B)]

    def concurrently[A, B](
      left: IO[A],
      right: IO[B]
    ): IO[(A, B)]

    implicit def forEither[E: Semigroup]: Applicative[Either[E, *]] =
      new Applicative[Either[E, *]] {

        def zip[A, B](
          left: Either[E, A],
          right: Either[E, B]
        ): Either[E, (A, B)] =
          merge(left, right)

      }

    implicit val forIO: Applicative[IO] =
      new Applicative[IO] {

        def zip[A, B](
          left: IO[A],
          right: IO[B]
        ): IO[(A, B)] =
          concurrently(left, right)

      }

  }

  trait fakeLaws[IsEq[+_]] {

    trait F[A] {
      def ap(a: Any): this.type
      def flatMap(a: A => Any): this.type
      def map(a: A => Any): this.type
      def <->(a: Any): IsEq[Nothing]
    }

  }

  trait `17`[IsEq[+_]] extends fakeLaws[IsEq] {

    def flatMapConsistentApply[A, B](
      fa: F[A],
      fab: F[A => B]
    ): IsEq[F[B]] =
      // ap is equivalent to `zip` then `map` applying the function on A
      fab.ap(fa) <-> fab.flatMap(f => fa.map(f))

  }

  object `18` {

    abstract class Parallel[F[_]] {

      def parZip[A, B](
        left: F[A],
        right: F[B]
      ): F[(A, B)]

    }

  }

  trait `19` {
    class ParIO[A](io: IO[A])

    implicit val forParallelIO: Applicative[ParIO]
  }

  trait `20` {
    import `18`.Parallel
    val p: `19`
    import p.ParIO

    implicit class ApplicativeZip[F[_]: Applicative, A](
      fa: F[A]
    ) {
      def zip[B](another: F[B]): F[(A, B)] =
        Applicative[F].product(fa, another)
    }

    trait ~>[F[_], G[_]] {
      def apply[A](fa: F[A]): G[A]
    }

    def parallelByIsomorphism[F[_]: Monad, G[_]: Applicative](
      to: F ~> G
    )(
      from: G ~> F
    ): Parallel[F] =
      new Parallel[F] {

        def parZip[A, B](
          left: F[A],
          right: F[B]
        ): F[(A, B)] =
          from(
            to(left).zip(to(right))
          )

      }

    implicit def eitherMonad[E]: Monad[Either[E, *]]

    implicit def validatedApplicative[
      E: Semigroup
    ]: Applicative[Validated[E, *]]

    def eitherToValidated[E]: Either[E, *] ~> Validated[E, *]
    def validatedToEither[E]: Validated[E, *] ~> Either[E, *]

    implicit def eitherParallel[E: Semigroup]: Parallel[Either[E, *]] =
      parallelByIsomorphism(eitherToValidated[E])(
        validatedToEither[E]
      )

    //

    implicit val ioMonad: Monad[IO]
    implicit val parIOApplicative: Applicative[ParIO]

    val ioToParIO: IO ~> ParIO
    val parIOToIO: ParIO ~> IO

    implicit val ioParallel: Parallel[IO] =
      parallelByIsomorphism(ioToParIO)(parIOToIO)

  }

  trait `21` {
    import cats.~>

    //no laws
    def parallelByIsomorphism[F[_]: Monad, G[_]: Applicative](
      to: F ~> G
    )(
      from: G ~> F
    ): Parallel[F]

    //can have laws!

    trait Parallel[F[_]] {
      type M[_]
      def F: Monad[F]
      def M: Applicative[F]

      def parallel: F ~> M
      def sequential: M ~> F
    }

    trait laws[IsEq[+_]] extends fakeLaws[IsEq] {

      def P: Parallel[F] { type M[A] = F[A] }

      def parallelRoundTrip[A](fa: F[A]): IsEq[F[A]] =
        P.sequential(P.parallel(fa)) <-> fa
    }

  }

  trait `22` {
    import cats.~>

    trait NonEmptyParallel[F[_]] {
      type M[_]

      def F: FlatMap[F]
      def M: Apply[F]

      def parallel: F ~> M
      def sequential: M ~> F
    }

    trait Parallel[F[_]] extends NonEmptyParallel[F] {

      def F: Monad[F]
      def M: Applicative[F]
    }

  }

}

object `2` {
  //

  sealed abstract class Either[+A, +B] extends Product with Serializable

  final case class Left[+A, +B](value: A) extends Either[A, B]

  final case class Right[+A, +B](value: B) extends Either[A, B]

  //
}

object `7` {

  sealed trait Validated[+E, +A] extends Product with Serializable

  final case class Invalid[+E](e: E) extends Validated[E, Nothing]

  final case class Valid[+A](a: A) extends Validated[Nothing, A]

}

object `9` {

  sealed abstract class Validated[E, A] extends Product with Serializable {

    def andThen[B](
      f: A => Validated[E, B]
    ): Validated[E, B] =
      this match {
        case Valid(a)   => f(a)
        case Invalid(e) => Invalid(e)
      }

  }

  final case class Valid[E, A](a: A) extends Validated[E, A]
  final case class Invalid[E, A](e: E) extends Validated[E, A]

}
