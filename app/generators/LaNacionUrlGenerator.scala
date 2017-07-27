package generators

import java.io.IOException
import java.net.SocketException
import java.util.Date
import java.sql.Timestamp

import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.util.matching.Regex

class LaNacionUrlGenerator extends BasicUrlGenerator{

    /**
      * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
      **/
    override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
      name match {
        case Some(x) =>
          //Split both name and query : Option[String]
          val splitName = name.get.split(" ")
          val splitQuery = query.get.split(" ")

          var searcher = "lanacion"

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
    val request = "https://www.google.com.ar/search?q=" + query + "&num=10"
    try {
      val doc = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
        .timeout(50000)
        .get
      val links: Elements = doc.select("a[href*=lanacion]")
      for (link <- links) {
        val url = cleanUrlDomain(link.attr("href"))
        if(!"".equals(url))
          result = url :: result
      }
    } catch {
      case e: SocketException => e.printStackTrace()
      case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
        Thread.sleep(10000)
      }
    }
    result.distinct
  }

    /**
      * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
      **/
    override def cleanUrlDomain(url: String): String = {
      val regex : Regex = "(http|https)://www.lanacion.com.ar/([0-9]+)".r
      regex findFirstIn  url match{
        case Some(x) => x
        case None => ""
      }
//      val split : Array[String] = url.split("http://")
//      val aux = split.filter(x => x.contains("www.lanacion.com.ar/"))
//      if(aux.nonEmpty)
//        "http://" + aux.head.substring(0, aux.head.indexOf("-"))
//      else
//        ""
    }

    private def selectProfileUrl(username : Array[String], list : List[String]) : List[String] = list.filter(isCorrect(username,_))


    def printTime(): Unit = {
      println("#####" + new Timestamp(new Date().getTime))
    }

    private def isCorrect(searchName: Array[String], domain: String): Boolean = {
      domain.contains("www.lanacion.com.ar/")
    }
}

/**
  * To test searcher add following line to Application.index
  *     val name : List[String] = List("Agustin Lopez Gabeiras", "Emilio Lopez Gabeiras")
  *     val list : List[(String,String)] = name.flatMap(x =>
  *     LaNacionUrlGeneratorObject.search(Option(x), Option("Universidad Austral")).map((x,_)))
  *     println("URL: \n")
  *     list.foreach(x=> println(x._1 + " : " + x._2 + "\n"))
  * */
object LaNacionUrlGeneratorObject{
  val generator : BasicUrlGenerator = new LaNacionUrlGenerator()

  def search(name : Option[String], query : Option[String]) : List[String] = generator.getSearchedUrl(name,query)
}
