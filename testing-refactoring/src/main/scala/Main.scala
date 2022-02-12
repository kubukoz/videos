import cats.effect.IO
import cats.effect.IOApp
import cats.implicits._
import io.circe.Codec
import org.http4s.Method._
import org.http4s.Request
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.implicits._
import org.http4s.headers.Authorization
import org.http4s.Credentials
import org.http4s.AuthScheme

object Main extends IOApp.Simple {

  case class Repository(name: String, stargazers_count: Int) derives Codec.AsObject

  val run: IO[Unit] = EmberClientBuilder.default[IO].build.use { client =>
    // let's just assume it's going to be 3 pages because that's what works for my profile :)
    (1 to 3)
      .toList
      .parFlatTraverse { page =>
        client
          .expect[List[Repository]](
            GET {
              uri"https://api.github.com/users"
                / System.getenv("GITHUB_USER")
                / "repos"
                +? ("page", page)
                +? ("per_page", 100)
            }.putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, System.getenv("GITHUB_TOKEN"))))
          )
      }
      .flatMap {
        _.sortBy(_.stargazers_count)
          .reverse
          .take(10)
          .zipWithIndex
          .map { case (repo, index) =>
            s"${index + 1}. ${repo.name} (${repo.stargazers_count} stars)"
          }
          .traverse_(IO.println(_))
      }
  }

}
