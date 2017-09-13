package generators


import java.io.IOException
import java.net.SocketException

import org.jsoup.nodes._
import org.jsoup.select._
import play.api.Logger
import services.SearchEngineService

import scala.collection.JavaConversions._

/**
  * Created by franco on 9/12/16.
  */
trait BasicUrlGenerator {

  final val ERROR_LOGGER: Logger = Logger("errorLogger")
  /**
    * En función de un nombre:String y un String a buscar, devuelve una Lista con los resultados obtenidos
    * */
  def getSearchedUrl(name : Option[String], query : Option[String]) : List[String]

  /**
    * En función del nombre de la persona que se encuentra dentro de un Array[String] y de un parametro
    * de busqueda (query : String), nos retorna una Lista con posibles resultados
    **/
  def getGoogleSearchRegisters(query: String, domain: String, cleanDomain: Boolean = false): List[String] = {

    // Setup proxy
    /* Proxy proxy = new Proxy(                                      //
       Proxy.Type.HTTP,                                      //
       InetSocketAddress.createUnresolved("127.0.0.1", 8080) //
     );*/
    var result: List[String] = List()

    try {
      val tuple = SearchEngineService.getQuery(query)
      val doc: Document = tuple._1
      val realQuery = tuple._2
      val links: Elements = doc.select("a[href*='" + domain + "']")
      //Puede ser un span tambien <span class="url">https://ar.linkedin.com/in/emiliolopezgabeiras</span>
      for (link: Element <- links) {
        result = cleanAndAdd(link.attr("href"), result, cleanDomain, domain)
      }
      var textLinks = doc.select(".fz-ms.fw-m.fc-12th.wr-bw,span.url,a.result__url") //Yahoo,IxQuick,Duck Duck Go

      for (link: Element <- textLinks) {
        result = cleanAndAdd(link.text(), result, cleanDomain, domain)
      }
      if (result.isEmpty) {
        ERROR_LOGGER.error(this.getClass.getName + " :-: Links Not Found :-: " + realQuery)
        println("Exited " + domain + " Generator without exception. Did not found links. Query: " + query)
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

  def cleanAndAdd(url: String, existentLinks: List[String], cleanDomain: Boolean, domain: String): List[String] = {
    var temp = url
    var result = existentLinks
    if (temp.contains(domain) && !temp.startsWith("/search") && !temp.contains("translate")) {
      if (temp.indexOf("https") > -1) temp = "https" + temp.split("https")(1)
      else if (temp.indexOf("http") > -1) temp = "http" + temp.split("http")(1)
      if (cleanDomain) temp = cleanUrlDomain(temp)
      if (!"".equals(temp))
        result = temp :: result
    }
    return result
  }
  /**
    * Metodo que se encarga de limpiar un dominio (url:String) para eliminar cualquier exceso de caracteres
    * */
  def cleanUrlDomain(url : String) : String
}
