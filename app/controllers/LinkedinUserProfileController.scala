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

  def saveLinkedinUserProfile = Action {
    val generator: LinkedInUrlGenerator = new LinkedInUrlGenerator()
    var graduate : Graduate = Await.result(graduateService.findByDocumentId("12345678"),Duration.Inf)
    val link: List[String] = generator.getSearchedUrl(Option("Franco Testori"),Option("Universidad Austral"))
    val scraper : LinkedinUserProfileScraper = new LinkedinUserProfileScraper()
    var linkedinUserProfile: LinkedinUserProfile = null
    link.map{link : String =>
      linkedinUserProfile = scraper.getLinkedinProfile(link)
      linkedinUserProfileService.save(linkedinUserProfile)
    }

    graduate = graduate.copy(linkedinUserProfile = Option(linkedinUserProfile).get)
    var result = Await.result(graduateService.update(graduate),Duration.Inf)

    Ok
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
        //var linkedinUserProfile = scraper.getLinkedinProfile(link)
        //linkedinUserProfileService.save(linkedinUserProfile)

        linkedinUserProfile = scraper.getLinkedinProfile(link)


      }
      if (linkedinUserProfile != null) {
        var graduate = grad.copy(linkedinUserProfile = linkedinUserProfile)
        var result = Await.result(graduateService.update(graduate), Duration.Inf)
      }
    }


      Ok(views.html.index.render("Success"))
  }

}