package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

import java.io.IOException
import java.util.{UUID, ArrayList, Date}
import io.netty.handler.timeout.ReadTimeoutException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Logger
import scala.collection.JavaConversions._
import models.{LinkedinUserProfile, LinkedinEducation, LinkedinJob}
import play.data.format.Formats.DateTime


class LinkedinUserProfileScraper () {

//  def main(args: Array[String]) {
//    val userProfile1 = getLinkedinProfile("https://ar.linkedin.com/in/ignacio-cassol-894a0935")
//    val userProfile2 = getLinkedinProfile("https://ar.linkedin.com/in/javier-isoldi-5a937091?trk=pub-pbmap")
//    val userProfile3 = getLinkedinProfile("https://ar.linkedin.com/in/andres-scoccimarro-303412")
//    val userProfile4 = getLinkedinProfile("https://ar.linkedin.com/in/santiagofuentes?trk=pub-pbmap")
//    val userProfile5 = getLinkedinProfile("https://ar.linkedin.com/in/kevstessens?trk=pub-pbmap")
//  }

  def getLinkedinProfile(url: String, cycle: Int): Option[LinkedinUserProfile]  = {
    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val successLogger: Logger = Logger("successLogger")
    val errorLogger: Logger = Logger("errorLogger")

    var doc: Option[Document] = None
    try {
      doc = Option(Jsoup.connect(url).userAgent(userAgentString).get)
      successLogger.info(url)

    val title = doc.get.select("#profile")(0)
      .getElementsByClass("profile-overview-content")(0)
      .getElementsByTag("p")
    val posicionActual = getText(title)

    val experience = doc.get.select("#experience")
    var position : Elements = new Elements()

    if(experience.nonEmpty)
      position = experience(0).getElementsByClass("position")
    var listJobs: List[LinkedinJob] = List[LinkedinJob]()
    if(position.nonEmpty){
      for (el <- position) {
        val posicion = el.getElementsByClass("item-title")
        val cargoEmpleo = getText(posicion)
        val lugarTrabajo = el.getElementsByClass("item-subtitle")(0)
        var lugarEmpleo: String = null
        var urlTrabajo: String = null
        if (lugarTrabajo.getElementsByTag("a") != null && lugarTrabajo.getElementsByTag("a").size > 0) {
          lugarEmpleo = lugarTrabajo.getElementsByTag("a")(0).text()
          urlTrabajo = lugarTrabajo.getElementsByTag("a").attr("href")
        } else {
          lugarEmpleo = lugarTrabajo.text()
        }
        val periodoTrabajo = el.getElementsByClass("date-range")
        val periodoEmpleo = getText(periodoTrabajo)
        val descripcion = el.getElementsByClass("description")
        val descripcionEmpleo = getText(descripcion)
        listJobs = LinkedinJob(UUID.randomUUID().toString,cargoEmpleo,lugarEmpleo,urlTrabajo,periodoEmpleo,descripcionEmpleo) :: listJobs
      }
    }


    val education : Elements = doc.get.select("#education")

    var educationList : Elements = new Elements()
    if(education.nonEmpty)
      educationList = education(0).getElementsByClass("school")
    var listEducation: List[LinkedinEducation] = List[LinkedinEducation]()

    if(educationList.nonEmpty){
      for (el <- educationList) {
        val school = el.getElementsByClass("item-title").get(0)
        var instituto: String = null
        var urlInstituto: String = null
        if (school.getElementsByTag("a") != null && school.getElementsByTag("a").size > 0) {
          instituto = school.getElementsByTag("a").get(0).text()
          urlInstituto = school.getElementsByTag("a").attr("href")
        } else {
          instituto = school.text()
        }
        val degreeName = el.getElementsByClass("item-subtitle").get(0).getElementsByClass("original").text()
        val dateRange = el.getElementsByClass("date-range")
        val date = getText(dateRange)
        val description = el.getElementsByClass("description")
        val desc = getText(description)
        listEducation = LinkedinEducation(UUID.randomUUID().toString,instituto,urlInstituto,degreeName,date,desc) :: listEducation
      }
    }
    Some(LinkedinUserProfile(UUID.randomUUID().toString,posicionActual, listJobs,listEducation , url))
    }

    catch {
      case  e: ReadTimeoutException =>
        if (cycle == 0) getLinkedinProfile(url, cycle + 1)
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

  private def getText(e: Elements): String = {
    if (e.nonEmpty) {
      if (e.size > 0) {
        return e(0).text()
      }
    }
    "No tiene"
  }
}
