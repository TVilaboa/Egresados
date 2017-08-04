package controllers

import actions.SecureAction
import com.google.inject.Inject
import generators.ClarinUrlGeneratorObject
import models.{News, Prospect}
import play.api.mvc.{Action, Controller}
import scrapers.ClarinScraper
import services.{ClarinNewsService, ProspectService}

import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
  * Created by matias on 21/09/2016.
  */
class ClarinNewsController @Inject()(newsClarinService: ClarinNewsService,
                                     prospectService: ProspectService,
                                     scraper: ClarinScraper,
                                     secureAction: SecureAction) extends Controller{

  def search(id: String) =secureAction{
    val prospect : Prospect = Await.result(prospectService.find(id), Duration.Inf)

    runSearch(prospect)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll =secureAction{
    val prospects : Seq[Prospect] = Await.result(prospectService.all(), Duration.Inf)

    prospects.foreach(x=>runSearch(x))

    Redirect(routes.ProspectController.index(""))
  }

  def runSearch(prospect: Prospect) : Unit = {
    val name : Option[String] = Option(prospect.getFullName)
    val links : Seq[String] = ClarinUrlGeneratorObject.search(name, Option(prospect.institution.name))

    val news : Seq[News] = links.map{x=> scraper.getArticleData(x,name,0)}.filter(_.isDefined).map(_.get)

    if(news.nonEmpty){
      val activeNews: List[String] = prospect.clarinNews.map(_.url)
      val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList
      difference.map(newsClarinService.save)

      val all : List[News] = prospect.clarinNews ::: difference
      Await.result(prospectService.update(prospect.copy(clarinNews = all)), Duration.Inf)
    }
  }

  def deleteNews(id:String) =secureAction {
    //Get graduate from DB.
    val news : News = Await.result(newsClarinService.find(id),Duration.Inf)
    Await.result(newsClarinService.drop(news), Duration.Inf)
    Redirect("/")
  }
}
