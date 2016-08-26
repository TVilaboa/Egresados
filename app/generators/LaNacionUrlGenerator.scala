package generators

import java.io.IOException
import java.net.SocketException
import java.util
import java.util.Date
import java.sql.Timestamp

import org.jsoup.Jsoup

import scala.collection.mutable.ListBuffer

class LaNacionUrlGenerator {

//    private var isTorRunning: Boolean = false
//
//    def searchLaNacionUrl(searchName: String, startup: String, role: String): String = {
//        if(searchName != null && isTorRunning) {
//            var nameSplit = searchName.split(" ")
//            var searcher = "lanacion"
//
//            //Limpio los nombres que tienen comas u otro caracter
//            for(i <- 0  until (nameSplit.length-1)) {
//                nameSplit(i) = nameSplit(i).filter("abcdefghijklmnopqrstvwxyz".contains(_))
//            }
//
//            //Completo para el buscador de google
//            for(word <- nameSplit) {
//                searcher = searcher + "%20" + word
//            }
//
//            if(startup != null) searcher = searcher + "%20" + startup
//            if(role != null) searcher = searcher + "%20" + role
//
//            var result = getDataFromGoogle(nameSplit, searcher);
//            return selectCorrectUrl(nameSplit, result);
//        }
//        ""
//    }
//
//    def printTime(): Unit = {
//      println("#####" + new Timestamp(new Date().getTime))
//    }
//
//    def startTorClient() {
//        System.setProperty("socksProxyHost", "localhost")
//        System.setProperty("socksProxyPort", "9050")
//        Thread.sleep(1000)
//        isTorRunning = true
//    }
//
//    def stopTorClient() {
//        System.clearProperty("socksProxyHost")
//        System.clearProperty("socksProxyPort")
//        isTorRunning = false
//    }
//
//    def getDataFromGoogle(name: Array[String], query: String): ListBuffer[String] = {
//      var result = scala.collection.mutable.ListBuffer.empty[String]
//      val request = "https://www.google.com.ar/search?q=" + query + "&num=10"
//
//      try {
//        val doc = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
//          .timeout(50000)
//          .get
//        val links = doc.select("a[href*=linkedin]")
//        for (link <- links) {
//          var temp = link.attr("href")
//          if (temp.startsWith("/url?q=")) {
//            temp = cleanDomain(temp)
//            if (temp != null && temp != "") result.add(temp)
//          }
//        }
//      } catch {
//        case e: SocketException => {
//          e.printStackTrace()
//          isTorRunning = false
//        }
//        case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
//          Thread.sleep(10000)
//        }
//      }
//      result
//    }

}
