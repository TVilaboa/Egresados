package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

import java.util.{UUID, ArrayList, Date}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
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

  def getLinkedinProfile(url: String): LinkedinUserProfile = {
    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val doc = Jsoup.connect(url).userAgent(userAgentString).get
    val profile = doc.select("#profile")
    val title = profile.get(0).getElementsByClass(  "profile-overview-content")
      .get(0)
      .getElementsByTag("p")
    val posicionActual = getText(title)
    val experience = doc.select("#experience")
    val position = experience.get(0).getElementsByClass("position")
    var listJobs: List[LinkedinJob] = List[LinkedinJob]()
    for (el <- position) {
      val posicion = el.getElementsByClass("item-title")
      val cargoEmpleo = getText(posicion)
      val lugarTrabajo = el.getElementsByClass("item-subtitle").get(0)
      var lugarEmpleo: String = null
      var urlTrabajo: String = null
      if (lugarTrabajo.getElementsByTag("a") != null && lugarTrabajo.getElementsByTag("a").size > 0) {
        lugarEmpleo = lugarTrabajo.getElementsByTag("a").get(0).text()
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
    val education = doc.select("#education")
    val educationList = education.get(0).getElementsByClass("school")
    var listEducation: List[LinkedinEducation] = List[LinkedinEducation]()
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
      val degreeName = el.getElementsByClass("item-subtitle")
      val degree = getText(degreeName)
      val dateRange = el.getElementsByClass("date-range")
      val date = getText(dateRange)
      val description = el.getElementsByClass("description")
      val desc = getText(description)
      listEducation = LinkedinEducation(UUID.randomUUID().toString,instituto,urlInstituto,degree,date,desc) :: listEducation
    }
    LinkedinUserProfile(UUID.randomUUID().toString,posicionActual, listJobs,listEducation , url, new Date())
  }

  private def getText(e: Elements): String = {
    if (e != null) {
      if (e.size > 0) {
        return e.get(0).text()
      }
    }
    "No tiene"
  }
}
