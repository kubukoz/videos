package base

@main def base = {
  val first = types.Item.fromString("42")

  println(first)
  // println(first.toTry.get + 5)

  println(types.Item.fromString("abc"))

}

object types {
  opaque type Item = Long

  object Item {

    def fromString(s: String): Either[Throwable, Item] = s
      .toLongOption
      .toRight(new Exception(s"Not a valid item: $s"))

  }

  opaque type NonNegInt = Int

  object NonNegInt {

    def fromInt(i: Int): Either[Throwable, NonNegInt] =
      if (i >= 0)
        Right(i)
      else
        Left(new Exception(s"Negative integer: $i"))

  }

}
