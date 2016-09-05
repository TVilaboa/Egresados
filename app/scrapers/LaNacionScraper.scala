package scrapers

import generators.LaNacionUrlGenerator
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

import scala.collection.mutable.ListBuffer

/**
  * ismet-scalongo-seed
  * Created by jeronimocarlos on 9/5/16.
  */
class LaNacionScraper {

  def getArticleData(url : String): List[String] ={

    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val doc: Document = Jsoup.connect(url).userAgent(userAgentString).get()

    val article = doc.select("#nota") //Para entrar en un tag <article id = "nota"/>

    //busco los datos en la nota
    val title = article.get(0).getElementsByTag("h1").get(0).text()
    val date = article.get(0).getElementsByClass("fecha").get(0).text()
    val tuft = article.get(0).getElementsByTag("p").get(0).text()
    var author: String = ""

    try{
      author = article.get(0).getElementsByClass("datos floatfix").get(0).text()
    } catch {
      case  e: Exception => author = "anonymus"
    }

    //armo la lista con todos los datos
    val data : List[String] = List(title, date, tuft, author)


    data

  }


//  def scrap(input: String, url: String): List[ScrapperObject] = {
//    val scrappingList = parse(input)
//    try {
//      val document = Jsoup.connect(url).get
//      for (obj <- scrappingList) {
//        val selector = obj.getSelector
//        val elements = document.select(selector)
//        for (element <- elements) {
//          if (element.hasText()) obj.addData(element.text()) else obj.addData(element.attr(obj.getExtract))
//        }
//      }
//    } catch {
//      case e: HttpStatusException => return scrappingList
//      case e: IOException => e.printStackTrace()
//    }
//    scrappingList
//  }
}
