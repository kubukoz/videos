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

  def fold[A](video: (FiniteDuration, String) => A, playlist: List[A] => A): A = {
    case class StackFrame(before: Chain[A], after: List[Content]) {
      def addResult(result: A): StackFrame = copy(before = before.append(result))

      def takeWork: Option[(Content, StackFrame)] = after match {
        case work :: moreWork => Some((work, copy(after = moreWork)))
        case _                => None
      }
    }

    case class Stack(frames: NonEmptyList[StackFrame]) {
      def pop: (StackFrame, Option[Stack]) = (frames.head, frames.tail.toNel.map(Stack(_)))

      def push(frame: StackFrame): Stack = Stack(frame :: frames)

      //Provides the given value to the top frame in the stack.
      //The result is NOT further analyzed to remove completed nodes.
      def provide(value: A): Stack = {
        val (topFrame, rest) = pop

        Stack.pushOrStart(rest)(topFrame.addResult(value))
      }
    }

    object Stack {
      def pushOrStart(stack: Option[Stack])(frame: StackFrame): Stack = stack.fold(Stack(NonEmptyList.one(frame)))(_.push(frame))
    }

    //unwinds the stack until a frame is found that's not complete, or until everything is complete and we have an A.
    @tailrec
    def unwind(stack: Stack): Either[Stack, A] = {
      val (topFrame, restOfStack) = stack.pop

      topFrame.after match {
        //no more work in this frame - applying!
        //after flattening the enclosing stack frame, we need to provide it as a value to the parents
        case Nil =>
          val finalizedNode = playlist(topFrame.before.toList)

          restOfStack match {
            //we used the top frame, that's it
            case None => Right(finalizedNode)
            //there's more stack - we need to provide this frame's value as the result of the work in the frame above, and try to unwind again
            case Some(moreStack) => unwind(moreStack.provide(finalizedNode))
          }

        //there's more work in this frame - returning the entire stack unchanged
        case _ => Left(stack)
      }
    }

    @tailrec
    def go(work: Content, stack: Option[Stack]): A = work match {
      case Video(length, link) =>
        val newValue = video(length, link)

        stack match {
          //we're the top node - just returning the value (the root was a video)
          case None => newValue
          case Some(stack) =>
            unwind(stack.provide(newValue)) match {
              case Left(moreStack) =>
                moreStack.pop match {
                  case (frame, moreStack) =>
                    frame.takeWork match {
                      case None =>
                        //no more work, we're okay I guess
                        ???
                      case Some((work, restInFrame)) =>
                        go(work, Some(Stack.pushOrStart(moreStack)(restInFrame)))
                    }
                }
              case Right(value) => value
            }
        }

      case Playlist(elements) =>
        val first :: more = elements

        go(first, Some(Stack.pushOrStart(stack)(StackFrame(Chain.nil, more))))
    }

    go(this, None)
  }

  def foldNaive[A](video: (FiniteDuration, String) => A, playlist: List[A] => A): A = this match {
    case Playlist(elements)  => playlist(elements.map(_.foldNaive(video, playlist)))
    case Video(length, link) => video(length, link)
  }
}

object Content {
  final case class Video(length: FiniteDuration, link: String) extends Content
  final case class Playlist(elements: List[Content]) extends Content

  val playlistCount: Content => Int = _.fold[Int]((_, _) => 0, 1 + _.sum)
  val playlistCountNaive: Content => Int = _.foldNaive[Int]((_, _) => 0, 1 + _.sum)

  val totalLength: Content => FiniteDuration = _.fold[FiniteDuration]((len, _) => len, _.foldLeft(Duration.Zero)(_ + _))
  val totalLengthNaive: Content => FiniteDuration = _.foldNaive[FiniteDuration]((len, _) => len, _.foldLeft(Duration.Zero)(_ + _))

  val links: Content => List[String] = _.fold[List[String]]((_, link) => List(link), _.flatten)
  val linksNaive: Content => List[String] = _.foldNaive[List[String]]((_, link) => List(link), _.flatten)
}

object Demo extends App {
  import Content._
  import scala.concurrent.duration._

  def vid(n: Int) = Video(1.seconds, "link:" + n.toString())
  val longList = Playlist(List(vid(0), vid(1), Playlist(List(vid(2), Playlist(List(vid(3))), vid(4))), vid(5)))

  def arbitraryPlaylist(depth: Int, frameSize: Int): Content =
    if (depth < 1) vid(0)
    else if (Random.nextInt() % 5 != 0) {
      Playlist {
        List.fill(frameSize)(arbitraryPlaylist(depth - 1, frameSize))
      }
    } else vid(Random.nextInt())

  // println(arbitraryPlaylist(5, 3))

  List.fill(1000)(arbitraryPlaylist(5, 5)).foreach { list =>
    val left = (playlistCount(list), links(list), totalLength(list))
    val right =
      (playlistCountNaive(list), linksNaive(list), totalLengthNaive(list))

    val isSame = left == right
    if (!isSame) println("wasn't equal for playlist: " + list + s", left: $left, right: $right")
  }
}
