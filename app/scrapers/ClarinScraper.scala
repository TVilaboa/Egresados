package scrapers

import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions._

class ClarinScraper extends NewsScraper{

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

  override protected def getScraperName(): String = "Clarin Scraper"
}
