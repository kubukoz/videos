package kroptime.routes

import krop.all.*

object Routes {
  val home =
    Route(
      Request.get(Path.root),
      Response.ok(Entity.html)
    )

  // This route serves static assets, such as Javascript or stylesheets, from
  // your resource directory.
  val assets =
    Route(
      Request.get(Path / "assets" / Param.separatedString("/")),
      Response.staticResource("kroptime/assets/")
    )
}
