package smithy4sdemo

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import hello.CityId
import hello.CreateCityOutput
import hello.GetWeatherOutput
import hello.WeatherService
import org.http4s.HttpRoutes
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import smithy4s.http4s.SimpleRestJsonBuilder

object ServerMain extends IOApp.Simple {

  val impl: WeatherService[IO] =
    new WeatherService[IO] {

      def getWeather(
        cityId: CityId
      ): IO[GetWeatherOutput] =
        IO.println(s"getWeather($cityId)") *>
          IO.pure(GetWeatherOutput("Good weather", Some(40)))

      def createCity(
        city: String,
        country: String,
      ): IO[CreateCityOutput] =
        IO.println(s"createCity($city, $country)") *>
          IO.pure(CreateCityOutput(CityId("123")))

    }

  def run: IO[Unit] =
    SimpleRestJsonBuilder
      .routes(impl)
      .resource
      .flatMap { routes =>
        EmberServerBuilder
          .default[IO]
          .withHttpApp(routes.orNotFound)
          .build
      }
      .evalMap(srv => IO.println(srv.addressIp4s))
      .useForever

}

object ClientMain extends IOApp.Simple {

  def run: IO[Unit] = EmberClientBuilder.default[IO].build.use { c =>
    SimpleRestJsonBuilder(WeatherService).client(c).resource.use { client =>
      client
        .createCity("London", "UK")
        .flatMap { output =>
          client.getWeather(output.cityId)
        }
        .flatMap(IO.println(_))
    }
  }

}
