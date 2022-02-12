import munit.FunSuite
import Main.Repository

class ProgramSpec extends FunSuite {

  val manyRepos = List(
    Repository("flawless", 36),
    Repository("sup", 174),
    Repository("videos", 26),
    Repository("spotify-next", 35),
    Repository("brick-store", 55),
    Repository("slick-effect", 62),
  )

  test("combineResults(Nil)") {
    assertEquals(Main.getResult(Nil, 5), Nil)
  }

  test("combineResults with 1 repo") {
    assertEquals(
      Main.getResult(List(Repository("flawless", 36)), 3),
      List(Repository("flawless", 36)),
    )
  }

  test("combineResults with 3 repos") {
    assertEquals(
      Main.getResult(
        manyRepos,
        3,
      ),
      List(
        Repository("sup", 174),
        Repository("slick-effect", 62),
        Repository("brick-store", 55),
      ),
    )
  }

  test("combineResults with 3 repos #2") {
    assertEquals(
      Main.getResult(
        manyRepos,
        2,
      ),
      List(
        Repository("sup", 174),
        Repository("slick-effect", 62),
      ),
    )
  }

  test("renderResults(Nil)") {
    assertEquals(Main.renderResults(Nil), Nil)
  }

  test("renderResults(6 repos)") {
    assertEquals(
      Main.renderResults(manyRepos),
      List(
        "1. flawless (36 stars)",
        "2. sup (174 stars)",
        "3. videos (26 stars)",
        "4. spotify-next (35 stars)",
        "5. brick-store (55 stars)",
        "6. slick-effect (62 stars)",
      ),
    )
  }
  test("renderResults(4 repos)") {
    assertEquals(
      Main.renderResults(manyRepos.take(4)),
      List(
        "1. flawless (36 stars)",
        "2. sup (174 stars)",
        "3. videos (26 stars)",
        "4. spotify-next (35 stars)",
      ),
    )
  }
}
