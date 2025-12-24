package refs_extensible

import cats.effect.IO
import cats.effect.IOApp

object refs_extensible extends IOApp.Simple {

  trait Logger {
    def log(s: String): IO[Unit]
  }

  trait Connection {
    def listUsers(): IO[List[String]]
  }

  val db: Connection = ???
  val logger: Logger = ???

  def build(): IO[History] = History.build {

    def go: History.Builder[Unit] = db.listUsers().flatMap { users =>
      History.length.flatMap {
        case i if i >= users.size =>
          History.print("done") *> logger.log("Finished processing users")
        case i => History.print(s"appending ${i}") *> History.append(i) *> go
      }

    }
    go
  }

  def run: IO[Unit] = build().debug().void

}

case class History private (log: Vector[Int]) {
  private[History] def append(i: Int): History = copy(log = log.appended(i))
}

object History {

  trait HistoryOps {
    def length: IO[Int]
    def append(i: Int): IO[Unit]
    def print(s: String): IO[Unit]
  }

  type Builder[A] = HistoryOps ?=> IO[A]

  // no monad!

  val length: Builder[Int] = summon[HistoryOps].length

  def print(s: String): Builder[Unit] = summon[HistoryOps].print(s)
  def append(i: Int): Builder[Unit] = summon[HistoryOps].append(i)

  def build(b: Builder[Unit]): IO[History] = IO
    .ref(History(Vector.empty))
    .flatMap { ref =>
      b(
        using new HistoryOps {
          def length: IO[Int] = ref.get.map(_.log.size)
          def append(i: Int): IO[Unit] = ref.update(history => history.append(i))
          def print(s: String): IO[Unit] = IO.println(s)
        }
      ) *> ref.get
    }

}
