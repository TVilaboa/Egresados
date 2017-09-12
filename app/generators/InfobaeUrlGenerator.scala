package generators

import java.io.IOException
import java.net.SocketException
import java.text.Normalizer

import org.jsoup.select.Elements
import services.SearchEngineService

import scala.collection.JavaConversions._
import scala.util.matching.Regex

/**
  * Created by nacho on 8/29/2016.
  */

class InfobaeUrlGenerator extends BasicUrlGenerator{
  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    **/
  override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
    name match {
      case Some(x) =>
        //Split both name and query : Option[String]
        val splitName = name.get.split(" ")
        val splitQuery = query.get.split(" ")

        var searcher = "site:infobae.com"

        for(splitVal : String <- splitName)
          searcher = searcher + "+" + splitVal

        for(splitVal : String <- splitQuery)
          searcher = searcher + "+" + splitVal

        getGoogleSearchRegisters(searcher)

      case None => List()
    }
  }

  /**
    * En función del nombre de la persona que se encuentra dentro de un Array[String] y de un parametro
    * de busqueda (query : String), nos retorna una Lista con posibles resultados
    **/
  override def getGoogleSearchRegisters(query: String): List[String] = {
    var result : List[String] = List()

    try {
      val doc = SearchEngineService.getQuery(query)
      val links: Elements = doc.select("a[href*=infobae]")
      for (link <- links) {
        val url = cleanUrlDomain(link.attr("href"))
        if(!"".equals(url))
          result = url :: result
      }
      println("Exited InfobaeUrlGenerator without exception")
    } catch {
      case e: SocketException => e.printStackTrace()
      case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
        Thread.sleep(10000)
      }
      case e: Exception => e.printStackTrace()
    }
    result
  }

  /**
    * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
    **/
  override def cleanUrlDomain(url: String): String = {
    val regex : Regex = "(http|https)://www.infobae.com/(.+)".r
    regex findFirstIn  url match{
      case Some(x) =>
        x.contains(":infobae") match{
          case true => ""
          case false => x
        }
      case None => ""
    }
  }

}

/**
  * To test searcher add following line to Application.index
  *     val name : List[String] = List("Alfonso Prat Gay", "Daniel Oyarzún", "Felix Peña", "Manuel Garcia Hamilton")
  *     val list : List[(String,String)] = name.flatMap(x =>
  *     InfobaeUrlGeneratorObject.search(Option(x), None).map((x,_)))
  *     println("URL: \n")
  *     list.foreach(x=> println(x._1 + " : " + x._2 + "\n"))
  * */
object InfobaeUrlGeneratorObject{
  val generator : BasicUrlGenerator = new InfobaeUrlGenerator()

  def search(name: Option[String], query: Option[String]): List[String] = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), query)
}