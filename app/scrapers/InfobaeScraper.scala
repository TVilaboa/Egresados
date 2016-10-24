package scrapers

import java.io.IOException
import java.util.UUID
import models.InfobaeNews
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.collection.JavaConversions._

/**
  * Created by Brian Re & Michele Re
 */


class InfobaeScraper {

  def scrape(url: String): InfobaeNews ={
    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    var document: Option[Document] = None
    try {
      document = Option(Jsoup.connect(url).userAgent(userAgentString).get)
    } catch {
      case e: IOException => e.printStackTrace()
    }
    val header = document.get.getElementsByTag("header")
    var titulo: Option[String] = None
    var copete: Option[String] = None
    for (e <- header if e.getElementsByClass("article-header").size == 1) {
      val elementsTitulo = e.getElementsByTag("h1")
      if (elementsTitulo.size > 0) {
        titulo = Option(e.getElementsByTag("h1").get(0).text())
      }
      val elementsCopete = e.getElementsByClass("subheadline")
      if (elementsCopete.size > 0) {
        copete = Option(e.getElementsByClass("subheadline").get(0).text())
      }
      //break
    }
    val elementAutorFecha = document.get.getElementsByClass("byline-author").get(0)
    val elementsAutor = elementAutorFecha.getElementsByClass("author-name")
    var autor: Option[String] = None
    if (elementsAutor.size > 0) {
      autor = Option(elementsAutor.get(0).text())
    }
    val fecha = elementAutorFecha.getElementsByClass("byline-date")
      .get(0)
      .text()


    return new InfobaeNews(UUID.randomUUID().toString, url, titulo.get, fecha, copete.get, autor.get)
  }
}
