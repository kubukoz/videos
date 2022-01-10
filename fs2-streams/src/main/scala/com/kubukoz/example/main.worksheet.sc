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
  .debug(tagged(": iterate"))
  .scan1(_ + _)
  .debug(tagged(": scan"))
  .dropWhile(_ < 1000)
  .debug(tagged(": drop"))
  .filter(_ % 3 == 0)
  .debug(tagged(": filter"))
  .map(_ * 2)
  .debug(tagged(": map"))
  .take(3)
  .debug(tagged(": take"))
  .showValues
