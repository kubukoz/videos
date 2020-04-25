package com.example

import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.IO

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    IO(println("Hello")).as(ExitCode.Success)
}

