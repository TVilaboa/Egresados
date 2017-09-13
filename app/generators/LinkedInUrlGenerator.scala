package generators

import java.sql.Timestamp
import java.text.Normalizer

import scala.collection.JavaConversions._

class LinkedInUrlGenerator extends BasicUrlGenerator{


  def printTime() {
    val date = new java.util.Date()
    println("#####" + new Timestamp(date.getTime))
  }

  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    **/
  override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
    var result: List[String] = List()
    if (name.isDefined) {
      //Split both name and query : Option[String]
      val splittedName = name.get.split(" ")
      val splittedQuery = query.get.split(" ")

      var searcher = "linkedIn"

      for (splitVal: String <- splittedName)
        searcher = searcher + "%20" + splitVal

      for (splitVal: String <- splittedQuery)
        searcher = searcher + "%20" + splitVal

      val urls = getGoogleSearchRegisters(searcher, "linkedin.com/in")
      result = selectProfileUrl(splittedName, urls)
    }
    result
  }

  private def selectProfileUrl(username : Array[String], list : List[String]) : List[String] = {
    val filterByMethod = list.filter(x => isCorrect(username, x))
    if(filterByMethod.nonEmpty)
      return filterByMethod

    //Filters result by Condition
    //Nota :: No encuentra perfiles con acento!!


    var filterByCondition = List[String]()
    var maxMatches = 0
    for (link <- list) {
      var matches = 0
      for (name <- username) {
        if (link.toLowerCase.contains(name.toLowerCase)) matches += 1
      }
      if (maxMatches < matches) {
        maxMatches = matches
        filterByCondition = List[String](link)
      } else if (maxMatches == matches && maxMatches > 0) {
        filterByCondition.add(link)
      }
    }
    println("Matched " + username.mkString(" ") + " to " + filterByCondition.mkString(" - "))
    filterByCondition
  }

  /**
    * TODO Refactor Code
    * */
  private def isCorrect(userName: Array[String], domain: String): Boolean = {
    var isCorrect = false

      val splitURL = domain.split("/")

    val nameOnURL = splitURL(splitURL.length - 1).split("-")
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


    isCorrect
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


  def search(name: Option[String], query: Option[String]): List[String] = {
    var links = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), query).distinct
    if (links.isEmpty) {
      println("No links found, trying without institution... " + name.getOrElse("Nombre vacio") + " - " + query.getOrElse("Query vacia"))
      links = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), null).distinct
    }



    return links
  }
}
