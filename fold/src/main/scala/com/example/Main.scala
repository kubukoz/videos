package com.example

import java.time.Instant
import com.example.Status.Pending
import com.example.Status.InProgress
import com.example.Status.Done

import cats.implicits._
import scala.concurrent.duration.FiniteDuration
import scala.annotation.tailrec
import com.example.Content.Video
import com.example.Content.Playlist
import scala.collection.immutable.Nil
import java.lang.StackWalker.StackFrame
import scala.util.control.NonFatal
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import cats.instances.long
import scala.util.Random
import cats.data.NonEmptyList
import cats.data.Chain
import cats.Monad

// Status = Pending | In Progress (percentage: Int) | Done (at: Instant)

sealed trait Status extends Product with Serializable {
  import Status._

  def fold[A](pending: => A, inProgress: Int => A, done: Instant => A): A = this match {
    case Pending                => pending
    case InProgress(percentage) => inProgress(percentage)
    case Done(at)               => done(at)
  }
}

object Status {
  case object Pending extends Status
  final case class InProgress(percentage: Int) extends Status
  final case class Done(at: Instant) extends Status
}

//Content = Video (length: FiniteDuration, link: String) | Playlist (elements: List[Content])
sealed trait Content extends Product with Serializable {

  def fold[A](video: (FiniteDuration, String) => A, playlist: NonEmptyList[A] => A): A = {
    case class StackFrame(results: Chain[A], pendingWork: List[Content]) {
      def addResult(result: A): StackFrame = copy(results = results.append(result))

      def takeWork: Option[(Content, StackFrame)] =
        pendingWork match {
          case head :: tail => (head, StackFrame(results, tail)).some
          case Nil          => None
        }
    }

    case class Stack(frames: NonEmptyList[StackFrame]) {
      def pop: (StackFrame, Option[Stack]) = (frames.head, frames.tail.toNel.map(Stack(_)))

      def push(frame: StackFrame): Stack = Stack(frame :: frames)
      def pushAll(another: Stack): Stack = Stack(another.frames.concatNel(frames))
    }

    object Stack {
      def pushOrStart(stack: Option[Stack]): StackFrame => Stack =
        frame => stack.fold(Stack(NonEmptyList.one(frame)))(_.push(frame))
    }

    //Process the current frame.
    //This can return up to 2 frames:
    // - a new frame, if the next unit of work is a playlist
    // - the top frame with some potential changes (or missing, if there's no more work in it)
    def step(topFrame: StackFrame): Either[Stack, A] =
      topFrame.takeWork match {
        case Some((work, topFrameRemaining)) =>
          val appliedWork = work match {
            case Playlist(elements) =>
              val newFrame = StackFrame(Chain.nil, elements.toList)

              //re-push the top frame, but without the current work.
              NonEmptyList.of(newFrame, topFrameRemaining)

            case Video(len, link) =>
              val newValue = video(len, link)

              NonEmptyList.one(topFrameRemaining.addResult(newValue))
          }

          Stack(appliedWork).asLeft

        case None =>
          //no more work - we can apply the results of the current frame.
          //this is safe, because a frame must have at least one of (pending work, results).
          val appliedFrame = playlist(topFrame.results.toList.toNel.get)

          appliedFrame.asRight
      }

    //Process a single step on the stack.
    def stepStack(stack: Stack): Either[Stack, A] = {
      val (top, rest) = stack.pop

      step(top) match {
        case Left(frames) => rest.fold(frames)(_.pushAll(frames)).asLeft
        case Right(value) =>
          rest.map(_.pop) match {
            //no more stack, no more work, done
            case None => value.asRight

            //there's more stack - we'll add the current result to the top frame
            case Some((nextFrame, restOfRest)) => Stack.pushOrStart(restOfRest)(nextFrame.addResult(value)).asLeft
          }
      }
    }

    this match {
      case Video(len, link) => video(len, link)
      case Playlist(elements) =>
        val initialStack = Stack(NonEmptyList.one(StackFrame(Chain.nil, elements.toList)))
        Monad[cats.Id].tailRecM(initialStack) { stepStack }
    }
  }

  def foldNaive[A](video: (FiniteDuration, String) => A, playlist: NonEmptyList[A] => A): A = this match {
    case Playlist(elements)  => playlist(elements.map(_.foldNaive(video, playlist)))
    case Video(length, link) => video(length, link)
  }
}

object Content {
  final case class Video(length: FiniteDuration, link: String) extends Content
  final case class Playlist(elements: NonEmptyList[Content]) extends Content

  val playlistCount: Content => Int = _.fold[Int]((_, _) => 0, 1 + _.reduce)
  val playlistCountNaive: Content => Int = _.foldNaive[Int]((_, _) => 0, 1 + _.reduce)

  val totalLength: Content => FiniteDuration = _.fold[FiniteDuration]((len, _) => len, _.foldLeft(Duration.Zero)(_ + _))
  val totalLengthNaive: Content => FiniteDuration = _.foldNaive[FiniteDuration]((len, _) => len, _.reduceLeft(_ + _))

  val links: Content => NonEmptyList[String] = _.fold[NonEmptyList[String]]((_, link) => NonEmptyList.one(link), _.flatten)
  val linksNaive: Content => NonEmptyList[String] = _.foldNaive[NonEmptyList[String]]((_, link) => NonEmptyList.one(link), _.flatten)
}

object Demo extends App {
  import Content._
  import scala.concurrent.duration._

  def vid(n: Int) = Video(1.seconds, "link:" + n.toString())

  val longList = Playlist(
    NonEmptyList.of(
      Playlist(
        NonEmptyList.of(
          vid(0),
          vid(-1)
        )
      ),
      vid(1),
      Playlist(
        NonEmptyList.of(
          vid(2),
          Playlist(NonEmptyList.of(vid(3))),
          vid(4)
        )
      ),
      vid(5)
    )
  )

  def arbitraryPlaylist(depth: Int, frameSize: Int): Content =
    if (depth < 1) vid(0)
    else if (Random.nextInt() % 5 != 0) {
      Playlist {
        NonEmptyList.fromListUnsafe(List.fill(frameSize)(arbitraryPlaylist(depth - 1, frameSize)))
      }
    } else vid(Random.nextInt())

  // println(arbitraryPlaylist(5, 3))

  List.fill(1000)(arbitraryPlaylist(5, 5)).foreach { list =>
    val left = list.fold(Video, Playlist)
    val right = list

    val isSame = left == right
    if (!isSame) println("wasn't equal for playlist: " + list + s", left: $left, right: $right")
  }

  // println(playlistCount(longList))
}
