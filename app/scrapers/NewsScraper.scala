package scrapers

import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import models.News
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Logger

/**
  * Created by franco on 01/03/17.
  */
trait NewsScraper {

  final val USER_AGENT : String = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
  final val ERROR_LOGGER : Logger = Logger("errorLogger")
  final val SUCCESS_LOGGER : Logger = Logger("successLogger")

  /**
    * Main method that scraps and retrieves a news object from a specific url and for a person's name
    */
  def getArticleData(url : String, name : Option[String], cycle : Int): Option[News] = {
    val scraper : String = getScraperName()
    var document : Option[Document] = None

    try{
      document = Option(Jsoup.connect(url).timeout(5000).userAgent(USER_AGENT).get())

      val title : Option[String] = getTitle(document.get)
      val tuft : Option[String] = getTuft(document.get)
      val date : Option[String] = getDate(document.get)
      val author : Option[String] = getAuthor(document.get)

      if(validateScrap(title,tuft,date,author) && validateNews(name,document.get)){
        SUCCESS_LOGGER.info(s"$scraper :-: '${name.get}'  Scrapped Successfully :-: $url")
        Some(News(UUID.randomUUID().toString,url,title.get,date.get,tuft.get,author.get, validated = false))
      }
      else{
        ERROR_LOGGER.warn(s"$scraper :-: '${name.get}' No Data Available or News is not valid :-: $url")
        None
      }
    }
    catch{
      case  e: ReadTimeoutException =>
        if (cycle == 0)
          getArticleData(url, name, cycle + 1)
        else {
          ERROR_LOGGER.error(s"$scraper :-: '${name.get}' ${e.toString} :-: $url")
          None
        }

      case e : IOException =>
        ERROR_LOGGER.error(s"$scraper :-: '${name.get}' ${e.toString} :-: $url")
        None

      case e: IOException =>
        ERROR_LOGGER.error(s"$scraper :-: '${name.get}' ${e.toString} :-: $url")
        None
    }
  }

  /**
    * Retrieves the Title of a News
    */
  protected def getTitle(document : Document) : Option[String]

  /**
    * Retrieves the Tuft of a News
    */
  protected def getTuft(document : Document) : Option[String]

  /**
    * Retrieves the Date of a News
    */
  protected def getDate(document : Document) : Option[String]

  /**
    * Retrieves the Author of a News
    */
  protected def getAuthor(document : Document) : Option[String]

  /**
    * Defines wether the scrap was successfull or not
    */
  protected def validateScrap(title : Option[String], tuft : Option[String],date : Option[String],author : Option[String]) : Boolean = {
    title.isDefined && tuft.isDefined && date.isDefined && author.isDefined
  }

  /**
    * Define wether the news is a valid one
    */
  protected def validateNews(name : Option[String], document: Document) : Boolean

  protected def getScraperName() : String
}
