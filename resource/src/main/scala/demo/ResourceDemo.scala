package demo

import cats.effect.ExitCode

import cats.effect.IO
import cats.effect.IOApp
import java.io.File
import java.io.FileReader
import cats.effect.Blocker
import cats.implicits._
import cats.effect.ContextShift
import cats.effect.Sync
import cats.effect.Console.io.putStrLn
import cats.Applicative
import cats.effect.Console
import cats.Monad

object ResourceDemo extends IOApp {
  val file1 = new File("src/main/resources/example.txt")
  val file2 = new File("src/main/resources/example2.txt")

  override def run(args: List[String]): IO[ExitCode] = {
    val line: IO[String] = Blocker[IO].use { blocker =>
      val files = Files.fileSystem[IO](blocker)

      files.open(file1).bracket(files.read)(files.close)
    }

    line.flatMap(putStrLn)
  }.as(ExitCode.Success)
}

trait Files[F[_]] {
  def open(file: File): F[FileReader]
  def read(reader: FileReader): F[String]
  def close(reader: FileReader): F[Unit]
}

object Files {
  def apply[F[_]](implicit F: Files[F]): Files[F] = F

  def fileSystem[F[_]: ContextShift: Sync](blocker: Blocker): Files[F] = new Files[F] {

    def open(file: File): F[FileReader] = blocker.delay[F, FileReader] {
      println("Opening file reader")
      new FileReader(file)
    }

    def read(reader: FileReader): F[String] = blocker.delay[F, String] {
      val buffer = Array.fill[Char](4096)(0)
      reader.read(buffer)
      new String(buffer).trim
    }

    def close(reader: FileReader): F[Unit] = blocker.delay[F, Unit] {
      println("Closing file reader")
      reader.close()
    }
  }
}

trait BusinessService[F[_]] {
  def getBusinessData: F[List[String]]
}

object BusinessService {
  def apply[F[_]](implicit F: BusinessService[F]): BusinessService[F] = F

  def fromFiles[F[_]: Files: Applicative](readers: List[FileReader]): BusinessService[F] = new BusinessService[F] {
    val getBusinessData: F[List[String]] = readers.traverse(Files[F].read)
  }
}

trait App[F[_]] {
  def run: F[Unit]
}

object App {

  def instance[F[_]: BusinessService: Console: Monad]: App[F] = new App[F] {

    val run: F[Unit] = BusinessService[F]
      .getBusinessData
      .flatMap { results =>
        results.traverse(Console[F].putStrLn)
      }
      .void
  }
}
