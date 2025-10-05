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
      Request.get(Path.root / "assets" / Param.separatedString("/")),
      Response.staticResource("kroptime/assets/")
    )

  val todos = Route(
    Request.get(Path.root / "todos"),
    Response.ok(Entity.html)
  )

  val newTodo = Route(
    Request.post(Path / "todos").withEntity(Entity.formOf[TodoForm]),
    Response.ok(Entity.html)
  )

  val test = Route(
    Request.get(Path.root / "todos2" / Param.int),
    Response.ok(Entity.html)
  )
}

case class TodoForm(name: String) derives FormCodec
