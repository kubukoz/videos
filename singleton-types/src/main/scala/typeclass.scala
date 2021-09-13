object typeclass {

  def getSingleton[A: ValueOf]: A =
    // builtin in Predef
    valueOf[A]

  val a = getSingleton[typeclass.type]
  val b = getSingleton[a.type]

  implicitly[a.type =:= b.type]

  val c = getSingleton["hello"]
  val d = getSingleton[42]
  val e = getSingleton[false]

  // make any parameter "effectively implicit"!
  def foo(a: Any): a.type =
    another[a.type]

  def another[A](implicit a: ValueOf[A]): A = a.value
}
