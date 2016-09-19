package generators

/**
  * Created by franco on 9/12/16.
  */
trait BasicUrlGenerator {
  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    * */
  def getSearchedUrl(name : Option[String], query : Option[String]) : List[String]
  /**
    * En función del nombre de la persona que se encuentra dentro de un Array[String] y de un parametro
    * de busqueda (query : String), nos retorna una Lista con posibles resultados
    * */
  def getGoogleSearchRegisters(query : String) : List[String]
  /**
    * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
    * */
  def cleanUrlDomain(url : String) : String
}
