object unions {
  val a = "hello"
  val b = "world"

  val result: a.type | b.type =
    if (true) a
    else b

}
