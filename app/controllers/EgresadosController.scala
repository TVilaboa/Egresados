package controllers

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.google.inject.Inject
import models.Graduate
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.{GraduateService, UserService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController @Inject()(graduateService: GraduateService) extends Controller {

  val graduateForm = Form(
    mapping(
      "_id"-> text(),
      "firstName" -> text(),
      "lastName" -> text(),
      "documentId" -> text(),
      "studentCode" -> text(),
      "birthDate" -> text(),
      "entryDate" -> text(),
      "graduationDate" -> text(),
      "career" -> text()
    )(Graduate.apply)(Graduate.unapply)
  )

  def showSearchForm = Action{
    var graduates = Seq[Graduate]()
    Ok(views.html.search.render(graduates, graduateForm,true))
  }

  def search = Action { implicit request => {
    var graduates = Seq[Graduate]()

    val firstname = graduateForm.bindFromRequest.data("firstName")
    val lastname = graduateForm.bindFromRequest.data("lastName")
    val gradDate = graduateForm.bindFromRequest.data("graduationDate")
    val career = graduateForm.bindFromRequest.data("career")


    val all: Future[Seq[Graduate]] = graduateService.all()
    all onSuccess  {
      case results: Seq[Graduate] => {
        println("Success")
        results.foreach { result => {

          graduates = graduates :+ result

        } }

      }

    }
    all onFailure {
      case _ => {
        println("Error")

      }
    }

    Await.ready(all,Duration.Inf)

    if(firstname.nonEmpty)
      graduates = graduates.filter(x => x.firstName.toLowerCase.contains(firstname.toLowerCase))
    if(lastname.nonEmpty)
      graduates = graduates.filter(x => x.lastName.toLowerCase.contains(lastname.toLowerCase))
    if(gradDate.nonEmpty)
      graduates = graduates.filter(x => x.graduationDate.toLowerCase.contains(gradDate.toLowerCase))
    if(career.nonEmpty)
      graduates = graduates.filter(x => x.career.toLowerCase.contains(career.toLowerCase))

    Ok(views.html.search.render(graduates, graduateForm, false))
  }
  }

}
