package com.example

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.std.Supervisor
import scala.concurrent.duration._

object CESupervisor extends IOApp.Simple {

  val program = {
    IO.sleep(1.second) *> IO.println("done!")
  }
    .guaranteeCase(IO.println(_))

  def run: IO[Unit] = Supervisor[IO]
    .use { sup =>
      sup
        .supervise(program)
        .flatMap { fiber =>
          fiber.join
        }
        .timeout(500.millis)
    }
    .guaranteeCase(oc => IO.println(s"oc: $oc"))
    .void

}
