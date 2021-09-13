object literals {
  // before 2.13
  import shapeless.{Witness => W}

  val x_old: W.`"hello"`.T = "hello"

  // since 2.13
  val x: "hello" = "hello"

  // force narrow inferred type parameter with <: Singleton
  def fun[T <: Singleton](t: T): T = t

  // force narrow inferred value type with "final"
  final val z = fun(x)

  val check: "hello" = z

  implicitly[check.type =:= "hello"]
}
