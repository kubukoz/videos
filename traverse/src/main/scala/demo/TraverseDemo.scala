package demo

import cats.implicits._
import cats.data.NonEmptyList
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.Console.io.putStrLn
import scala.concurrent.duration._
import cats.data.ValidatedNel
import cats.data.Validated.Valid

object TraverseDemo extends IOApp {

  //suspending futures in IO to keep referential transparency
  val sequencedFutures = IO {
    import scala.concurrent.Future
    import scala.concurrent.ExecutionContext.Implicits.global

    val future1 = Future {
      Thread.sleep(1000)
      42
    }

    val future2 = Future {
      Thread.sleep(500)
      100
    }

    val futures = List(future1, future2)

    Future.sequence(futures)
  }

  val sequencedIOs = {
    //using non-blocking sleep as it's good
    val io1 = IO.sleep(1.second).as(42)
    val io2 = IO.sleep(500.millis).as(500)

    //this would run all IOs in the list in parallel
    //List(io1, io2).parSequence
    List(io1, io2).sequence
  }

  val traversedInts = {
    val ints = List.range(1, 10)

    def action(i: Int): IO[String] = IO.sleep(500.millis).as(i.toString)

    ints.traverse(action)
  }

  val sequencedOptions = {
    val options: List[Option[String]] = List(
      Some("hello"),
      Some("world"),
      None,
      Some("again")
    )

    // val options: List[Option[String]] = List(
    //   Some("hello"),
    //   Some("world"),
    //   Some("again")
    // )

    options.sequence
  }

  val sequencedEithers = {
    val eithers: List[Either[String, Int]] = List(
      Right(42),
      Left("err1"),
      Left("err2"),
      Right(200)
    )

    // val eithers: List[Either[String, Int]] = List(
    //   Right(42),
    //   Right(200)
    // )

    eithers.sequence: Either[String, List[Int]]
  }

  val sequencedValidations = {
    //ValidatedNel has NonEmptyList in the failure type

    val validations: List[ValidatedNel[String, Int]] = List(
      Valid(42),
      "err".invalidNel,
      "err2".invalidNel,
      Valid(200)
    )

    //contains two failures in the Nel
    validations.sequence: ValidatedNel[String, List[Int]]
  }

  val sequencedMap = {

    val maps: NonEmptyList[Map[Int, String]] = NonEmptyList.of(
      Map(
        1 -> "hello",
        2 -> "world",
        3 -> "!!!!!!!!"
      ),
      Map(
        1 -> "foo",
        3 -> "........",
        4 -> "anything"
      )
    )

    //would contain all keys in all the maps in the list, with values combined in a NEL
    //maps.reduce

    //contains only keys that exist in all maps in the list, with values combined in a NEL
    maps.nonEmptySequence: Map[Int, NonEmptyList[String]]
  }

  def run(args: List[String]): IO[ExitCode] = {
    IO.fromFuture(sequencedFutures).flatMap(putStrLn(_)) *>
      sequencedIOs.flatMap(putStrLn(_)) *>
      traversedInts.flatMap(putStrLn(_)) *>
      putStrLn(sequencedOptions) *>
      putStrLn(sequencedEithers) *>
      putStrLn(sequencedValidations) *>
      putStrLn(sequencedMap)
  } as ExitCode.Success
}
