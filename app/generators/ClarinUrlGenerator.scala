package generators

import java.io.IOException
import java.net.SocketException
import java.util.Date
import java.sql.Timestamp

import scala.collection.JavaConversions._

import org.jsoup.Jsoup
import org.jsoup.select.Elements

class ClarinUrlGenerator extends BasicUrlGenerator{

    /**
      * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
      **/
    override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
      var result : List[String] = List()
      if(name.isDefined){
        //Split both name and query : Option[String]
        val splittedName = name.get.split(" ")
        val splittedQuery = query.get.split(" ")

        var searcher = "Clarin"

        for(splitVal : String <- splittedName)
          searcher = searcher + "+" + splitVal

        for(splitVal : String <- splittedQuery)
          searcher = searcher + "+" + splitVal

        val urls = getGoogleSearchRegisters(searcher)
        result = selectProfileUrl(splittedName, urls)
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
      val links: Elements = doc.select("a[href*=Clarin]")
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
    result.distinct
  }

    /**
      * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
      **/
    override def cleanUrlDomain(url: String): String = {
      val split : Array[String] = url.split("http://")
      val aux = split.filter(x => x.contains("www.clarin.com/"))
      if(aux.nonEmpty)
        "http://" + aux.head.substring(0, aux.head.indexOf(".html")+5)
      else
        ""
    }

    private def selectProfileUrl(username : Array[String], list : List[String]) : List[String] = list.filter(isCorrect(username,_))


    def printTime(): Unit = {
      println("#####" + new Timestamp(new Date().getTime))
    }

    private def isCorrect(searchName: Array[String], domain: String): Boolean = {
      domain.contains("www.clarin.com/")
    }
}

/**
  * To test searcher add following line to Application.index
  *     val name : List[String] = List("Agustin Lopez Gabeiras", "Emilio Lopez Gabeiras")
  *     val list : List[(String,String)] = name.flatMap(x =>
  *     ClarinUrlGeneratorObject.search(Option(x), Option("Universidad Austral")).map((x,_)))
  *     println("URL: \n")
  *     list.foreach(x=> println(x._1 + " : " + x._2 + "\n"))
  * */
object ClarinUrlGeneratorObject{
  val generator : BasicUrlGenerator = new ClarinUrlGenerator()

  def search(name : Option[String], query : Option[String]) : List[String] = generator.getSearchedUrl(name,query)
}
