package controllers

import actions.SecureAction
import com.google.inject.Inject
import generators.{ElCronistaUrlGenerator, ElCronistaUrlGeneratorObject}
import models.{Graduate, News}
import play.api.mvc.{Action, Controller}
import scrapers.ElCronistaScraper
import services.{ElCronistaNewsService, GraduateService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by matias on 21/09/2016.
  */
class ElCronistaNewsController @Inject()(newsElCronistaService: ElCronistaNewsService, graduateService: GraduateService, secureAction: SecureAction) extends Controller{

  def saveNews(id : String) = secureAction {
    val generator: ElCronistaUrlGenerator = new ElCronistaUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val fullName : Option[String]= Option(graduate.firstName + " " +graduate.lastName)
    val links = ElCronistaUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
    val scraper: ElCronistaScraper = new ElCronistaScraper()
    var news: List[News] = List[News]()
    var element: Option[News] = None
    for(link <- links) {
      if (!link.equals(null)) {
        element = scraper.getArticleData(link,fullName,0)
      }
      if (element.isDefined) {
        newsElCronistaService.save(element.get)
        news = element.get :: news
      }
    }
    graduate = graduate.copy(elCronistaNews = news)
    Await.result(graduateService.update(graduate),Duration.Inf)

    Redirect("/profile/" + graduate._id)

  }

  def deleteNews(id:String) = Action {
    //Get graduate from DB.
    val news : News = Await.result(newsElCronistaService.find(id),Duration.Inf)
    Await.result(newsElCronistaService.drop(news), Duration.Inf)
    Redirect("/")
  }


  def saveAllElCronistaNews = Action {
    val scraper : ElCronistaScraper = new ElCronistaScraper()
    val all : Future[Seq[Graduate]] = graduateService.all()
    val graduates : Seq[Graduate] = Await.result(all,Duration.Inf)
    graduates.foreach{grad : Graduate =>
      var newsList: List[News] = List[News]()
      val fullName : Option[String]= Option(grad.firstName + " " +grad.lastName)
      val links = ElCronistaUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
      var element: Option[News] = null
      links.foreach{link : String =>

        element = scraper.getArticleData(link,fullName,0)
        if (element.isDefined) {
          newsElCronistaService.save(element.get)
          newsList = element.get :: newsList
        }
      }
      val graduate = grad.copy(elCronistaNews = newsList)
      Await.result(graduateService.update(graduate),Duration.Inf)
    }

    Redirect("/")
  }
}
