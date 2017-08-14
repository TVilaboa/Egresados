package controllers

import java.io.IOException
import java.util.UUID

import actions.SecureAction
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import enums.EnumPlayUtils._
import enums.{InstitutionSector, InstitutionType}
import models.{Institution, Prospect}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Controller
import services.{InstitutionService, ProspectService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
/**
  * Created by franco on 28/07/17.
  */
class InstitutionController @Inject()(institutionService: InstitutionService, prospectService: ProspectService, secureAction: SecureAction,
                                      val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val form: Form[Institution] = Form(mapping(
    "_id" -> text(),
    "name" -> text.verifying(_.nonEmpty),
    "address" -> text.verifying(_.nonEmpty),
    "active" -> boolean,
    "institutionType" -> enum(InstitutionType),
    "sector" -> enum(InstitutionSector))
  (Institution.apply)(Institution.unapply))

  def index =secureAction{
    val institutions : List[Institution] = Await.result(institutionService.all(), Duration.Inf).toList

    Ok(com.institutions.views.html.index.render(institutions))
  }

  def create =secureAction{
    val default: Map[String, String] = Map("_id" -> "", "name" -> "", "address" -> "", "institutionType" -> "", "sector" -> "")
    Ok(com.institutions.views.html.create.render(default))
  }

  def store =secureAction.async{
    implicit request => {
      val uuid : String = UUID.randomUUID().toString

      val input : Map[String,String] = form.bindFromRequest().data

      if(form.bindFromRequest.hasErrors)
        Future{ BadRequest(com.institutions.views.html.create(input)) }
      else{
        val institution: Institution = Institution(uuid, input("name"), input("address"), active = true, InstitutionType.withName(input("institutionType")), InstitutionSector.withName(input("sector")))

        try{
          institutionService.save(institution).map((_) => {
            Redirect(s"$uuid")
          }).recoverWith {
            case e: MongoWriteException => Future {Forbidden}
            case e => Future {Forbidden}
          }
        }
        catch{
          case e: IOException => Future{Redirect(request.headers("referer"))}
        }
      }
    }
  }

  def show(id: String) =secureAction {
    val prospects: List[Prospect] = Await.result(prospectService.findByInstitution(id), Duration.Inf).toList
    val institution : Institution = Await.result(institutionService.find(id), Duration.Inf)
    Ok(com.institutions.views.html.show(Option(institution), prospects))
  }

  def edit(id: String) =secureAction{
    val institution : Institution = Await.result(institutionService.find(id), Duration.Inf)
    Ok(com.institutions.views.html.edit.render(institution.toMap))
  }

  def update(id: String) =secureAction{
    implicit request => {
      val input : Map[String,String] = form.bindFromRequest().data

      if(form.bindFromRequest.hasErrors)
        BadRequest(com.institutions.views.html.edit(input))

      institutionService.update(form.bindFromRequest.get)

      Redirect(routes.InstitutionController.show(input("_id")))
    }
  }

  def delete(id: String) =secureAction{
    implicit request => {
      val institution : Institution = Await.result(institutionService.find(id), Duration.Inf)
      institutionService.drop(institution)
      Redirect(routes.InstitutionController.index())
    }
  }

}
