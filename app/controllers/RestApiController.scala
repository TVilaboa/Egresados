package controllers

import actions.SecureAction
import com.google.inject.Inject
import models.{LinkedinUserProfile, LaNacionNews, Graduate, InfobaeNews}
import play.api.http.ContentTypes
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services._
import scala.concurrent.ExecutionContext.Implicits.global

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
    Ok(Json.prettyPrint(Json.toJson(info)))
    }
  }

  def getAllEgresadosData = Action { implicit request => {
    var info = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    info = Await.result(all,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(info)))
  }}

  def getAllLaNacionData = Action { implicit request => {
    var info = Seq[LaNacionNews]()
    val all: Future[Seq[LaNacionNews]] = newsLaNacionService.all()
    info = Await.result(all,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(info)))
  }}

  def getAllLinkedInData = Action { implicit request => {
    var info = Seq[LinkedinUserProfile]()
    val all: Future[Seq[LinkedinUserProfile]] = linkedinUserProfileService.all()
    info = Await.result(all,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(info)))
  }}


  def getOneInfobaeData(id:String) = Action {implicit request =>{
    val find = graduateService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if(one.infobaeNews.isEmpty){
        Ok("{ \"type\" : \" error \" , \" value\" : \" the graduate exists, but there is no data regarding infobaeNews\" }")
      }else{
        Ok("{ \"type\" : \" error \" , \" value\" : " + Json.prettyPrint(Json.toJson(find.value.get.get.infobaeNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{ \"type\" : \" error \" , \" value\" : \" the graduate couldn't be found\" }")
    }
  }}

  def getOneLaNacionData(id:String) = Action {implicit request =>{
    val find= graduateService.find(id)
    val one = Await.result(find, Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(one.laNacionNews)))
  }}


  def getOneLinkedinData(id:String) = Action {implicit request =>{
    val find= graduateService.find(id)
    val one = Await.result(find, Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(one.linkedinUserProfile)))
  }}


  def getAllEgresadosByCareer(career : String)= Action { implicit request => {
    var info= Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    info = Await.result(all,Duration.Inf)
    Ok(Json.prettyPrint(Json.toJson(info.filter(_.career.equals(career)))))
  }
  }
}
