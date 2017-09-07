package generators

import java.io.IOException
import java.net.SocketException
import java.sql.Timestamp
import java.text.Normalizer

import org.jsoup.Jsoup

import scala.collection.JavaConversions._

class LinkedInUrlGenerator extends BasicUrlGenerator{

  def printTime() {
    val date = new java.util.Date()
    println("#####" + new Timestamp(date.getTime))
  }

  private def selectProfileUrl(username : Array[String], list : List[String]) : List[String] = {
    val filterByMethod = list.filter(x => isCorrect(username, x))
    if(filterByMethod.nonEmpty)
      return filterByMethod

    //Filters result by Condition
    //Nota :: No encuentra perfiles con acento!!
    val possible : List[String] = list.filter(x => x.substring(8).startsWith("www.linkedin.com") || x.substring(11).startsWith("linkedin.com"))
    val filterByCondition = possible.filter(x => x.split("/").length >= 5 && username.mkString("").equalsIgnoreCase(x.split("/")(4)))
    filterByCondition
  }

  /**
    * TODO Refactor Code
    * */
  private def isCorrect(userName: Array[String], domain: String): Boolean = {
    var isCorrect = false
    if (domain.substring(8).startsWith("www.linkedin.com") || domain.substring(11).startsWith("linkedin.com")) {
      val splitURL = domain.split("/")
      if (splitURL.length >= 5) {
        val nameOnURL = splitURL(4).split("-")
        var nameUrl = ""
        for (name <- nameOnURL if !name.matches(".*\\d+.*")) {
          nameUrl += name + "-"
        }
        if (nameUrl.length > 0 && nameUrl.charAt(nameUrl.length - 1) == '-') {
          nameUrl = nameUrl.substring(0, nameUrl.length - 1)
        }
        if (nameUrl.equalsIgnoreCase(userName.mkString(""))) {
          isCorrect = true
        } else if (nameUrl.equalsIgnoreCase(userName.mkString("-"))) {
          isCorrect = true
        } else {
          var i = 0
          while (i != userName.length) {
            if (nameUrl.contains(userName(i).toLowerCase())) {
              if (userName.length >= 2 && userName(1).length != 0) {
                if (i != (userName.length - 1) &&
                  nameUrl.contains("" + userName(i + 1).toLowerCase().charAt(0))) {
                  if (nameUrl.contains(userName(i + 1).toLowerCase())) {
                    if (nameUrl.length < (userName(i).length + userName(i + 1).length + 3)) isCorrect = true
                  } else if (nameUrl.length < (userName(i).length + 5)) isCorrect = true
                } else if (i != 0 &&
                  nameUrl.contains("" + userName(i - 1).toLowerCase().charAt(0))) {
                  if (nameUrl.length < (userName(i).length + 5)) isCorrect = true
                }
              }
            }
            i += 1
          }
        }
      }
    }
    isCorrect
  }

  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    **/
  override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
    var result : List[String] = List()
    if(name.isDefined){
      //Split both name and query : Option[String]
      val splittedName = name.get.split(" ")
      val splittedQuery = query.get.split(" ")

      var searcher = "linkedIn"

      for(splitVal : String <- splittedName)
        searcher = searcher + "%20" + splitVal

      for(splitVal : String <- splittedQuery)
        searcher = searcher + "%20" + splitVal

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
      val links = doc.select("a[href*=linkedin]")
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
      case e: IOException => e.printStackTrace()
        if (e.getMessage == "HTTP error fetching URL") {
        //Thread.sleep(10000)
      }
      case e: Exception => e.printStackTrace()
    }
    result
  }

  /**
    * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
    **/
  override def cleanUrlDomain(url: String): String = {
    var domainName = ""
    if(!url.contains("pub/dir/")){
        domainName = url.substring(7)
        if (domainName.contains("&")) {
          domainName = domainName.split("&").head
      }
    }
    domainName
  }
}

/**
  * To test searcher add following line to Application.index
  *     val name : List[String] = List("Franco Testori", "Ricardo Pasquini", "Kevin Stessens", "Javier Isoldi", "Pedro Colunga")
  *     val list : List[(String,String)] = name.flatMap(x =>
  *     LinkedInUrlGeneratorObject.search(Option(x), Option("Universidad Austral")).map((x,_)))
  *     println("URL: \n")
  *     list.foreach(x=> println(x._1 + " : " + x._2 + "\n"))
  * */
object LinkedInUrlGeneratorObject{
  val generator : BasicUrlGenerator = new LinkedInUrlGenerator()

  def search(name: Option[String], query: Option[String]): List[String] = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), query).distinct
}
