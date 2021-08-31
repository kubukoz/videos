package demo

import cats.Show
import cats.effect.IO

enum Button {
  case X, Y, Z
}

trait ComboDSL[Combo] {
  def press(button: Button): Combo
  def hold(button: Button): Combo
  def both(one: Combo, another: Combo): Combo
  def oneOf(one: Combo, another: Combo): Combo
  def sequence(one: Combo, another: Combo): Combo

  def demo =
    sequence(
      both(
        press(Button.X),
        oneOf(
          hold(Button.Y),
          press(Button.Z),
        ),
      ),
      press(Button.X),
    )

}

enum Combo {
  case A
}

def demo[Combo](implicit dsl: ComboDSL[Combo]): Combo = ???

trait Security[F[_], Role: Show] {
  def requireRole[A](role: Role)(fa: F[A]): F[A]
  def giveRole[A](role: Role)(fa: F[A]): F[A]

  def admin: Role

  import cats.implicits._

  def demo[A](fa: F[A]): F[A] = {
    admin.show
    giveRole(admin) {
      requireRole(admin) {
        fa
      }
    }
  }

}

enum Role {
  case Admin
  case User
}

trait UserRepository[UserId, User] {
  def findUser(id: UserId): IO[Option[User]]
  def createUser(user: User): IO[Unit]
}
