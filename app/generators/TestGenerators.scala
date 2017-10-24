package generators

import play.api.libs.json.Json
import scrapers.{ClarinScraper, ElCronistaScraper, InfobaeScraper, LaNacionScraper}

/**
  * Created by franco on 27/07/17.
  */
object TestGenerators {

  def main(args: Array[String]): Unit = {
    testNacion("Mauricio Macri")
    println("-------------------------------------------------\n")
    testNacion("Emmanuel Macron")
    println("-------------------------------------------------\n")
    testNacion("Javier Milei")
  }

  private def testClarin(name : String) : Unit = {
    val genUrls : List[String] = ClarinUrlGeneratorObject.search(Option(name), Option(""))
    genUrls.foreach { x =>
      println(s"$x\n")
    }

    val clarinScraper : ClarinScraper = new ClarinScraper()
    genUrls.foreach{x =>
      val news = clarinScraper.getArticleData(x,Option(name),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }

  private def testCronista(name : String) : Unit = {
    val genUrls : List[String] = ElCronistaUrlGeneratorObject.search(Option(name), Option(""))
    genUrls.foreach { x =>
      println(s"$x\n")
    }

    val elCronistaScraper : ElCronistaScraper = new ElCronistaScraper()
    genUrls.foreach{x =>
      val news = elCronistaScraper.getArticleData(x,Option(name),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }

  private def testInfobae(name : String) : Unit = {
    val genUrls : List[String] = InfobaeUrlGeneratorObject.search(Option(name), Option(""))
    genUrls.foreach { x =>
      println(s"$x\n")
    }

    val infobaeScraper : InfobaeScraper = new InfobaeScraper()
    genUrls.foreach{x =>
      val news = infobaeScraper.getArticleData(x,Option(name),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }

  private def testNacion(name : String) : Unit = {
    val genUrls : List[String] = LaNacionUrlGeneratorObject.search(Option(name), Option(""))
    genUrls.foreach { x =>
      println(s"$x\n")
    }
    val laNacionScraper : LaNacionScraper = new LaNacionScraper()
    genUrls.foreach{x =>
      val news = laNacionScraper.getArticleData(x,Option(name),0)
      if(news.isDefined)
        println(Json.toJson(news.get).toString())
    }
  }
}
