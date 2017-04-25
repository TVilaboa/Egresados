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
  final val SUCCESS_LOGGER: Logger = Logger("successLogger")
  final val ERROR_LOGGER: Logger = Logger("errorLogger")
  var userAgent : String = UserAgentStrings.getActive()

  def getLinkedinProfile(url: String, cycle: Int): Option[LinkedinUserProfile]  = {
    var doc: Option[Document] = None

    try {

     doc = Option(Jsoup.connect(url).userAgent(userAgent).get)

      SUCCESS_LOGGER.info(s"${scraper} :-: ${url}")

      val title = getJobTitle(doc.get)

      val jobs = getJobsList(doc.get)

      val education = getEducationList(doc.get)

      Some(LinkedinUserProfile(UUID.randomUUID().toString,title, jobs,education , url))
    }
    catch {
      case e : HttpStatusException =>
        try{
          userAgent = UserAgentStrings.next()
          getLinkedinProfile(url,0)
        }
      catch{
        case e : IndexOutOfBoundsException =>
          ERROR_LOGGER.warn(s"${scraper} :-: ${url} :-: Tried All Available User Agents")
          UserAgentStrings.reset()
          None
        }

      case  e: ReadTimeoutException =>
        if (cycle == 0)
          getLinkedinProfile(url, cycle + 1)
        else {
          ERROR_LOGGER.error(s"${scraper} :-: ${url} :-: ${e.toString}")
          None
        }

      case e : IOException =>
        ERROR_LOGGER.error(s"${scraper} :-: ${url} :-: ${e.toString}")
        None

      case  e: Exception =>
        ERROR_LOGGER.error(s"${scraper} :-: ${url} :-: ${e.toString}")
        None
    }
  }

  protected def getJobTitle(document : Document) : String = document.select("#topcard .profile-overview-content p").head.text()

  protected def getJobsList(document : Document) : List[LinkedinJob] = {
    val jobs : List[Element] = document.select("#experience li").toList

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
    val education : List[Element] = document.select("#education li").toList

    //TODO write selectors
    education.map{x=>
      println(s"${x.html()}\n")
      val institution : String = x.select("").head.text()
      val institutionUrl : String = x.select("").attr("href")
      val degree : String = x.select("").head.text()
      val dateRange : String = x.select("").head.text()
      val description : String = x.select("").head.text()

      LinkedinEducation(UUID.randomUUID().toString,institution,institutionUrl,degree,dateRange,description)
    }
  }


}
