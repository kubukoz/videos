package demo

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import java.io.File
import java.io.FileReader
import cats.effect.Blocker
import cats.implicits._
import cats.effect.ContextShift

object BracketDemo extends IOApp {
  val file = new File("src/main/resources/example.txt")

  override def run(args: List[String]): IO[ExitCode] = {
    val line: IO[String] = Blocker[IO].use { blocker =>
      import Files._
      open(file)(blocker).bracket { read(blocker) }(close(blocker))
    }

    line
      .flatMap { line =>
        IO { println(line) }
      }
      .as(ExitCode.Success)
  }
}

object Files {

  def open(file: File)(blocker: Blocker)(implicit cs: ContextShift[IO]): IO[FileReader] = blocker.delay[IO, FileReader] {
    println("Opening file reader")
    new FileReader(file)
  }

  def read(blocker: Blocker)(reader: FileReader)(implicit cs: ContextShift[IO]): IO[String] = blocker.delay[IO, String] {
    val buffer = Array.fill[Char](4096)(0)
    reader.read(buffer)
    new String(buffer).trim
  }

  def close(blocker: Blocker)(reader: FileReader)(implicit cs: ContextShift[IO]): IO[Unit] = blocker.delay[IO, Unit] {
    println("Closing file reader")
    reader.close()
  }
}
