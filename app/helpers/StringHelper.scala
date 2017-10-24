package helpers

object StringHelper {
  def containsCaseInsensitive(s: String, l: List[String]): Boolean = {

    for (string <- l) {
      if (string.equalsIgnoreCase(s)) return true
    }
    false
  }
}
