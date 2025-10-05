package kroptime

import cats.effect.ExitCode
import cats.effect.IO
import com.monovore.decline.Opts
import com.monovore.decline.effect.CommandIOApp
import krop.all.{*, given}
import krop.tool.cli.*
import krop.BuildInfo

import kroptime.conf.Context
import kroptime.routes.Routes
import kroptime.views.html
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration.Duration
import com.augustnagro.magnum.*

val name = "krop-time"

object Main
    extends CommandIOApp(
      name = name,
      header = "An amazing web application built with Krop"
    ) {

  def home(ctx: Context) =
    Routes.home.handleIO { () =>
      ctx.xa
        .transact {
          sql"update visits set count = count + 1 returning count"
            .query[Int]
            .run()
            .head
        }
        .map { visits =>
          html
            .base(
              name,
              html.home(
                name = name,
                kropVersion = BuildInfo.kropVersion,
                visits = visits,
                todosLink = Routes.todos.pathTo
              )
            )
            .toString
        }
    }

  val assets =
    Routes.assets.passthrough

  def todos(ctx: Context) =
    Routes.todos.handleIO { () =>
      todosImpl(ctx)
    }

  private def todosImpl(ctx: Context) =
    ctx.xa
      .connect {
        sql"select name from todos"
          .query[String]
          .run()
      }
      .map { todos =>
        html
          .base(
            name,
            html.todos(todos.toList, Routes.newTodo.pathTo)
          )
          .toString
      }

  def newTodo(ctx: Context) = Routes.newTodo.handleIO { newTodo =>
    ctx.xa.connect {
      sql"insert into todos(name) values(${newTodo.name})".update.run()
    } *>
      IO.println(s"Inserted todo: ${newTodo.name}") *>
      todosImpl(ctx)
  }

  def application(ctx: Context) =
    home(ctx)
      .orElse(assets)
      .orElse(todos(ctx))
      .orElse(newTodo(ctx))
      .orElse(Routes.test.handleIO(_ => IO.stub))
      .orElse(Application.notFound)

  override def main: Opts[IO[ExitCode]] =
    (Cli.serveOpts.orElse(Cli.migrateOpts)).map {
      case Serve(port) =>
        Context.current.use { ctx =>
          ctx.xa.connect {
            sql"drop table if exists visits".update
              .run()
          } *>
            ctx.xa.connect {
              sql"create table visits (count integer)".update
                .run()
            } *>
            ctx.xa.connect {
              sql"insert into visits values(1)".update
                .run()
            } *>
            ctx.xa.connect {
              sql"drop table if exists todos".update
                .run()
            } *>
            ctx.xa.connect {
              sql"create table todos (name text)".update
                .run()
            } *>
            ctx.xa.connect {
              sql"insert into todos(name) values('foo'), ('bar')".update
                .run()
            } *>
            ServerBuilder.default
              .withApplication(application(ctx))
              .withPort(port)
              .unwrap
              .flatMap { _.withShutdownTimeout(Duration.Zero).build.useForever }
        }

      case Migrate() => ???
    }
}
