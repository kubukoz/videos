import language.experimental.modularity

final case class Person(tracked val name: String)

object Main {

  def f(p: Person): p.name.type =
    p.name

  final val name = "John"

  final val p = Person(name)

  val x: "John" = f(p)

  val jk: Person("Jakub") = p.copy(name = "Jakub")

}

case class Keys(tracked val keys: List[String], extra: String)

def validate(keys: Keys): Either[Throwable, Keys { val keys: ::[String]}] = {
  keys.keys match {
    case Nil => Left(new IllegalArgumentException("Keys cannot be empty"))
    case head :: next => Right(keys.copy(keys = ::(head, next)))
  }
}

@main def run = {
  val initKeysForValidation = Keys(List("a", "b"), extra = "c")

  validate(initKeysForValidation).foreach { validatedKeys =>

    validatedKeys.keys match {
      case head :: next => println(s"definitely not empy! $head, $next")
    }
  }
}
