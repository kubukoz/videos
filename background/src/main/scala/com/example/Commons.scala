package com.example

import cats.effect.Sync
import cats.effect.Timer
import scala.concurrent.duration._

import cats.effect.concurrent._
import cats.implicits._
import cats.effect.Async
import cats.effect.Concurrent

object Commons {
  import Text._

  def process[F[_]: Sync: Timer]: F[Nothing] =
    (
      Timer[F].sleep(100.millis) *>
        Sync[F].delay(println("Running".backgroundMessage))
    ).foreverM

  def consumer[F[_]: Concurrent: Timer](processMessage: Message => F[Unit]): F[Nothing] = {
    def go(round: Int): F[Nothing] = {
      val msg = new Message {
        def text: String = "Message " + round
      }

      Timer[F].sleep(100.millis) *> processMessage(msg) >> [Nothing] go(round + 1)
    }

    go(1)
  }

  trait Message {
    def text: String
  }
}
