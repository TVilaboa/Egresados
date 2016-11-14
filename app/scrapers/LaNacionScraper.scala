package scrapers

import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import models.LaNacionNews
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Logger

class LaNacionScraper () {

  def getArticleData(url : String, cycle : Int): Option[LaNacionNews] ={

    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val successLogger: Logger = Logger("successLogger")
    val errorLogger: Logger = Logger("errorLogger")
    var doc: Document = null


    try {
      doc = Jsoup.connect(url).userAgent(userAgentString).get()
      successLogger.info(url)

      val article = doc.select("#nota") //Para entrar en un tag <article id = "nota"/>

      //busco los datos en la nota
      val title = article.get(0).getElementsByTag("h1").get(0).text()
      val date = article.get(0).getElementsByClass("fecha").get(0).text()
      val tuft = article.get(0).getElementsByTag("p").get(0).text()
      var author: String = "anonymus"

      try{
       author = article.get(0).select("a[itemprop = author]").get(0).text()
       //armo la lista con todos los datos
      }
      catch{
        case e : IndexOutOfBoundsException =>
          errorLogger.info(url + " - " + e.toString)
      }

      val news: LaNacionNews = LaNacionNews(UUID.randomUUID().toString, url, title, date, tuft, author)

      Some(news)

    }
    catch {
      case  e: ReadTimeoutException =>
        if (cycle == 0) getArticleData(url, cycle + 1)
        else {
          errorLogger.info(url + " - " + e.toString)
          None
        }
      case e : IOException =>
        errorLogger.info(url + " - " + e.toString)
        None
      case  e: Exception =>
        errorLogger.info(url + " - " + e.toString)
        None
    }

  }
}
