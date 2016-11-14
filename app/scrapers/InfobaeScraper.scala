package scrapers

import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import models.InfobaeNews
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Logger

import scala.collection.JavaConversions._

/**
  * Created by Brian Re & Michele Re
 */


class InfobaeScraper {

  def scrape(url: String, cycle: Int): Option[InfobaeNews] ={
    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    var document: Option[Document] = None
    val successLogger: Logger = Logger("successLogger")
    val errorLogger: Logger = Logger("errorLogger")


    try {
      document = Option(Jsoup.connect(url).userAgent(userAgentString).get)
      successLogger.info(url)
    } catch {
      case  e: ReadTimeoutException =>
        if (cycle == 0) scrape(url, cycle + 1)
        else e.printStackTrace()

      case e : IOException => e.printStackTrace()
    }
    val header = document.get.getElementsByTag("header")
    var titulo: Option[String] = getString(document.get.getElementsByClass("entry-title"))
    var copete: Option[String] = getString(document.get.getElementsByClass("preview"))

    var autor: Option[String] = getString(document.get.getElementsByClass("author-name"))

    val fecha = getString(document.get.getElementsByClass("byline-date"))


      Some(InfobaeNews(UUID.randomUUID().toString, url, titulo.get, fecha.get, copete.get, autor.get))
    } catch {
      case e: IOException => e.printStackTrace()
        errorLogger.info(url + " - " + e.toString)
        None
    }

  }

  private def getString(elements : Elements) : Option[String] ={
    if(elements.isEmpty)
      Some("")
    else
      Option(elements(0).text())
  }
}
