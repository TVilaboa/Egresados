package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import actions.SecureAction
import com.google.inject.Inject
import generators.LinkedInUrlGeneratorObject
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
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
    try {
      val links: List[String] = LinkedInUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

      val profiles: List[LinkedinUserProfile] = links.map(x => scraper.getLinkedinProfile(x, 0)).filter(_.isDefined).map(_.get)
      val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val now: Date = Calendar.getInstance().getTime
      //TODO :: Logica duplicada en ScraperActor, para todos los scraps. Unificar!
      if (profiles.nonEmpty) {
        var updatedProfiles: List[LinkedinUserProfile] = List[LinkedinUserProfile]()
        var matchedValidOrAddNew = false
        for (profile <- profiles) {
          var matchedProfile = prospect.linkedInProfiles.find(p => p.profileUrl == profile.profileUrl)
          if (matchedProfile.isDefined) {
            val updatedProfile = profile.copy(_id = matchedProfile.get._id, rejected = matchedProfile.get.rejected, validated = matchedProfile.get.validated)
            linkedinUserProfileService.update(updatedProfile)
            updatedProfiles = updatedProfile :: updatedProfiles
            if (!matchedValidOrAddNew) {
              matchedValidOrAddNew = !matchedProfile.get.rejected
            }

          } else {
            linkedinUserProfileService.save(profile)
            updatedProfiles = profile :: updatedProfiles
            matchedValidOrAddNew = true
          }
        }


        if (matchedValidOrAddNew) {
          prospectService.update(prospect.copy(linkedInProfiles = updatedProfiles, updatedAt = format.format(now), errorDate = null))
        }
        else {
          prospectService.update(prospect.copy(errorDate = format.format(now)))
        }


      } else {
        prospectService.update(prospect.copy(errorDate = format.format(now)))
      }
    } catch {

      case e: Exception =>

        None
    }
  }

  def deleteProfile(id:String) =secureAction {
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect(routes.Application.homeFeed())
  }

}