package generators

import java.io.IOException
import java.net.SocketException
import java.util
import java.util.Date
import java.util.ArrayList
import java.sql.Timestamp

import scala.collection.JavaConversions._

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.jsoup.nodes.Element

import scala.collection.mutable.ListBuffer

class LaNacionUrlGenerator {

    private var isTorRunning: Boolean = true

    def searchLaNacionUrl(searchName: String): ListBuffer[String] = {
       // startTorClient()
        if(searchName != null && isTorRunning) {
            val quotationName = "%22" + searchName + "%22"
            val nameSplit = quotationName.split(" ")
            var searcher = "lanacion"

            //Limpio los nombres que tienen comas u otro caracter
            for(i <- 0  until (nameSplit.length-1)) {
                nameSplit(i) = nameSplit(i).filter("abcdefghijklmnopqrstuvwxyz%22".contains(_))
            }

            //Completo para el buscador de google
            for(word <- nameSplit) {
                searcher = searcher + "%20" + word
            }

            searcher = searcher + "%20austral"

            val result = getDataFromGoogle(nameSplit, searcher)
         //   stopTorClient()
            return selectCorrectURLs(nameSplit, result)
        }
      scala.collection.mutable.ListBuffer.empty[String]
    }

    def printTime(): Unit = {
      println("#####" + new Timestamp(new Date().getTime))
    }

    def startTorClient() {
        System.setProperty("socksProxyHost", "localhost")
        System.setProperty("socksProxyPort", "9050")
        Thread.sleep(1000)
        isTorRunning = true
    }

    def stopTorClient() {
        System.clearProperty("socksProxyHost")
        System.clearProperty("socksProxyPort")
        isTorRunning = false
    }

    def getDataFromGoogle(name: Array[String], query: String): ListBuffer[String] = {
      val result = scala.collection.mutable.ListBuffer.empty[String]
      val request = "https://www.google.com.ar/search?q=" + query + "&num=10"

      try {
        val doc: Document = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
          .timeout(50000)
          .get
        val links: Elements = doc.select("a[href*=lanacion]")
        for (link:Element <- links.toList) {
          var temp: String = link.attr("href")
          if (temp.startsWith("/url?q=")) {
            temp = cleanDomain(temp)
            if (temp != null && temp != "") result += temp
          }
        }
      } catch {
        case e: SocketException => {
          e.printStackTrace()
          isTorRunning = false
        }
        case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
          Thread.sleep(10000)
        }
      }
      result
    }

    def cleanDomain(url: String): String = {
        var domainName: String = ""
        var domainNameSplitByAmper = Array[String]()

        //Elimino las url que tengan busqueda de usuarios
        if (!url.contains("pub/dir/")) {
            //Borro los caracteres '/url?q='
            domainName = url.substring(7)
            //Limpio los parámetros pasados por url
            if (domainName.contains("&")) {
                domainNameSplitByAmper = domainName.split("&")
                domainName = domainNameSplitByAmper(0)
            }
        }
        domainName
    }

  private def selectCorrectURLs(name: Array[String], manyURLs: ListBuffer[String]): ListBuffer[String] = {
    for(url <- manyURLs) {
      if(!isCorrect(name, url)) {
        manyURLs -= url
      }
    }
    manyURLs
  }

    private def isCorrect(searchName: Array[String], domain: String): Boolean = {
      for(i <- 0  until (searchName.length-1)) {
        searchName(i) = searchName(i).filter(!"%22".contains(_))
      }
      var isCorrect: Boolean = false
      if (domain.substring(7).startsWith("www.lanacion.com.ar") || domain.substring(11).startsWith("lanacion.com.ar")) {
          isCorrect = true
//        val splitURL = domain.split("/")
//        if (splitURL.length >= 4) {
//          val nameOnURL = splitURL(3)
//          for(name <- searchName) {
//            if (nameOnURL.contains(name)) {
//              isCorrect = true
//            }
//          }
//        }
      }
      isCorrect
    }
}
