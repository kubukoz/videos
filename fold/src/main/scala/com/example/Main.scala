package com.example

import java.time.Instant
import com.example.Status.Pending
import com.example.Status.InProgress
import com.example.Status.Done

import cats.Eval
import cats.data.NonEmptyList
import com.example.Content.Playlist
import com.example.Content.Video
import scala.concurrent.duration.FiniteDuration
import cats.implicits._
import scala.concurrent.duration.Duration
import scala.util.Random

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

  def foldEval[A](video: (FiniteDuration, String) => A, playlist: NonEmptyList[A] => A): A = {
    def go(self: Content): Eval[A] = self match {
      case Playlist(elements)  => elements.traverse(go).map(playlist)
      case Video(length, link) => Eval.now(video(length, link))
    }

    go(this).value
  }

  def foldNaive[A](video: (FiniteDuration, String) => A, playlist: NonEmptyList[A] => A): A = this match {
    case Playlist(elements)  => playlist(elements.map(_.foldNaive(video, playlist)))
    case Video(length, link) => video(length, link)
  }
}

object Content {
  final case class Video(length: FiniteDuration, link: String) extends Content
  final case class Playlist(elements: NonEmptyList[Content]) extends Content

  val playlistCount: Content => Int = StackSafeContentFold.fold[Int]((_, _) => 0, 1 + _.reduce)
  val playlistCountNaive: Content => Int = _.foldNaive[Int]((_, _) => 0, 1 + _.reduce)

  val totalLength: Content => FiniteDuration = StackSafeContentFold.fold[FiniteDuration]((len, _) => len, _.foldLeft(Duration.Zero)(_ + _))
  val totalLengthNaive: Content => FiniteDuration = _.foldNaive[FiniteDuration]((len, _) => len, _.reduceLeft(_ + _))

  val links: Content => NonEmptyList[String] =
    StackSafeContentFold.fold[NonEmptyList[String]]((_, link) => NonEmptyList.one(link), _.flatten)
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
    val left = StackSafeContentFold.fold(Video, Playlist)(list)
    val right = list

    val isSame = left == right
    if (!isSame) println("wasn't equal for playlist: " + list + s", left: $left, right: $right")
  }

  // println(playlistCount(longList))
}
