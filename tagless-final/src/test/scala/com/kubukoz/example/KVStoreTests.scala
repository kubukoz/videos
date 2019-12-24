package com.kubukoz.example

import cats.tests.CatsSuite
import org.typelevel.discipline.Laws
import org.scalacheck.Arbitrary
import org.scalacheck.Prop._
import cats.laws.discipline._
import cats.data.State
import cats.mtl.instances.all._
import cats.laws.discipline.arbitrary._

import scala.util.Try
import dev.profunktor.redis4cats.connection.RedisClient
import scala.concurrent.ExecutionContext
import dev.profunktor.redis4cats.connection.RedisURI
import dev.profunktor.redis4cats.log4cats._
import io.chrisdavenport.log4cats.Logger
import io.chrisdavenport.log4cats.noop.NoOpLogger
import dev.profunktor.redis4cats.interpreter.Redis
import dev.profunktor.redis4cats.domain.RedisCodec
import cats.effect.laws.util.TestInstances._
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite

class KVStoreInstanceTests extends CatsSuite with RedisSuite {
  import TestUtils._

  checkAll("KVStore[State[Map[K, V], ?]", KVStoreTests[State[Map[MiniInt, MiniInt], ?], MiniInt, MiniInt].kvstore)

  locally {
    implicit val redisClient = client
    implicit val arbitraryString = Arbitrary(Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString))
    implicit val store = KVStore.redisImpl[IO, String, String]

    checkAll("KVStore(redis)", KVStoreTests[IO, String, String].kvstore)
  }
}

trait KVStoreTests[F[_], K, V] extends Laws {
  def laws: KVStoreLaws[F, K, V]

  def kvstore(implicit arbKey: Arbitrary[K], arbValue: Arbitrary[V], eqFOptionV: Eq[F[Option[V]]]): RuleSet =
    new DefaultRuleSet(
      name = "KVStore",
      parent = None,
      "write-get persistence" -> forAll(laws.writeGetPersistence _),
      "latest overwrite wins" -> forAll(laws.latestOverwriteWins _),
      "delete removes" -> forAll(laws.deleteRemoves _),
      "can write after delete" -> forAll(laws.canWriteAfterDelete _)
    )
}

object KVStoreTests {

  def apply[F[_]: Monad, K, V](implicit store: KVStore[F, K, V]): KVStoreTests[F, K, V] = new KVStoreTests[F, K, V] {
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

  implicit def ioEq[A: Eq]: Eq[IO[A]] = Eq.by(_.attempt.unsafeRunSync())
  implicit def stateInstance[K, V]: KVStore[State[Map[K, V], ?], K, V] = KVStore.inMemoryStateBased
}

trait RedisSuite extends Suite with BeforeAndAfterAll with BeforeAndAfterEach {
  import TestUtils._

  val redis = for {
    uri    <- Resource.liftF(RedisURI.make[IO]("redis://localhost:6379"))
    client <- RedisClient[IO](uri)
    api    <- Redis[IO, String, String](client, RedisCodec.Utf8, uri)
  } yield api

  val (client, shutdownClient) = redis.allocated.unsafeRunSync()

  override def afterEach(): Unit =
    // probably not the best way to clear the database long-term, as it flushes to disk...
    client.flushAll.unsafeRunSync()

  override def afterAll(): Unit =
    shutdownClient.unsafeRunSync()
}
