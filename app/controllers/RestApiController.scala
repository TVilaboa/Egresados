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
    var info = List[InfobaeNews]()
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    graduates = Await.result(all,Duration.Inf)
    for(grad <- graduates){
      info =  info ::: grad.infobaeNews
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
    }
  }

  def getAllEgresadosData = Action { implicit request => {
    var info = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    info = Await.result(all,Duration.Inf)
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllLaNacionData = Action { implicit request => {
    var info = List[LaNacionNews]()
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    graduates = Await.result(all,Duration.Inf)
    for(grad <- graduates){
      info =  info ::: grad.laNacionNews
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllLinkedInData = Action { implicit request => {
    var info = List[LinkedinUserProfile]()
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    graduates = Await.result(all,Duration.Inf)
    for(grad <- graduates){
      info =  grad.linkedinUserProfile :: info
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}


  def getOneInfobaeData(id:String) = Action {implicit request =>{
    val find = graduateService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if(one.infobaeNews.isEmpty){
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.infobaeNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}

  def getOneLaNacionData(id:String) = Action {implicit request =>{
    val find= graduateService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if(one.laNacionNews.isEmpty){
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.laNacionNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}


  def getOneLinkedinData(id:String) = Action {implicit request =>{
    val find= graduateService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if(one.linkedinUserProfile == null){
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.linkedinUserProfile)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}


  def getAllEgresadosByCareer(career : String)= Action { implicit request => {
    var info= Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    info = Await.result(all,Duration.Inf)
    val selectInfo = info.filter(_.career.toLowerCase.contains(career.toLowerCase))
    if(selectInfo.isEmpty) {
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    } else {
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(selectInfo)) + "}")
    }
  }
  }
}
