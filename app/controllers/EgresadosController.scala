package controllers

import java.util.UUID

import actions.SecureAction
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.GraduateForms.GraduateData
import io.netty.util.Mapping
import models.{LaNacionNews, Graduate, User}
import play.api.i18n.MessagesApi
import play.api.libs.json.JsValue
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.{GraduateService, SessionService, UserService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController @Inject()(graduateService: GraduateService,sessionService: SessionService,
                                    secureAction: SecureAction,
                                    val messagesApi: MessagesApi) extends Controller {

  val graduateForm = Form(
    mapping(
      "_id"-> text(),
      "firstName" -> text(),
      "lastName" -> text(),
      "documentId" -> text(),
      "birthDate" -> text(),
      "entryDate" -> text(),
      "graduationDate" -> text(),
      "career" -> text(),
      "studentCode" -> text(),
      "lanacionNews" -> list(mapping("_id" -> text(),
      "url" -> text(),
      "title" -> text(),
      "date" -> text(),
      "tuft" -> text(),
      "author" -> text())(LaNacionNews.apply)(LaNacionNews.unapply))
    )(Graduate.apply)(Graduate.unapply)
  )

  def showSearchForm = Action{
    var graduates = Seq[Graduate]()


    val all: Future[Seq[Graduate]] = graduateService.all()


    graduates = Await.result(all,Duration.Inf)
    Ok(views.html.search.render(graduates, graduateForm,true,null,null,null,null))
  }

  def search = Action { implicit request => {
    var graduates = Seq[Graduate]()

    val firstname = graduateForm.bindFromRequest.data("firstName")
    val lastname = graduateForm.bindFromRequest.data("lastName")
    val gradDate = graduateForm.bindFromRequest.data("graduationDate")
    val career = graduateForm.bindFromRequest.data("career")


    val all: Future[Seq[Graduate]] = graduateService.all()

    graduates = Await.result(all,Duration.Inf)

    if(firstname.nonEmpty)
      graduates = graduates.filter(x => x.firstName.toLowerCase.contains(firstname.toLowerCase))
    if(lastname.nonEmpty)
      graduates = graduates.filter(x => x.lastName.toLowerCase.contains(lastname.toLowerCase))
    if(gradDate.nonEmpty)
      graduates = graduates.filter(x => x.graduationDate.toLowerCase.contains(gradDate.toLowerCase))
    if(career.nonEmpty)
      graduates = graduates.filter(x => x.career.toLowerCase.contains(career.toLowerCase))

    Ok(views.html.search.render(graduates, graduateForm, false,firstname,lastname,gradDate,career))
  }
  }

  def showGraduateForm = Action { implicit request => {
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
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("firstName").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("lastName").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("dni").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("studentcode").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("birthday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("entryday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("graduationday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("career").head,
        null

      )

      graduateService.save(graduate).map((_) => {
//        val name = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("firstName").get(0)
//        val surname = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("lastName").get(0)
//        val dni = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("dni").get(0)
//        val code = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("studentcode").get(0)
//        val bday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("birthday").get(0)
//        val eday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("entryday").get(0)
//        val gday = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("graduationday").get(0)
//        val career = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("career").get(0)
        Redirect("/profile/" + graduate._id)
//        Ok(views.html.graduateProfile.render(name,surname,dni,code,bday,eday,gday,career,"Graduado creado correctamente!"))
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

  def showProfile(id:String) = Action {
    try{
      var graduate: Option[Graduate] = None
      val result: Future[Graduate] = graduateService.find(id)
      result onSuccess {
        case grad: Graduate => {
          println("Success")
          graduate = Option(grad)
        }
      }
      result onFailure {
        case _ => {
          println("Error")

        }
      }
      Await.ready(result, Duration.Inf)
      Ok(views.html.graduateProfile.render(graduate.get))

    }
  }
}
