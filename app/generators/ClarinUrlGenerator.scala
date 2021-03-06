package generators

import java.text.Normalizer

import scala.util.matching.Regex

class ClarinUrlGenerator extends BasicUrlGenerator{

    /**
      * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
      **/
    override def getSearchedUrl(name: Option[String], query: Option[String]): List[String] = {
      name match {
        case Some(x) =>
          //Split both name and query : Option[String]
          val splitName = name.get.split(" ")
          val splitQuery = query.get.split(" ")

          var searcher = "Clarin"

          for(splitVal : String <- splitName)
            searcher = searcher + "+" + splitVal

          for(splitVal : String <- splitQuery)
            searcher = searcher + "+" + splitVal

          getGoogleSearchRegisters(searcher, "clarin.com")

        case None => List()
      }
    }



    /**
      * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
      **/
    override def cleanUrlDomain(url: String): String = {
      val regex : Regex = "(http|https)://www.clarin.com/(.+?)(?=.html).html".r
      regex findFirstIn  url match{
        case Some(x) => x
        case None => ""
      }
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

  def search(name: Option[String], query: Option[String]): List[String] = generator.getSearchedUrl(Option(Normalizer.normalize(name.getOrElse(""), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}̃']", "")), query)
}
