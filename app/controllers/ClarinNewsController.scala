package controllers

import actions.SecureAction
import com.google.inject.Inject
import generators.{ClarinUrlGenerator, ClarinUrlGeneratorObject}
import models.{Graduate, ClarinNews}
import play.api.mvc.{Action, Controller}
import scrapers.ClarinScraper
import services.{GraduateService, ClarinNewsService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by matias on 21/09/2016.
  */
class ClarinNewsController @Inject()(newsClarinService: ClarinNewsService, graduateService: GraduateService, secureAction: SecureAction) extends Controller{

  def saveNews(id : String) = secureAction {
    val generator: ClarinUrlGenerator = new ClarinUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val fullName : Option[String]= Option(graduate.firstName + " " +graduate.lastName)
    val links = ClarinUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
    val scraper: ClarinScraper = new ClarinScraper()
    var news: List[ClarinNews] = List[ClarinNews]()
    var element: Option[ClarinNews] = None
    for(link <- links) {
      if (!link.equals(null)) {
        element = scraper.getArticleData(link,fullName,0)
      }
      if (element.isDefined) {
        newsClarinService.save(element.get)
        news = element.get :: news
      }
    }
    graduate = graduate.copy(clarinNews = news)
    Await.result(graduateService.update(graduate),Duration.Inf)

    Redirect("/profile/" + graduate._id)

  }

  def deleteNews(id:String) = Action {
    //Get graduate from DB.
    val news : ClarinNews = Await.result(newsClarinService.find(id),Duration.Inf)
    Await.result(newsClarinService.drop(news), Duration.Inf)
    Redirect("/")
  }


  def saveAllClarinNews = Action {
    val scraper : ClarinScraper = new ClarinScraper()
    val all : Future[Seq[Graduate]] = graduateService.all()
    val graduates : Seq[Graduate] = Await.result(all,Duration.Inf)
    graduates.foreach{grad : Graduate =>
      var newsList: List[ClarinNews] = List[ClarinNews]()
      val fullName : Option[String]= Option(grad.firstName + " " +grad.lastName)
      val links = ClarinUrlGeneratorObject.search(fullName,Option("Universidad Austral"))
      var element: Option[ClarinNews] = null
      links.foreach{link : String =>

        element = scraper.getArticleData(link,fullName,0)
        if (element.isDefined) {
          newsClarinService.save(element.get)
          newsList = element.get :: newsList
        }
      }
      val graduate = grad.copy(clarinNews = newsList)
      Await.result(graduateService.update(graduate),Duration.Inf)
    }

    Redirect("/")
  }
}
