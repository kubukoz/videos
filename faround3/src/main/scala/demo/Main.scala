final case class Person(name: String)

object Main {

  def f(p: Person): p.name.type =
    p.name

  val p: Person = Person("John")

  val x: p.name.type = f(p)

}
