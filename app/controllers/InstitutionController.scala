package controllers

import java.io.IOException
import java.util.UUID

import com.google.inject.Inject
import com.mongodb.MongoWriteException
import models.Institution
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.InstitutionService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by franco on 28/07/17.
  */
class InstitutionController  @Inject()(institutionService: InstitutionService,
                                       val messagesApi: MessagesApi) extends Controller with I18nSupport{

  val form: Form[Institution] = Form(mapping(
    "_id" -> text(),
    "name" -> text.verifying(_.nonEmpty),
    "address" -> text.verifying(_.nonEmpty))
  (Institution.apply)(Institution.unapply))

  def index = Action{
    val institutions : List[Institution] = Await.result(institutionService.all(), Duration.Inf).toList

    Ok(com.institutions.views.html.index.render(institutions))
  }

  def create = Action{
    val default: Map[String,String] = Map("_id"->"","name"->"","address"->"")
    Ok(com.institutions.views.html.create.render(default))
  }

  def store = Action.async{
    implicit request => {
      val uuid : String = UUID.randomUUID().toString

      val input : Map[String,String] = form.bindFromRequest().data

      if(form.bindFromRequest.hasErrors)
        Future{ BadRequest(com.institutions.views.html.create(input)) }
      else{
        val institution : Institution = Institution(uuid,input("name"),input("address"))

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

  def show(id: String) = Action {
    val institution : Institution = Await.result(institutionService.find(id), Duration.Inf)
    Ok(com.institutions.views.html.show(Option(institution)))
  }

  def edit(id: String) = Action{
    Ok("")
  }

  def update(id: String) = Action{
    implicit request => {
      Ok("")
    }
  }

  def delete(id: String) = Action{
    implicit request => {
      Ok("")
    }
  }

}
