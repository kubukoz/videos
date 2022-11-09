object main extends App {

  object <<<<<<< {
    def HEAD(i: Int): Int = i
  }

  implicit class StringGitOps(s: String) {
    def =======(i: Int): Int = i
    def >>>>>>>(i: Int): Int = i
  }

  val git0 = 40

  println(
<<<<<<< HEAD
"hello!"
=======
"world"
>>>>>>> git0
  )

  println(
    <<<<<<<.HEAD("hello!".=======("world".>>>>>>>(git0)))
  )
}
