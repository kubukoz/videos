package kroptime.conf

import cats.effect.IO
import cats.effect.Resource
import krop.Mode
import krop.sqlite.Sqlite
import krop.sqlite.Transactor

// This contains all the dependencies your application needs to run
final case class Context(xa: Transactor)
object Context {
  // The development mode context
  val development: Resource[IO, Context] =
    Sqlite.create("./kroptime-development.sqlite3").map(xa => Context(xa))

  // The production mode context
  val production: Resource[IO, Context] =
    Sqlite.create("./kroptime-production.sqlite3").map(xa => Context(xa))

  // The context for the current mode
  val current: Resource[IO, Context] =
    Mode.mode match {
      case Mode.Development => development
      case Mode.Production  => production
    }
}
