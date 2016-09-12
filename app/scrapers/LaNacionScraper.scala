package scrapers

import models.{LaNacionUserNews, News}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import java.util.Date


/**
  * ismet-scalongo-seed
  * Created by jeronimocarlos on 9/5/16.
  */
class LaNacionScraper {

  def getArticleData(url : String): News ={

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
      case  e: Exception =>
    }

    //armo la lista con todos los datos
    val news: News = new News(url, title, date, tuft, author)

    news

  }
}
