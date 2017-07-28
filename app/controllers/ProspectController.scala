package controllers

import com.google.inject.Inject
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import services.ProspectService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by franco on 27/07/17.
  */
class ProspectController @Inject()(prospectService: ProspectService) extends Controller{

  val form = Form(mapping(
    "_id" -> text(),
    "firstName" -> text(),
    "lastName" -> text(),
    "documentId" -> text(),
    "birthDate" -> text(),
    "entryDate" -> text(),
    "exitDate" -> text(),
    "institution"-> mapping("_id" -> text(),
                            "name" -> text(),
                            "address" -> text())(Institution.apply)(Institution.unapply),
    "title" -> text(),
    "institutionCode" -> text(),
    "nacionNews" -> list(mapping("_id" -> text(),
      "url" -> text(),
      "title" -> text(),
      "date" -> text(),
      "tuft" -> text(),
      "author" -> text())(News.apply)(News.unapply)),
    "infobaeNews" -> list(mapping("_id" -> text(),
      "url" -> text(),
      "title" -> text(),
      "date" -> text(),
      "tuft" -> text(),
      "author" -> text())(News.apply)(News.unapply)),
    "clarinNews" -> list(mapping("_id" -> text(),
      "url" -> text(),
      "title" -> text(),
      "date" -> text(),
      "tuft" -> text(),
      "author" -> text())(News.apply)(News.unapply)),
    "cronistaNews" -> list(mapping("_id" -> text(),
      "url" -> text(),
      "title" -> text(),
      "date" -> text(),
      "tuft" -> text(),
      "author" -> text())(News.apply)(News.unapply)),
    "linkedInProfile" -> mapping("_id" -> text(),
      "actualPosition" -> text(),
      "jobList" -> list(mapping("_id" -> text(),
        "position" -> text(),
        "workPlace" -> text(),
        "workUrl" -> text(),
        "activityPeriod" -> text(),
        "jobDescription" -> text()) (LinkedinJob.apply) (LinkedinJob.unapply)),
      "educationList" -> list(mapping("_id" -> text(),
        "institute" -> text(),
        "instituteUrl" -> text(),
        "title" -> text(),
        "educationPeriod" -> text(),
        "educationDescription" -> text()) (LinkedinEducation.apply) (LinkedinEducation.unapply)),
      "profileUrl" -> text()) (LinkedinUserProfile.apply) (LinkedinUserProfile.unapply),
    "country" -> default(text,"")
  )(Prospect.apply)(Prospect.unapply))

  def index = Action{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    Ok(com.prospects.views.html.index.render(prospects, form, true, Map[String,String]()))
  }

  def search = Action{
    implicit request =>{
      val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

      Ok(com.prospects.views.html.index.render(prospects, form, true, Map[String,String]()))
    }
  }

  def create = Action{
    Ok("")
  }

  def store = Action{
    implicit request =>{
      Ok("")
    }
  }

  def show(id : String) = Action{
    Ok("")
  }

  def edit(id : String) = Action{
      Ok("")
  }

  def update(id : String) = Action{
    implicit request => {
      Ok("")
    }
  }

  def delete(id : String) = Action{
    implicit request => {
      Ok("")
    }
  }

  def createBatch = Action{
    Ok("")
  }

  def storeBatch = Action{
    implicit request => {
      Ok("")
    }
  }

}
