package controllers

import java.sql.Timestamp
import java.util.Date

import com.google.inject.Inject
import generators.{InfobaeUrlGeneratorObject, InfobaeUrlGenerator, LaNacionUrlGeneratorObject, LaNacionUrlGenerator}
import models._
import play.api.mvc.{Action, Controller}
import scrapers.{InfobaeScraper, LaNacionScraper}
import services.{InfobaeNewsService, GraduateService, LaNacionNewsService}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsController @Inject() (newsInfobaeService: InfobaeNewsService,graduateService: GraduateService) extends Controller{

  def saveNews = Action {
    val generator: InfobaeUrlGenerator = new InfobaeUrlGenerator()
    val links = InfobaeUrlGeneratorObject.search(Option("lopez gabeiras"),Option("Universidad Austral"))
    val scraper: InfobaeScraper = new InfobaeScraper()
    var news: List[InfobaeNews] = List[InfobaeNews]()
    var element: InfobaeNews = null
    var graduate : Graduate = Await.result(graduateService.findByLastName("Testori"),Duration.Inf)
    for(link <- links) {
      element = scraper.scrape(link)
      newsInfobaeService.save(element)
      news = element :: news
    }
    graduate = graduate.copy(infobaeNews = news)
    var result = Await.result(graduateService.update(graduate),Duration.Inf)
    val userNews: InfobaeUserNews = new InfobaeUserNews(news, new Timestamp(new Date().getTime))
    for(news <- userNews.news) {
      println(news.url)
      println(news.title)
      println(news.date)
      println(news.tuft)
      println(news.author)
      println("*********************************")
    }
    Ok
  }

}
