package generators

import java.io.IOException
import java.net.SocketException
import java.sql.Timestamp
import java.util.ArrayList

import org.jsoup.Jsoup
import scala.collection.JavaConversions._

class LinkedInUrlGenerator {

  def searchLinkedinUrl(searchName: String, startup: String, role: String): String = {
    if (searchName != null) {
      val nameSplit = searchName.split(" ")
      var searcher = "linkedIn"
      for (i <- 0 until nameSplit.length - 1) {
        if (!java.lang.Character.isLetter(nameSplit(i).charAt(0))) nameSplit(i) = nameSplit(i).substring(1).trim() else if (!java.lang.Character.isLetter(nameSplit(i).charAt(nameSplit(i).length - 1))) nameSplit(i) = nameSplit(i).substring(0,
          nameSplit(i).length - 1).trim()
      }
      var i = 0
      while (i != nameSplit.length) {
        searcher = searcher + "%20" + nameSplit(i)
        i += 1
      }
      if (startup != null) searcher = searcher + "%20" + startup
      if (role != null) searcher = searcher + "%20" + role
      val result = getDataFromGoogle(nameSplit, searcher)
      return selectCorrectURL(nameSplit, result)
    }
    ""
  }

  def printTime() {
    val date = new java.util.Date()
    println("#####" + new Timestamp(date.getTime))
  }

  private def getDataFromGoogle(name: Array[String], query: String): ArrayList[String] = {
    val result = new ArrayList[String]()
    val request = "https://www.google.com.ar/search?q=" + query + "&num=10"
    try {
      val doc = Jsoup.connect(request).userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
        .timeout(50000)
        .get
      val links = doc.select("a[href*=linkedin]")
      for (link <- links) {
        var temp = link.attr("href")
        if (temp.startsWith("/url?q=")) {
          temp = cleanDomain(temp)
          if (temp != null && temp != "") result.add(temp)
        }
      }
    } catch {
      case e: SocketException => e.printStackTrace()
      case e: IOException => if (e.getMessage == "HTTP error fetching URL") {
        Thread.sleep(10000)
      }
    }
    result
  }

  private def cleanDomain(url: String): String = {
    var domainName = ""
    var domainNameSplitByAmper: Array[String] = null
    if (!url.contains("pub/dir/")) {
      domainName = url.substring(7)
      if (domainName.contains("&")) {
        domainNameSplitByAmper = domainName.split("&")
        domainName = domainNameSplitByAmper(0)
      }
    }
    domainName
  }

  private def selectCorrectURL(userName: Array[String], manyURLs: ArrayList[String]): String = {
    var url = ""
    val possibleAnswers = new ArrayList[String]()
    if (!manyURLs.isEmpty) {
      if (isCorrect(userName, manyURLs.get(0))) url = manyURLs.get(0) else {
        for (anURL <- manyURLs if anURL.substring(8).startsWith("www.linkedin.com") || anURL.substring(11).startsWith("linkedin.com")) {
          val splitURL = anURL.split("/")
          if (splitURL.length >= 5) {
            val nameOnURL = splitURL(4)
//            if (nameOnURL.equalsIgnoreCase(String.join("", userName))) {
            if (nameOnURL.equalsIgnoreCase(userName.mkString(""))) {
              url = anURL
              //break
            } else if (nameOnURL.equalsIgnoreCase(userName.mkString(""))) {
              url = anURL
              //break
            }
          }
        }
      }
    }
    url
  }

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
        if (nameUrl.charAt(nameUrl.length - 1) == '-') {
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
}
