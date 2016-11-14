package controllers

import java.sql.Timestamp
import java.util.Date

import actions.SecureAction
import com.google.inject.Inject
import generators.{LaNacionUrlGeneratorObject, LaNacionUrlGenerator}
import models.{Graduate, LaNacionUserNews, LaNacionNews}
import play.api.mvc.{Action, Controller}
import scrapers.LaNacionScraper
import services.{GraduateService, LaNacionNewsService}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by matias on 21/09/2016.
  */
class LaNacionNewsController @Inject() (newsLaNacionService: LaNacionNewsService,graduateService: GraduateService, secureAction: SecureAction) extends Controller{

  def saveNews(id : String) = secureAction {
    val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val links = LaNacionUrlGeneratorObject.search(Option(graduate.firstName + " " +graduate.lastName),Option("Universidad Austral"))
    val scraper: LaNacionScraper = new LaNacionScraper()
    var news: List[LaNacionNews] = List[LaNacionNews]()
    var element: Option[LaNacionNews] = None
    for(link <- links) {
      if (!link.equals(null)) {
        element = scraper.getArticleData(link,0)
      }
      if (element.isDefined) {
        newsLaNacionService.save(element.get)
        news = element.get :: news
      }
    }
    graduate = graduate.copy(laNacionNews = news)
    Await.result(graduateService.update(graduate),Duration.Inf)

    Redirect("/profile/" + graduate._id)

  }

  def deleteNews(id:String) = Action {
    //Get graduate from DB.
    val news : LaNacionNews = Await.result(newsLaNacionService.find(id),Duration.Inf)
    Await.result(newsLaNacionService.drop(news), Duration.Inf)
    Redirect("/")
  }

}
