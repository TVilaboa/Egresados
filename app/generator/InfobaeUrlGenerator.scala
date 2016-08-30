package generator

import java.io.IOException
import java.net.SocketException
import java.util.Date

import com.sun.jmx.snmp.Timestamp
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.jsoup.nodes.Element
import scala.collection.JavaConversions._

import scala.collection.mutable.ListBuffer

/**
  * Created by nacho on 8/29/2016.
  */

class InfobaeUrlGenerator {
  private var isTorRunning: Boolean = true

  def searchInfobaeUrl(searchName: String, startup: String, role: String): String = {
    if(searchName != null && isTorRunning) {
      val nameSplit = searchName.split(" ")
      var searcher = "site:infobae.com"

      //Limpio los nombres que tienen comas u otro caracter
      for(i <- 0  until (nameSplit.length-1)) {
        nameSplit(i) = nameSplit(i).filter("abcdefghijklmnopqrstvwxyz".contains(_))
      }
      nameSplit(0) = "%22" + nameSplit(0)
      nameSplit(nameSplit.length-1) = nameSplit(nameSplit.length-1) + "%22"
      //Completo para el buscador de google
      for(word <- nameSplit) {
        searcher = searcher + "%20" + word
      }

      if(startup != null) searcher = searcher + "%20" + startup
      if(role != null) searcher = searcher + "%20" + role

      val result = getDataFromGoogle(nameSplit, searcher)

      if(result.isEmpty){
        throw new NullPointerException("No se encontro ningun articulo.")
      }
      return result.head
    }
    ""
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
        .get()
      val links: Elements = doc.select("a[href*=infobae]")

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

}
