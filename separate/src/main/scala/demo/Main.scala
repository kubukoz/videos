import cats.implicits._
import cats.Alternative
import cats.data.Chain

object Main extends App {

  val l: List[Either[String, Int]] = List(
    "error 1".asLeft,
    2.asRight,
    "error 3".asLeft,
  )

  val tups: List[(String, Int)] = List(
    ("e1", 1),
    ("e2", 3),
    ("e3", 5),
  )

  println(l.separateFoldable)
  println(tups.separateFoldable)

  def demo[F[_]: Alternative]: F[Int] =
    Alternative[F].pure(42) <+> Alternative[F].pure(10) <+> Alternative[F].empty

  println(demo[List])
  println(demo[Vector])
  println(demo[Chain])
  println(demo[Option])
}
