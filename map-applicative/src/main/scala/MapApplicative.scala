import cats.implicits._
import cats.kernel.Order
import cats.effect.Console.io._
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import scala.collection.immutable.SortedMap

final case class ProductId(value: String) extends AnyVal

object ProductId {
  implicit val order: Order[ProductId] = Order.by(_.value)
}

final case class Product(id:          ProductId, name:   String)
final case class Inventory(productId: ProductId, amount: Int)

object MapApplicative extends IOApp {

  val getProducts: IO[List[Product]] =
    putStrLn("Loading products").as(
      List(
        Product(ProductId("PRO1"), "Bread"),
        Product(ProductId("PRO2"), "Milk"),
        Product(ProductId("PRO3"), "Coffee")
      )
    )

  val getInventory: IO[List[Inventory]] =
    putStrLn("Loading inventory").as(
      List(
        Inventory(ProductId("PRO1"), 4),
        //
        Inventory(ProductId("PRO2"), 2),
        Inventory(ProductId("PRO2"), 4),
        //
        Inventory(ProductId("P4"), 10)
      )
    )

  def run(args: List[String]): IO[ExitCode] = {

    val result: IO[SortedMap[ProductId, (Product, Int)]] = for {
      products  <- getProducts.map(_.groupByNel(_.id).fmap(_.head))
      inventory <- getInventory.map(_.groupByNel(_.productId).fmap(_.reduceMap(_.amount)))
    } yield (products, inventory).tupled

    result.flatMap { map =>
      map.toList.fmap(_.toString).traverse(putStrLn(_))
    }

  }.as(ExitCode.Success)
}
