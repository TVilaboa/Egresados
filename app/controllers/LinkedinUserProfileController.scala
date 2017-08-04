package controllers

import com.google.inject.Inject
import generators.LinkedInUrlGeneratorObject
import models._
import play.api.mvc.{Action, Controller}
import scrapers.LinkedinUserProfileScraper
import services.{LinkedinUserProfileService, ProspectService}

import scala.concurrent.duration.Duration
import scala.concurrent.Await

/**
  * Created by Nacho on 23/09/2016.
  */

class LinkedinUserProfileController @Inject() (linkedinUserProfileService: LinkedinUserProfileService,
                                               scraper : LinkedinUserProfileScraper,
                                               prospectService: ProspectService) extends Controller{

  def search(id: String) = Action{
    val prospect : Prospect = Await.result(prospectService.find(id), Duration.Inf)

    runSearch(prospect)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll = Action{
    val prospects : Seq[Prospect] = Await.result(prospectService.all(), Duration.Inf)

    prospects.foreach(x=>runSearch(x))

    Redirect(routes.ProspectController.index(""))
  }

  private def runSearch(prospect: Prospect) : Unit = {
    val links: List[String] = LinkedInUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val profiles : Seq[LinkedinUserProfile] = links.map{x=>scraper.getLinkedinProfile(x,0)}.filter(_.isDefined).map(_.get)

    if(profiles.nonEmpty){
      val profile: LinkedinUserProfile = profiles.head
      Await.result(linkedinUserProfileService.save(profile), Duration.Inf)
      Await.result(prospectService.update(prospect.copy(linkedInProfile = profile)), Duration.Inf)
    }
  }

  def deleteProfile(id:String) = Action {
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect(routes.Application.homeFeed())
  }

}