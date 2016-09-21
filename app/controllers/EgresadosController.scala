package controllers

import java.util.UUID

import actions.SecureAction
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.GraduateForms.GraduateData
import models.{User, Graduate}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.data.Form
import play.api.data.Forms._

import play.api.mvc._
import services.{SessionService, UserService, GraduateService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController @Inject()(graduateService: GraduateService,
                                    sessionService: SessionService,
                                    secureAction: SecureAction,
                                    val messagesApi: MessagesApi) extends Controller {

  val graduateForm = Form(
    mapping(
      "id" -> text(),
      "firstName" -> text(),
      "lastName" -> text(),
      "dni" -> text(),
      "studentCode" -> text(),
      "birthday" -> text(),
      "entrydate" -> text(),
      "graduationdate" -> text(),
      "carreer" -> text()
    )(Graduate.apply)(Graduate.unapply)
  )

  def search = Action { implicit request => {
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    all onSuccess {
      case results: Seq[Graduate] => {
        println("Success")
        results.foreach { result => {

          graduates = graduates :+ result

        }
        }

      }

    }
    all onFailure {
      case _ => {
        println("Error")

      }
    }
    Await.ready(all, Duration.Inf)

    Ok(views.html.search.render(graduates, false))
  }
  }

  def doSearch = Action { implicit request => {
    val graduates = Seq[Graduate]()
    graduateService.all().foreach(g => graduates ++ g)
    Ok(views.html.search.render(graduates, true))
  }
  }

  def add = Action { implicit request => {
    Ok(views.html.addGraduate.render())
  }
  }

  def save = Action { implicit request => {
    Ok(views.html.index.render())
  }
  }

  def addGraduate = Action.async { implicit request =>
    try {
      val graduate = Graduate(
        UUID.randomUUID().toString,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("firstName").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("lastName").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("dni").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("studentcode").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("birthday").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("entryday").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("graduationday").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("career").get(0)

      )

      graduateService.save(graduate).map((_) => {
        val name = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("firstName").get(0)
        val surname = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("lastName").get(0)
        val dni = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("dni").get(0)
        val code = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("studentcode").get(0)
        val bday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("birthday").get(0)
        val eday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("entryday").get(0)
        val gday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("graduationday").get(0)
        val career = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("career").get(0)
        Ok(views.html.graduateProfile.render(name,surname,dni,code,bday,eday,gday,career,"Graduado creado correctamente!"))

      }).recoverWith {
        case e: MongoWriteException => Future {

          Forbidden
        }
        case e => Future {
          Forbidden
        }
      }
    } catch {
      case e: Exception => Future {
        //Ok(e.toString)
        BadRequest
      }
    }
  }

}
