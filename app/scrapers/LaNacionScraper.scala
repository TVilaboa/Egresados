package scrapers

import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import models.LaNacionNews
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class LaNacionScraper () {

  def getArticleData(url : String, cycle : Int): LaNacionNews ={

    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val doc: Document = Jsoup.connect(url).userAgent(userAgentString).get()

    val article = doc.select("#nota") //Para entrar en un tag <article id = "nota"/>

    //busco los datos en la nota
    val title = article.get(0).getElementsByTag("h1").get(0).text()
    val date = article.get(0).getElementsByClass("fecha").get(0).text()
    val tuft = article.get(0).getElementsByTag("p").get(0).text()
    var author: String = "anonymus"

    try{
     author = article.get(0).select("a[itemprop = author]").get(0).text()
    } catch {
      case  e: ReadTimeoutException =>
        if (cycle == 0) getArticleData(url, cycle + 1)
        else e.printStackTrace()

      case e : IOException => e.printStackTrace()
    }

    //armo la lista con todos los datos
    val news: LaNacionNews = LaNacionNews(UUID.randomUUID().toString,url, title, date, tuft, author)

    news

  }
}
