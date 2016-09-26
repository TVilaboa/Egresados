package controllers

import java.sql.Timestamp
import java.util.Date
import play.data.format.Formats.DateTime

import com.google.inject.Inject
import generators.LinkedInUrlGenerator
import models.{LinkedinUserProfile,LinkedinEducation,LinkedinJob}
import play.api.mvc.{Action, Controller}
import scrapers.LinkedinUserProfileScraper
import services.LinkedinUserProfileService


import scala.collection.mutable.ListBuffer

/**
  * Created by Nacho on 23/09/2016.
  */

class LinkedinUserProfileController @Inject() (linkedinUserProfileService: LinkedinUserProfileService) extends Controller{

  def saveLinkedinUserProfile = Action {
    val generator: LinkedInUrlGenerator = new LinkedInUrlGenerator()
    val link: List[String] = generator.getSearchedUrl(Option("Franco Testori"),Option("Universidad Austral"))
    val scraper : LinkedinUserProfileScraper = new LinkedinUserProfileScraper()
    link.map{link : String =>
      var linkedinUserProfile = scraper.getLinkedinProfile(link)
      linkedinUserProfileService.save(linkedinUserProfile)
    }

    Ok
  }

}