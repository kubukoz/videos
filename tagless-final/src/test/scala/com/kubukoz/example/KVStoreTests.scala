package com.kubukoz.example

import org.typelevel.discipline.Laws
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import cats.laws.discipline._
import cats.laws.discipline.arbitrary._
import cats.data.State
import cats.mtl.instances.all._

import scala.util.Try
import dev.profunktor.redis4cats.connection.RedisClient

import scala.concurrent.ExecutionContext
import dev.profunktor.redis4cats.connection.RedisURI
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.domain.RedisCodec
import cats.effect.laws.util.TestInstances._
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import dev.profunktor.redis4cats.algebra.RedisCommands
import io.chrisdavenport.log4cats.noop.NoOpLogger
import cats.tests.CatsSuite

class KVStoreInMemoryTests extends CatsSuite {
  import TestUtils._

  checkAll("KVStore(MonadState)", KVStoreTests[State[Map[MiniInt, MiniInt], ?], MiniInt, MiniInt].kvstore)
}

class KVStoreRedisTests extends CatsSuite with RedisSuite {
  import TestUtils._

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =q checkConfiguration.copy(workers = 1)

  implicit val redisClient = client

  implicit val arbitraryString = Arbitrary(Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString))
  implicit val store = KVStore.redisImpl[IO, String, String]

  checkAll("KVStore(redis)", KVStoreTests[IO, String, String].kvstore)
}

trait KVStoreTests[F[_], K, V] extends Laws {
  def laws: KVStoreLaws[F, K, V]

  def kvstore(
    implicit arbKey: Arbitrary[K],
    arbValue: Arbitrary[V],
    eqFOptionV: Eq[F[Option[V]]],
    eqOptionF: Eq[Option[F[(Option[V], Option[V])]]]
  ): RuleSet =
    new DefaultRuleSet(
      name = "KVStore",
      parent = None,
      "write-get persistence" -> forAllNoShrink(laws.writeGetPersistence _),
      "get idempotence" -> forAllNoShrink(laws.getIdempotence _),
      "latest overwrite wins" -> forAllNoShrink(laws.latestOverwriteWins _),
      "delete removes" -> forAllNoShrink(laws.deleteRemoves _),
      "delete absent key is no-op" -> forAllNoShrink(laws.deleteAbsentKey _),
      "delete twice is delete once" -> forAllNoShrink(laws.deleteTwiceIsDeleteOnce _),
      "can write after delete" -> forAllNoShrink(laws.canWriteAfterDelete _),
      "writes are independent" -> forAllNoShrink(laws.writeIndependence _),
      "deletes are independent (left)" -> forAllNoShrink(laws.deleteIndependenceLeft _),
      "deletes are independent (right)" -> forAllNoShrink(laws.deleteIndependenceRight _)
    )
}

object KVStoreTests {

  def apply[F[_]: Monad, K: Eq, V](implicit store: KVStore[F, K, V]): KVStoreTests[F, K, V] =
    new KVStoreTests[F, K, V] {
      def laws: KVStoreLaws[F, K, V] = new KVStoreLaws[F, K, V](store)
    }
}

object TestUtils {
  import org.scalatestplus.scalacheck.Checkers._

  implicit def eqStateByResultOnly[S: Arbitrary, A: Eq]: Eq[State[S, A]] =
    Eq.instance(
      (state1, state2) =>
        Try {
          check((s: S) => Eq[Eval[A]].eqv(state1.runA(s), state2.runA(s)))
        }.isSuccess
    )

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val logger: Logger[IO] = NoOpLogger.impl

  implicit def stateInstance[K, V]: KVStore[State[Map[K, V], ?], K, V] = KVStore.inMemoryStateBased
}

trait RedisSuite extends Suite with BeforeAndAfterAll {
  import TestUtils._

  val redis = for {
    uri    <- Resource.liftF(RedisURI.make[IO]("redis://localhost:6379"))
    client <- RedisClient[IO](uri)
    api    <- Redis[IO, String, String](client, RedisCodec.Utf8, uri)
  } yield api

  val (client, shutdownClient) = redis.allocated.unsafeRunSync()

  def removeAllKeys[V](client: RedisCommands[IO, String, V]) =
    client.keys("*").flatMap(_.traverse(client.del(_)))

  implicit def redisIoEq[A: Eq]: Eq[IO[A]] =
    Eq.by(io => (removeAllKeys(client) *> io).attempt.unsafeRunSync())

  override def afterAll(): Unit =
    (removeAllKeys(client) *> shutdownClient).unsafeRunSync()
}
