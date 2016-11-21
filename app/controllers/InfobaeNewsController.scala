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
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsController @Inject() (newsInfobaeService: InfobaeNewsService,graduateService: GraduateService) extends Controller{

  def saveNews(id : String) = Action {
    val generator: InfobaeUrlGenerator = new InfobaeUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val links = InfobaeUrlGeneratorObject.search(Option(graduate.firstName + " " +graduate.lastName),Option("Universidad Austral"))
    val scraper: InfobaeScraper = new InfobaeScraper()
    var news: List[InfobaeNews] = List[InfobaeNews]()
    var element: Option[InfobaeNews] = null
    for(link <- links) {
      element = scraper.scrape(link,0)
      if (element.isDefined) {
        newsInfobaeService.save(element.get)
        news = element.get :: news
      }
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
    Redirect("/profile/" + graduate._id)

  }

  def saveAllInfobaeNews = Action {
    val scraper : InfobaeScraper = new InfobaeScraper()
    val all : Future[Seq[Graduate]] = graduateService.all()
    val graduates : Seq[Graduate] = Await.result(all,Duration.Inf)
    graduates.foreach{grad : Graduate =>
      var newsList: List[InfobaeNews] = List[InfobaeNews]()
      val links = InfobaeUrlGeneratorObject.search(Option(grad.firstName + " " +grad.lastName),Option("Universidad Austral"))
      links.foreach{link : String =>
        val news = scraper.scrape(link)
        newsInfobaeService.save(news)
        newsList = news :: newsList
      }
      val graduate = grad.copy(infobaeNews = newsList)
      Await.result(graduateService.update(graduate),Duration.Inf)
    }

    Ok(views.html.index.render("Success"))
  }


}
