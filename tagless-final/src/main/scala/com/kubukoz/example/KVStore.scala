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

final class KVStoreLaws[F[_]: Monad, K, V](store: KVStore[F, K, V]) {
  import cats.laws._
  import store._

  def writeGetPersistence(k: K, v: V) =
    write(k, v) *> get(k) <->
      write(k, v) *> v.some.pure[F]

  def getIdempotence(k: K, v: V) =
    write(k, v) *> get(k) *> get(k) <-> write(k, v) *> get(k)

  def latestOverwriteWins(k: K, vOld: V, v: V) =
    write(k, vOld) *> write(k, v) *> get(k) <->
      write(k, v) *> get(k)

  def deleteRemoves(k: K, v: V) =
    write(k, v) *> delete(k) *> get(k) <->
      none[V].pure[F]

  def canWriteAfterDelete(k: K, v: V, v2: V) =
    write(k, v) *> delete(k) *> write(k, v2) *> get(k) <->
      write(k, v2) *> get(k)
}
