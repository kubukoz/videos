import cats.data.State
import cats.implicits._

case class MyState(
  even: List[Int],
  odd: List[Int],
  div5: List[Int],
)

object MyState {
  val empty: MyState = MyState(Nil, Nil, Nil)
}

object Main extends App {

  def v1_step1(next: Int): MyState => MyState = s =>
    if (next % 2 == 0) s.copy(even = next :: s.even)
    else s.copy(odd = next :: s.odd)

  def v1_step2(next: Int): MyState => MyState = s =>
    if (next % 5 == 0)
      s.copy(div5 = next :: s.div5)
    else s

  val v1 = (
    1 to 10
  ).toList.foldLeft(MyState.empty) { (s, next) =>
    v1_step1(next)
      .andThen(v1_step2(next))
      .apply(s)
  }

  def v2_step1(next: Int): State[MyState, Unit] =
    if (next % 2 == 0)
      State.modify(s => s.copy(even = next :: s.even))
    else
      State.modify(s => s.copy(odd = next :: s.odd))

  def v2_step2(next: Int): State[MyState, Unit] =
    if (next % 5 == 0)
      State.modify(s => s.copy(div5 = next :: s.div5))
    else State.pure(())

  val v2 = (
    1 to 10
  ).toList
    .traverse_ { next =>
      v2_step1(next) *>
        v2_step2(next)
    }
    .runS(MyState.empty)
    .value

}
