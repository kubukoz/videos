object example {

  import singleton.ops._

  def safeurl(
    s: String
  )(
    implicit p: Require[StartsWith[s.type, "https://"]]
  ) =
    p.value

  safeurl("https://good-url-seriously")
}
