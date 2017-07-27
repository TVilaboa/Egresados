package generators

import java.io.IOException
import java.net.SocketException

import org.jsoup.Jsoup
import org.jsoup.select.Elements

import scala.collection.JavaConversions._

/**
  * Created by nacho on 8/29/2016.
  */

class InfobaeUrlGenerator extends BasicUrlGenerator{
  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    **/
  override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
    var result : List[String] = List()
    if(name.isDefined){
      //Split both name and query : Option[String]
      val splittedName = name.get.split(" ")
      var splittedQuery : Array[String] = Array()
      if(query.isDefined){
        splittedQuery = query.get.split(" ")
      }

      var searcher = "site:infobae.com"

      for(splitVal : String <- splittedName)
        searcher = searcher + "%20" + splitVal

      for(splitVal : String <- splittedQuery)
        searcher = searcher + "%20" + splitVal

      result = getGoogleSearchRegisters(searcher)
    }
    result
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
      val links: Elements = doc.select("a[href*=infobae]")
      for (link <- links) {
        var temp = link.attr("href")
        if (temp.startsWith("/url?q=")) {
          temp = cleanUrlDomain(temp)
          if(!"".equals(temp))
            result = temp :: result
        }
      }
    } catch {
      case e: SocketException => e.printStackTrace()
      case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
        Thread.sleep(10000)
      }
    }
    result
  }

  /**
    * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
    **/
  override def cleanUrlDomain(url: String): String = {
    val split : Array[String] = url.split("http://")
    val aux = split.filter(x => !x.contains(":infobae") && x.contains("www.infobae.com/"))
    if(aux.nonEmpty)
      "http://" + aux.head
    else
      ""
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

  def search(name : Option[String], query : Option[String]) : List[String] = generator.getSearchedUrl(name,query)
}