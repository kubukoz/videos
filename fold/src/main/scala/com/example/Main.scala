package com.example

import scala.util.chaining._
import scala.collection.immutable.Nil
import scala.annotation.tailrec
import java.time.Instant
import com.example.Status.Pending
import com.example.Status.InProgress
import com.example.Status.Done
import scala.concurrent.duration.FiniteDuration
import cats.data.NonEmptyList
import com.example.Content.Video
import com.example.Content.Playlist

object Examples extends App {
  val o: Option[Int] = Some(42)

  //Option[A] = None | Some[A](a: A)

  // println(o.fold("a")(_.toString()))

  // val l: List[Int] = ???

  //List[A] = Nil | A :: List[A]

  def sum(l: List[Int]): Int = l match {
    case Nil          => 0
    case head :: tail => head + sum(tail)
  }

  def sumFold(l: List[Int]) = l.foldRight(0)(_ + _)

  println(foldRightList(1 :: 2 :: 3 :: Nil)(List.empty[Int])(_ :: _))

  @tailrec
  def foldLeftList[A, B](list: List[A])(nil: B)(cons: (B, A) => B): B = list match {
    case head :: tail => foldLeftList(tail)(cons(nil, head))(cons)
    case Nil          => nil
  }

  def foldRightList[A, B](list: List[A])(nil: B)(cons: (A, B) => B): B = list match {
    case head :: tail => cons(head, foldRightList(tail)(nil)(cons))
    case Nil          => nil
  }

}

//
//
//
//
//
//
//
// Status = Pending | In Progress (percentage: Int) | Done (at: Instant)
sealed trait Status extends Product with Serializable {

  def fold[A](pending: => A)(inProgress: Int => A)(done: Instant => A): A = this match {
    case Pending                => pending
    case InProgress(percentage) => inProgress(percentage)
    case Done(at)               => done(at)
  }
}

object Status {
  case object Pending extends Status
  final case class InProgress(percentage: Int) extends Status
  final case class Done(at: Instant) extends Status

  val completion: Status => Int =
    _.fold(pending = 0)(inProgress = identity)(done = _ => 100)
}

// Content = Video (length: FiniteDuration, link: String) | Playlist (elements: NonEmptyList[Content])
sealed trait Content extends Product with Serializable {

  def fold[A](video: (FiniteDuration, String) => A)(playlist: NonEmptyList[A] => A): A = this match {
    case Video(length, link) => video(length, link)
    case Playlist(elements)  => playlist(elements.map(_.fold(video)(playlist)))
  }
}

object Content {
  final case class Video(length: FiniteDuration, link: String) extends Content
  final case class Playlist(elements: NonEmptyList[Content]) extends Content

  val playlistCount: Content => Int = _.fold((_, _) => 0)(1 + _.reduceLeft(_ + _))
  val length: Content => FiniteDuration = _.fold((d, _) => d)(_.reduceLeft(_ + _))
}

//Message[A] = NoContent | Content (s: String, payload: A, parent: Option[Message[A]])
sealed trait Message[+A] extends Product with Serializable {

  def fold[B](noContent: => B)(content: (String, A, Option[B]) => B): B = this match {
    case Message.NoContent                   => noContent
    case Message.Content(s, payload, parent) => content(s, payload, parent.map(_.fold(noContent)(content)))
  }
}

object Message {
  case object NoContent extends Message[Nothing]
  final case class Content[+A](s: String, payload: A, parent: Option[Message[A]]) extends Message[A]
}
