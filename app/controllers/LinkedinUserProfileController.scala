package controllers


import com.google.inject.Inject
import generators.LinkedInUrlGenerator
import models.{Graduate, LinkedinUserProfile, LinkedinEducation, LinkedinJob}
import play.api.mvc.{Action, Controller}
import scrapers.LinkedinUserProfileScraper
import services.{GraduateService, LinkedinUserProfileService}


import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Nacho on 23/09/2016.
  */

class LinkedinUserProfileController @Inject() (linkedinUserProfileService: LinkedinUserProfileService,graduateService: GraduateService) extends Controller{

  def saveLinkedinUserProfile(id : String) = Action {
    val generator: LinkedInUrlGenerator = new LinkedInUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val link: List[String] = generator.getSearchedUrl(Option(graduate.firstName + " " +graduate.lastName),Option("Universidad Austral"))
    val scraper : LinkedinUserProfileScraper = new LinkedinUserProfileScraper()
    var linkedinUserProfile: Option[LinkedinUserProfile] = None
    link.map{link : String =>
      linkedinUserProfile = scraper.getLinkedinProfile(link,0)
      if(linkedinUserProfile.isDefined)
        linkedinUserProfileService.save(linkedinUserProfile.get)
    }
    if(linkedinUserProfile.isDefined){
      graduate = graduate.copy(linkedinUserProfile = linkedinUserProfile.get)
      Await.result(graduateService.update(graduate),Duration.Inf)
    }

    Redirect("/profile/" + graduate._id)

  }

  def saveAllLinkedinUserProfile = Action {
    val generator: LinkedInUrlGenerator = new LinkedInUrlGenerator()
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    graduates = Await.result(all,Duration.Inf)
    for(grad <- graduates) {
      val link: List[String] = generator.getSearchedUrl(Option(grad.firstName + " " + grad.lastName), Option("Universidad Austral"))
      val scraper: LinkedinUserProfileScraper = new LinkedinUserProfileScraper()
      var linkedinUserProfile: Option[LinkedinUserProfile] = None
      link.foreach { link: String =>
        val opLinkedinUserProfile = scraper.getLinkedinProfile(link,0)

        if (opLinkedinUserProfile.isDefined){
          linkedinUserProfile = opLinkedinUserProfile
        }
      }
      if (linkedinUserProfile.isDefined) {
        val graduate = grad.copy(linkedinUserProfile = linkedinUserProfile.get)
        Await.result(graduateService.update(graduate), Duration.Inf)
      }
    }
      Ok(views.html.index.render("", "", 0, "", 0))
  }

  def deleteProfile(id:String) = Action {
    //Get graduate from DB.
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect("/")
  }

}