package com.example

import cats.effect.Sync
import scala.concurrent.duration._

import cats.implicits._
import cats.effect.Async
import cats.effect.Concurrent
import cats.effect.Temporal
import cats.effect.std

object Commons {
  import Text._

  def process[F[_]: Temporal: std.Console]: F[Nothing] =
    (
      Temporal[F].sleep(100.millis) *>
        std.Console[F].println("Running".backgroundMessage)
    ).foreverM

  def consumer[F[_]: Temporal](processMessage: Message => F[Unit]): F[Nothing] = {
    def go(round: Int): F[Nothing] = {
      val msg = new Message {
        def text: String = "Message " + round
      }

      Temporal[F].sleep(100.millis) *> processMessage(msg) >> [Nothing] go(round + 1)
    }

    go(1)
  }

  trait Message {
    def text: String
  }

}
