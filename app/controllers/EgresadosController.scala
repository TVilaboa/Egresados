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
    val rawBody: JsValue = request.body.asJson.get
    try {
      val graduateData: GraduateData = rawBody.validate[GraduateData].get
      val graduate = Graduate(
        UUID.randomUUID().toString,
        "???????????????",
        graduateData.firstName,
        graduateData.lastName,
        graduateData.dni,
        graduateData.birthday+"/"+graduateData.birthmonth+"/"+graduateData.birthyear,
        graduateData.entryday+"/"+graduateData.entrymonth+"/"+graduateData.entryyear,
        graduateData.graduationday+"/"+graduateData.graduationmonth+"/"+graduateData.graduationyear,
        graduateData.carreer
      )

      graduateService.save(graduate).map((_) => {
        Ok
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
