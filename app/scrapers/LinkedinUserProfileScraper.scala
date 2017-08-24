package scrapers


import java.io.IOException
import java.util.UUID

import io.netty.handler.timeout.ReadTimeoutException
import org.jsoup.{HttpStatusException, Jsoup}
import org.jsoup.nodes.{Document, Element}
import play.api.Logger

import scala.collection.JavaConversions._
import models.{LinkedinEducation, LinkedinJob, LinkedinUserProfile}


class LinkedinUserProfileScraper {
  final val scraper : String = "LinkedIn Scraper"

  final val USER_AGENT : String = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"

  final val SUCCESS_LOGGER: Logger = Logger("successLogger")
  final val ERROR_LOGGER: Logger = Logger("errorLogger")

  def getLinkedinProfile(url: String, cycle: Int): Option[LinkedinUserProfile]  = {
    var doc: Option[Document] = None

    try {

      doc = Option(Jsoup.connect(url).userAgent(USER_AGENT).get)

      SUCCESS_LOGGER.info(s"$scraper :-: Profile scraped :-: $url")

      val title : String = getJobTitle(doc.get)

      val jobs : List[LinkedinJob]= getJobsList(doc.get)

      val education : List[LinkedinEducation] = getEducationList(doc.get)

      Some(LinkedinUserProfile(UUID.randomUUID().toString,title, jobs,education , url))
    }
    catch {

      case e : HttpStatusException =>
        ERROR_LOGGER.error(e.toString)
        e.getStatusCode match{
          case 999 => Some(LinkedinUserProfile(UUID.randomUUID().toString,"", List[LinkedinJob](),List[LinkedinEducation]() , url))
          case _ => None
        }

      case  e: ReadTimeoutException =>
        if (cycle == 0)
          getLinkedinProfile(url, cycle + 1)
        else {
          ERROR_LOGGER.error(s"$scraper :-: ${e.toString} :-: $url")
          None
        }

      case e : IOException =>
        ERROR_LOGGER.error(s"$scraper :-: ${e.toString} :-: $url")
        None

      case  e: Exception =>
        ERROR_LOGGER.error(s"$scraper :-: ${e.toString} :-: $url")
        None
    }
  }

  protected def getJobTitle(document : Document) : String = document.select("#topcard .profile-overview-content p").head.text()

  protected def getJobsList(document : Document) : List[LinkedinJob] = {
    val jobs : List[Element] = document.select(".experience-section li").toList

    jobs.map{x =>
      val title : String = x.select("h4").head.text()
      val workplace : String = x.select("h5").head.text()
      val workplaceUrl : String = x.select("h5 a").attr("href")
      val dateRange : String = x.select(".date-range").head.text()
      val description : String = x.select("p").head.text()

      LinkedinJob(UUID.randomUUID().toString,title,workplace,workplaceUrl,dateRange,description)
    }
  }

  protected def getEducationList(document : Document) : List[LinkedinEducation] =  {
    val education : List[Element] = document.select(".education-section li").toList

    //TODO write selectors
    education.map{x=>
      println(s"${x.html()}\n")
      val institution : String = x.select("h3").head.text()
      val institutionUrl : String = x.select("").attr("href")
      val degree : String = x.select("").head.text()
      val dateRange : String = x.select("").head.text()
      val description : String = x.select("").head.text()

      LinkedinEducation(UUID.randomUUID().toString,institution,institutionUrl,degree,dateRange,description)
    }
  }


}
