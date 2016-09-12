package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */


import org.jsoup.Jsoup
import org.jsoup.select.Elements
import scala.collection.JavaConversions._

object LinkedinScraper {

  def main(args: Array[String]) {
    printData("https://ar.linkedin.com/in/ignacio-cassol-894a0935")
    printData("https://ar.linkedin.com/in/javier-isoldi-5a937091?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/andres-scoccimarro-303412")
    printData("https://ar.linkedin.com/in/santiagofuentes?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/kevstessens?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/ignacio-juan-nu%C3%B1ez-560183b7?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/joaqu%C3%ADn-bucca-04428278?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/sofiabraun?trk=pub-pbmap")
    printData("https://ar.linkedin.com/in/tmsmateus?trk=pub-pbmap")
  }

  def printData(url: String) {
    println("---------------------------------------------------------------------------------------------------------------------")
    val userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36"
    val doc = Jsoup.connect(url).userAgent(userAgentString).get
    val profile = doc.select("#profile")
    val title = profile.get(0).getElementsByClass("profile-overview-content")
      .get(0)
      .getElementsByTag("p")
    println("\n\nPosicion actual: " + getText(title))
    val experience = doc.select("#experience")
    val position = experience.get(0).getElementsByClass("position")
    println("Empleos:")
    for (el <- position) {
      println("\n")
      val posicion = el.getElementsByClass("item-title")
      println("\tPosicion empleo: " + getText(posicion))
      val lugarTrabajo = el.getElementsByClass("item-subtitle").get(0)
      if (lugarTrabajo.getElementsByTag("a") != null && lugarTrabajo.getElementsByTag("a").size > 0) {
        println("\tLugar de trabajo: " + lugarTrabajo.getElementsByTag("a").get(0).text())
        println("\tURL trabajo: " + lugarTrabajo.getElementsByTag("a").attr("href"))
      } else {
        println("\tLugar de trabajo: " + lugarTrabajo.text())
      }
      val periodoTrabajo = el.getElementsByClass("date-range")
      println("\tPeriodo de trabajo: " + getText(periodoTrabajo))
      val descripcion = el.getElementsByClass("description")
      println("\tDescripcion de trabajo: " + getText(descripcion))
    }


    val education = doc.select("#education")
    val educationList = education.get(0).getElementsByClass("school")
    println("\nEducacion: ")
    for (el <- educationList) {
      println("\n")
      val school = el.getElementsByClass("item-title").get(0)
      if (school.getElementsByTag("a") != null && school.getElementsByTag("a").size > 0) {
        println("\tInstituto/Universidad: " + school.getElementsByTag("a").get(0).text())
        println("\tURL: " + school.getElementsByTag("a").attr("href"))
      } else {
        println("\tInstituto/Universidad: " + school.text())
      }
      val degreeName = el.getElementsByClass("item-subtitle")
      println("\tTitulo: " + getText(degreeName))
      val dateRange = el.getElementsByClass("date-range")
      println("\tPeriodo: " + getText(dateRange))

      val description = el.getElementsByClass("description")
      println("\tDescripcion: " + getText(description))
    }



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


