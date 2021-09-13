object obj {
  val ref: obj.type = obj
}

class Clazz {

  object child

  // won't compile
  // def f(another: Clazz): child.type = another.child

}
