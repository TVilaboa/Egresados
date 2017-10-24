package controllers

import actions.SecureAction
import com.google.inject.Inject
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
import scrapers.InfobaeScraper
import services.{InfobaeNewsService, ProspectService, ScrapingService}

import scala.concurrent.Future

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsController @Inject() (newsInfobaeService: InfobaeNewsService,
                                       prospectService: ProspectService,
                                       scraper: InfobaeScraper,
                                       secureAction: SecureAction,
                                       scrapingService: ScrapingService) extends Controller {

  def search(id: String) =secureAction{
    val prospect : Future[Prospect] = prospectService.find(id)

    prospect.map(runSearch)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll =secureAction{
    val prospects : Future[Seq[Prospect]] = prospectService.all()

    prospects.map(x => x.foreach(runSearch))

    Redirect(routes.ProspectController.index(""))
  }

  def runSearch(prospect: Prospect) : Unit = {
    scrapingService.runInfobaeSearch(scraper, newsInfobaeService, prospectService, prospect)
  }
}
