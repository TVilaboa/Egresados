package controllers

import java.sql.Timestamp
import java.util.Date
import play.data.format.Formats.DateTime

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
    var linkedinUserProfile: LinkedinUserProfile = null
    link.map{link : String =>
      if (!scraper.getLinkedinProfile(link).equals(None)) {
        linkedinUserProfile = scraper.getLinkedinProfile(link).get
        linkedinUserProfileService.save(linkedinUserProfile)
      }
    }
    if(Option(linkedinUserProfile).isDefined){
      graduate = graduate.copy(linkedinUserProfile = Option(linkedinUserProfile).get)
      var result = Await.result(graduateService.update(graduate),Duration.Inf)
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
      var linkedinUserProfile: LinkedinUserProfile = null
      link.foreach { link: String =>

        var opLinkedinUserProfile = scraper.getLinkedinProfile(link)
        if (!opLinkedinUserProfile.equals(None)){
          linkedinUserProfile = opLinkedinUserProfile.get
        }
      }
      if (linkedinUserProfile != null) {
        val graduate = grad.copy(linkedinUserProfile = linkedinUserProfile)
        Await.result(graduateService.update(graduate), Duration.Inf)
      }
    }
      Ok(views.html.index.render("Success"))
  }

  def deleteProfile(id:String) = Action {
    //Get graduate from DB.
    val profile : LinkedinUserProfile = Await.result(linkedinUserProfileService.find(id),Duration.Inf)
    linkedinUserProfileService.drop(profile)
    Redirect("/")
  }

}