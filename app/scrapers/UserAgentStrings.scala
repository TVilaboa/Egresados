package scrapers

/**
  * Created by franco on 25/04/17.
  */
object UserAgentStrings {
  final val USER_AGENTS : List[String] = List("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")

  private var counter = 0

  def getActive() : String = USER_AGENTS(counter)

  def next() : String = {
    counter += 1
    try{
      getActive()
    }
    catch{
      case e:IndexOutOfBoundsException =>
        reset()
        throw e
    }
  }

  def reset() = counter = 0
}
