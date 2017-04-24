package scrapers

import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

class ClarinScraper extends NewsScraper{

//  override def getArticleData(url : String, name : Option[String], cycle : Int): Option[News] ={
//
//    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
//    val successLogger: Logger = Logger("successLogger")
//    val errorLogger: Logger = Logger("errorLogger")
//    var doc: Document = null
//
//
//    try {
//      doc = Jsoup.connect(url).timeout(5000).userAgent(userAgentString).get()
//
//      val article = doc.select(".main-section") //Para entrar en un tag <article id = "nota"/>
//
//      //busco los datos en la nota
//      val title = article.get(0).getElementsByTag("h1").get(0).text()
//      val date = article.get(0).getElementsByTag("span").get(0).text()
//      val tuft = article.get(0).getElementsByClass("volanta").get(0).text()
//      var author: String = "anonymus"
//
//      val aux = article.get(0).select("a[itemprop = author]")
//      if(aux.size() > 0)
//        author = aux.get(0).text()
//
//      Some(News(UUID.randomUUID().toString, url, title, date, tuft, author))
//
////      if(name.isDefined && article.toString.contains(name)){
////        successLogger.info(url)
////        Some(ClarinNews(UUID.randomUUID().toString, url, title, date, tuft, author))
////      }
////      else
////        None
//    }
//    catch {
//      case  e: ReadTimeoutException =>
//        if (cycle == 0) getArticleData(url, name, cycle + 1)
//        else {
//          errorLogger.info(url + " - " + e.toString)
//          None
//        }
//      case e : IOException =>
//        errorLogger.info(url + " - " + e.toString)
//        None
//      case e : IndexOutOfBoundsException =>
//        errorLogger.info(url + " - " + e.toString)
//        None
//      case  e: Exception =>
//        errorLogger.info(url + " - " + e.toString)
//        None
//    }
//
//  }
  /**
    * Retrieves the Title of a News
    */
  override protected def getTitle(document: Document): Option[String] = {
    try{
      Option(document.select(".title h1").head.text())
    }
    catch {
      case e :Exception => None
    }
  }

  /**
    * Retrieves the Tuft of a News
    */
  override protected def getTuft(document: Document): Option[String] = {
    try{
      val tuft = Option(document.select(".title .bajada").head.text())
      tuft match{
        case Some(x) => tuft
        case None => Option("undefined")
      }
    }
    catch {
      case e :NoSuchElementException => Option("undefined")
      case e :Exception => None
    }
  }

  /**
    * Retrieves the Date of a News
    */
  override protected def getDate(document: Document): Option[String] = {
    try{
      Option(document.select(".title span").head.text())
    }
    catch {
      case e :Exception => None
    }
  }

  /**
    * Retrieves the Author of a News
    */
  override protected def getAuthor(document: Document): Option[String] = {
    try{
      val author = Option(document.select("p[itemprop=\"author\"]").head.text())
      author match{
        case Some(x) => author
        case None => Option("undefined")
      }
    }
    catch {
      case e :NoSuchElementException => Option("undefined")
      case e :Exception => None
    }
  }
  /**
    * Define wether the news is a valid one
    */
  override protected def validateNews(name: Option[String], document: Document): Boolean = {
    val articleContent : List[Element] = document.select(".body-nota p").toList
    var valid : Boolean = false
    articleContent.foreach{x=>
      if(x.toString.contains(name.get))
        valid = true
    }
    valid
  }
}
