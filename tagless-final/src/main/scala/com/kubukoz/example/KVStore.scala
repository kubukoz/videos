package com.kubukoz.example

trait KVStore[F[_], K, V] {
  def get(k: K): F[Option[V]]
  def write(k: K, v: V): F[Unit]
  def delete(k: K): F[Unit]
}

object KVStore {
  import cats.mtl.MonadState

  def inMemoryStateBased[F[_], K, V](implicit S: MonadState[F, Map[K, V]]): KVStore[F, K, V] = new KVStore[F, K, V] {
    def write(k: K, v: V): F[Unit] = S.modify(_ + (k -> v))
    def get(k: K): F[Option[V]] = S.inspect(_.get(k))
    def delete(k: K): F[Unit] = S.modify(_ - k)
  }

  import dev.profunktor.redis4cats.algebra._

  def redisImpl[F[_], K, V](implicit commands: StringCommands[F, K, V], del: KeyCommands[F, K]): KVStore[F, K, V] =
    new KVStore[F, K, V] {
      def write(k: K, v: V): F[Unit] = commands.set(k, v)
      def get(k: K): F[Option[V]] = commands.get(k)
      def delete(k: K): F[Unit] = del.del(k)
    }
}

final class KVStoreLaws[F[_]: Monad, K: Eq, V](store: KVStore[F, K, V]) {
  import cats.laws._
  import store._

  // writes are persisted
  def writeGetPersistence(k: K, v: V) =
    write(k, v) *> get(k) <->
      write(k, v) *> v.some.pure[F]

  // get doesn't mutate key
  def getIdempotence(k: K, v: V) =
    write(k, v) *> get(k) *> get(k) <-> write(k, v) *> get(k)

  // latest write to same key wins
  def latestOverwriteWins(k: K, vOld: V, v: V) =
    write(k, vOld) *> write(k, v) *> get(k) <->
      write(k, v) *> get(k)

  // delete drops key
  def deleteRemoves(k: K, v: V) =
    write(k, v) *> delete(k) *> get(k) <->
      none[V].pure[F]

  // it's possible to write again after delete
  def canWriteAfterDelete(k: K, v: V, v2: V) =
    write(k, v) *> delete(k) *> write(k, v2) *> get(k) <->
      write(k, v2) *> get(k)

  // writes to different keys are independent
  def writeIndependence(k1: K, v1: V, k2: K, v2: V) = (k1 neqv k2) ==> {
    write(k1, v1) *> write(k2, v2) *> (get(k1), get(k2)).tupled <->
      write(k1, v1) *> write(k2, v2) *> (v1.some.pure[F], v2.some.pure[F]).tupled
  }

  // deleting key added first doesn't delete key added second
  def deleteIndependenceLeft(k1: K, v1: V, k2: K, v2: V) = (k1 neqv k2) ==> {
    write(k1, v1) *> write(k2, v2) *> delete(k1) *> get(k2) <->
      write(k1, v2) *> v2.some.pure[F]
  }

  // deleting key added second doesn't delete key added first
  def deleteIndependenceRight(k1: K, v1: V, k2: K, v2: V) = (k1 neqv k2) ==> {
    write(k1, v1) *> write(k2, v2) *> delete(k2) *> get(k1) <->
      write(k1, v1) *> v1.some.pure[F]
  }

  implicit class ConditionalLaw(cond: Boolean) {

    def ==>[A](law: IsEq[F[A]]): IsEq[Option[F[A]]] =
      if (cond) mapResult(law)(_.some)
      else IsEq(None, None)

    private def mapResult[A, B](iseqA: IsEq[A])(f: A => B): IsEq[B] = IsEq(f(iseqA.lhs), f(iseqA.rhs))
  }

}
