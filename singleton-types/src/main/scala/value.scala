object value {
  val greeting: String = "hello"

  val anotherGreeting: greeting.type = greeting

  def fun(param: Any): param.type = param

  val result: Int = fun(42)
}
