package generators

import java.sql.Timestamp
import java.text.Normalizer
import java.util.Date

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

          getGoogleSearchRegisters(searcher, "lanacion.com")

        case None => List()
      }
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
      domain.contains("lanacion.com")
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

  def search(name: Option[String], query: Option[String]): List[String] = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), query)
}
