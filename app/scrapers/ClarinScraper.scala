package scrapers

import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import models.ClarinNews
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Logger

class ClarinScraper() {

  def getArticleData(url : String, name : Option[String], cycle : Int): Option[ClarinNews] ={

    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val successLogger: Logger = Logger("successLogger")
    val errorLogger: Logger = Logger("errorLogger")
    var doc: Document = null


    try {
      doc = Jsoup.connect(url).timeout(5000).userAgent(userAgentString).get()

      val article = doc.select(".main-section") //Para entrar en un tag <article id = "nota"/>

      //busco los datos en la nota
      val title = article.get(0).getElementsByTag("h1").get(0).text()
      val date = article.get(0).getElementsByTag("span").get(0).text()
      val tuft = article.get(0).getElementsByClass("volanta").get(0).text()
      var author: String = "anonymus"

      val aux = article.get(0).select("a[itemprop = author]")
      if(aux.size() > 0)
        author = aux.get(0).text()

      Some(ClarinNews(UUID.randomUUID().toString, url, title, date, tuft, author))

//      if(name.isDefined && article.toString.contains(name)){
//        successLogger.info(url)
//        Some(ClarinNews(UUID.randomUUID().toString, url, title, date, tuft, author))
//      }
//      else
//        None
    }
    catch {
      case  e: ReadTimeoutException =>
        if (cycle == 0) getArticleData(url, name, cycle + 1)
        else {
          errorLogger.info(url + " - " + e.toString)
          None
        }
      case e : IOException =>
        errorLogger.info(url + " - " + e.toString)
        None
      case e : IndexOutOfBoundsException =>
        errorLogger.info(url + " - " + e.toString)
        None
      case  e: Exception =>
        errorLogger.info(url + " - " + e.toString)
        None
    }

  }
}
