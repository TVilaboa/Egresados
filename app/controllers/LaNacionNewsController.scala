package controllers

import java.sql.Timestamp
import java.util.Date

import actions.SecureAction
import com.google.inject.Inject
import generators.{InfobaeUrlGeneratorObject, LaNacionUrlGenerator, LaNacionUrlGeneratorObject}
import models.{Graduate, News}
import play.api.mvc.{Action, Controller}
import scrapers.{InfobaeScraper, LaNacionScraper}
import services.{GraduateService, LaNacionNewsService}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Created by matias on 21/09/2016.
  */
class LaNacionNewsController @Inject() (newsLaNacionService: LaNacionNewsService,graduateService: GraduateService, secureAction: SecureAction) extends Controller{

  def saveNews(id : String) = secureAction {
    val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val fullName : Option[String]= Option(graduate.firstName + " " +graduate.lastName)
    val links = LaNacionUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
    val scraper: LaNacionScraper = new LaNacionScraper()
    var news: List[News] = List[News]()
    var element: Option[News] = None
    for(link <- links) {
      if (!link.equals(null)) {
        element = scraper.getArticleData(link,fullName,0)
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

  def deleteNews(id:String) =secureAction {
    //Get graduate from DB.
    val news : News = Await.result(newsLaNacionService.find(id),Duration.Inf)
    Await.result(newsLaNacionService.drop(news), Duration.Inf)
    Redirect("/")
  }


  def saveAllLaNacionNews =secureAction {
    val scraper : LaNacionScraper = new LaNacionScraper()
    val all : Future[Seq[Graduate]] = graduateService.all()
    val graduates : Seq[Graduate] = Await.result(all,Duration.Inf)
    graduates.foreach{grad : Graduate =>
      var newsList: List[News] = List[News]()
      val fullName : Option[String]= Option(grad.firstName + " " +grad.lastName)
      val links = LaNacionUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
      var element: Option[News] = null
      links.foreach{link : String =>

        element = scraper.getArticleData(link,fullName,0)
        if (element.isDefined) {
          newsLaNacionService.save(element.get)
          newsList = element.get :: newsList
        }
      }
      val graduate = grad.copy(laNacionNews = newsList)
      Await.result(graduateService.update(graduate),Duration.Inf)
    }

    Redirect("/")
  }
}
