package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actions.SecureAction
import com.google.inject.Inject
import generators.LinkedInUrlGeneratorObject
import models._
import play.api.mvc.{Action, Controller}
import scrapers.LinkedinUserProfileScraper
import services.{LinkedinUserProfileService, ProspectService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Nacho on 23/09/2016.
  */

class LinkedinUserProfileController @Inject() (linkedinUserProfileService: LinkedinUserProfileService,
                                               scraper : LinkedinUserProfileScraper,
                                               prospectService: ProspectService,secureAction: SecureAction) extends Controller{

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

  private def runSearch(prospect: Prospect) : Unit = {
    val links: List[String] = LinkedInUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val profiles : Seq[LinkedinUserProfile] = links.map(x=> scraper.getLinkedinProfile(x,0)).filter(_.isDefined).map(_.get)

    if(profiles.nonEmpty){
      val profile: LinkedinUserProfile = profiles.head

      val format : SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val now : Date = Calendar.getInstance().getTime

      if(profile.actualPosition.nonEmpty)
        linkedinUserProfileService.save(profile).map(x=> prospectService.update(prospect.copy(linkedInProfile = profile, updatedAt = format.format(now))))
      else
        linkedinUserProfileService.save(profile).map(x=> prospectService.update(prospect.copy(linkedInProfile = profile, errorDate = format.format(now))))
    }
  }

  def deleteProfile(id:String) =secureAction {
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect(routes.Application.homeFeed())
  }

}