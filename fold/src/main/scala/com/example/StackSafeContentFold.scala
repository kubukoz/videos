package com.example

import scala.concurrent.duration.FiniteDuration
import cats.data.NonEmptyList
import cats.Monad
import cats.data.Chain
import cats.implicits._

object StackSafeContentFold {

  import com.example.Content._

  def fold[A](video: (FiniteDuration, String) => A, playlist: NonEmptyList[A] => A): Content => A = {
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

    {
      case Video(len, link) => video(len, link)
      case Playlist(elements) =>
        val initialStack = Stack(NonEmptyList.one(StackFrame(Chain.nil, elements.toList)))
        Monad[cats.Id].tailRecM(initialStack) { stepStack }
    }
  }
}
