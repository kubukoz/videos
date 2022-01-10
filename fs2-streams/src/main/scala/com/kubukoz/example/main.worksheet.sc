import java.io.PrintStream
//IO effect type
import cats.effect.IO

//syntax, e.g. mapN, *>
import cats.implicits._
import cats.effect.unsafe.implicits._

//.showValues syntax for safe stream running in worksheets
import com.kubukoz.example.SnippetRunner._

//core fs2 abstraction
import fs2.Stream

//print values effectfully
def putStrLn(s: Any): IO[Unit] = IO(println(s))

//
//
//
//
//
//
//

Stream.emit(42).showValues

Stream(1, 2, 4).showValues

Stream.eval(putStrLn("hello world")).showValues

Stream.emits(List(42, 48, 50)).showValues

val a = Stream.constant(10).take(5)

val b = Stream.iterate(1)(_ * 2).take(8)

(a ++ b).showValues

(a ++ b).take(7).showValues

(a ++ b)
  .take(7)
  .flatMap { e =>
    Stream.eval(putStrLn(e)).map(_ => e + 2).repeatN(2)
  }
  .showValues

Stream
  .iterate(1)(_ * 2)
  .debugged(": iterate")
  .scan1(_ + _)
  .debugged(": scan")
  .dropWhile(_ < 1000)
  .debugged(": drop")
  .filter(_ % 3 == 0)
  .debugged(": filter")
  .map(_ * 2)
  .debugged(": map")
  .take(3)
  .debugged(": take")
  .showValues
