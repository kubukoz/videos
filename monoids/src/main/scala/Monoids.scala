import cats.kernel.Monoid
import cats.data.NonEmptyList

object Monoids {

  def intAddition() = {
    println((1 + 2) + 3)
    println((1 + 2) + 3 + 0)
  }

  def intMonoid() = {
    import cats.implicits._
    println((1 |+| 2) |+| 3)
    println((1 |+| 2) |+| 3 |+| Monoid[Int].empty)
  }

  def stringMonoid() = {
    import cats.implicits._
    println("foo" |+| "bar" |+| Monoid[String].empty)
  }

  def productMonoidInt() = {
    implicit val intMonoidProduct: Monoid[Int] = new Monoid[Int] {
      def combine(x: Int, y: Int): Int = x * y
      val empty: Int = 1
    }

    import cats.syntax.all._

    println(10 |+| 5) //50
  }

  def combineAllList[T: Monoid](list: List[T]): T = {
    import cats.implicits._

    list.combineAll
  }

  def mapAndThenCombine() = {
    //using Numeric
    println(List("foo", "bar", "foobar", "helloworld").map(_.length).sum)

    //using Monoid
    //works with anything that has a Monoid instance, not just numbers!
    import cats.implicits._
    println(List("foo", "bar", "foobar", "helloworld").foldMap(_.length))
  }

  def mapMonoid() = {
    val maps = List(
      Map("a" -> List(1, 2, 3)),
      Map("b" -> List(4, 5, 6)),
      Map("a" -> List(2, 3, 4), "b" -> List(9, 10, 11), "c" -> List(4, 5, 6))
    )

    import cats.implicits._

    //combines values under the same key using their Semigroup instance
    println(maps.combineAll)
  }

  def mapMonoidGroupByLength() = {
    val strings = List(
      "foo",
      "bar",
      "hello",
      "a"
    )

    import cats.implicits._

    println {
      strings.foldMap { s =>
        Map(s.length -> NonEmptyList.one(s))
      }
    }
  }

  def mapMonoidCountByValue() = {
    val strings = List(
      "foo",
      "foo",
      "bar",
      "hello",
      "foo",
      "a"
    )

    import cats.implicits._

    println {
      //uses the Int semigroup
      strings.foldMap { s =>
        Map(s -> 1)
      }
    }
  }

  def main(args: Array[String]): Unit = {
    import cats.implicits._

    intAddition()
    intMonoid()
    stringMonoid()
    productMonoidInt()
    println(combineAllList((1 to 100).toList))
    mapAndThenCombine()
    mapMonoid()
    mapMonoidGroupByLength()
    mapMonoidCountByValue()
  }

}
