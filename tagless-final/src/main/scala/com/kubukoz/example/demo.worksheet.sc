trait Expr[A] {
  //+
  def add(a: A, b: A): A
  def zero: A
  def one: A
}

//zero + anything = anything
//anything + zero = anything

def program[A](expr: Expr[A]): A = {
  import expr._

  add(
    add(
      zero,
      one
    ),
    one
  )
}

object IntExpr extends Expr[Int] {
  def add(a: Int, b: Int): Int = a + b
  val zero: Int = 0
  val one: Int = 1
}

program(IntExpr)
