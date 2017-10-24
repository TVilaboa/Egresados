package controllers

import actions.SecureAction
import com.google.inject.Inject
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
import scrapers.LinkedinUserProfileScraper
import services.{LinkedinUserProfileService, ProspectService, ScrapingService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Nacho on 23/09/2016.
  */

class LinkedinUserProfileController @Inject() (linkedinUserProfileService: LinkedinUserProfileService,
                                               scraper : LinkedinUserProfileScraper,
                                               prospectService: ProspectService, secureAction: SecureAction,
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

  private def runSearch(prospect: Prospect) = {
    scrapingService.runLinkedinSearch(prospectService, scraper, linkedinUserProfileService, prospect)
  }

  def deleteProfile(id:String) =secureAction {
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect(routes.Application.homeFeed())
  }

}