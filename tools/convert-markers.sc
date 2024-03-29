//> using scala "3.2.2"

import scala.io.Codec
import scala.io.Source
import scala.util.Using

case class Timestamp(h: String, m: String, s: String, frames: String)

case class Marker(
    name: String,
    in: Timestamp,
    tpe: String
) {
  def renderOut: String = {
    s"${in.h}:${in.m}:${in.s} - $name"
  }
}

def parseTimestamp(s: String): Timestamp = {
  val parts = s.split(":")
  Timestamp(parts(0), parts(1), parts(2), parts(3))
}

Using(
  Source.fromFile(
    args.headOption.getOrElse(sys.error("No file passed to args"))
  )(Codec("UTF-16"))
) {
  _.getLines().toList.tail.filterNot(_.trim.isEmpty).map { line =>
    val name :: _ :: in :: _ :: _ :: tpe :: Nil =
      line.split("\\t").toList

    Marker(name, parseTimestamp(in), tpe)
  }

}.get
  .filter(_.tpe == "Chapter")
  .map(_.renderOut)
  .foreach(println)
