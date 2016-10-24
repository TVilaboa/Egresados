package controllers

import actions.SecureAction
import com.google.inject.Inject
import models.{Graduate, InfobaeNews}
import play.api.http.ContentTypes
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by sebi on 24/10/16.
  */
class RestApiController @Inject()(graduateService: GraduateService,
                                  sessionService: SessionService,
                                  secureAction: SecureAction,
                                  newsInfobaeService: InfobaeNewsService,
                                  newsLaNacionService: LaNacionNewsService,
                                  linkedinUserProfileService: LinkedinUserProfileService,
                                  val messagesApi: MessagesApi) extends Controller {

  def getAllInfobaeData = Action { implicit request => {
    var info= Seq[InfobaeNews]()
    val all: Future[Seq[InfobaeNews]] = newsInfobaeService.all()
    info = Await.result(all,Duration.Inf)
//    Ok(Json.toJson(info))
    Ok(info.toString)
    }
  }

  def getInfobaeData(id:String){
    val find: Future[InfobaeNews] = newsInfobaeService.find(id)
    var one = Await.result(find,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(one)))
  }


  def getAllEgresadosData = Action { implicit request => {
    var info = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    info = Await.result(all,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(info)))
  }}
}
