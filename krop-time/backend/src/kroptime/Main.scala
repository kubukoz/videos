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

val name = "krop-time"

object Main
    extends CommandIOApp(
      name = name,
      header = "An amazing web application built with Krop"
    ) {

  val home =
    Routes.home.handle(() => html.base(name, html.home(name, BuildInfo.kropVersion)).toString)

  val assets =
    Routes.assets.passthrough

  val application =
    home.orElse(assets).orElse(Application.notFound)

  override def main: Opts[IO[ExitCode]] =
    (Cli.serveOpts.orElse(Cli.migrateOpts)).map {
      case Serve(port) =>
        Context.current.use { _ =>
          ServerBuilder.default
            .withApplication(application)
            .withPort(port)
            .build
            .toIO
            .as(ExitCode.Success)
        }

      case Migrate() => ???
    }
}
