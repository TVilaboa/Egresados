package controllers

import actions.SecureAction
import com.google.inject.Inject
import models.{News, Prospect}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
import scrapers.ClarinScraper
import services.{ClarinNewsService, ProspectService, ScrapingService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by matias on 21/09/2016.
  */
class ClarinNewsController @Inject()(newsClarinService: ClarinNewsService,
                                     prospectService: ProspectService,
                                     scraper: ClarinScraper,
                                     secureAction: SecureAction,
                                     scrapingService: ScrapingService) extends Controller {

  def search(id: String) =secureAction{
    val prospect : Future[Prospect] = prospectService.find(id)

    prospect.map(runSearch)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll = secureAction{
    val prospects : Future[Seq[Prospect]] = prospectService.all()

    prospects.map(x => x.foreach(runSearch))

    Redirect(routes.ProspectController.index(""))
  }

  private def runSearch(prospect: Prospect): Unit = {
    scrapingService.runClarinSearch(scraper, newsClarinService, prospectService, prospect)

  }

  def deleteNews(id:String) = secureAction {
    //Get graduate from DB.
    val news : News = Await.result(newsClarinService.find(id),Duration.Inf)
    Await.result(newsClarinService.drop(news), Duration.Inf)
    Redirect("/")
  }
}
