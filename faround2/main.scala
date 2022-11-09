object main extends App {

  object <<<<<<< {
    def HEAD(a: Int) = ???
  }

  implicit class StringOpsGit(s: String) {
    def =======(s2: String) = ???
    def >>>>>>>(s2: String) = ???
  }

  val git1 = "foo"

  println(
<<<<<<< HEAD
"hello!"
=======
"world"
>>>>>>> git1
  )

  // desugared
  println(
    <<<<<<<.HEAD("hello!".=======("world".>>>>>>>(git1)))
  )
}
