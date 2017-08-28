package controllers

import com.google.inject.Inject
import models._
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by sebi on 24/10/16.
  */
class RestApiController @Inject()(prospectService: ProspectService,
                                  sessionService: SessionService,

                                  newsInfobaeService: InfobaeNewsService,
                                  newsLaNacionService: LaNacionNewsService,
                                  linkedinUserProfileService: LinkedinUserProfileService,
                                  val messagesApi: MessagesApi) extends Controller {

  def getAllInfobaeData =Action { implicit request => {
    var info = List[News]()
    var graduates = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
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

  def getAllProspectsData = Action { implicit request => {
    var info = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    info = Await.result(all,Duration.Inf)
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllLaNacionData =Action { implicit request => {
    var info = List[News]()
    var graduates = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    graduates = Await.result(all,Duration.Inf)
    for (prospect <- graduates) {
      info = info ::: prospect.nacionNews
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllClarinData =Action { implicit request => {
    var info = List[News]()
    var graduates = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    graduates = Await.result(all,Duration.Inf)
    for (prospect <- graduates) {
      info = info ::: prospect.clarinNews
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllElCronistaData =Action { implicit request => {
    var info = List[News]()
    var prospects = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    prospects = Await.result(all, Duration.Inf)
    for (prospect <- prospects) {
      info = info ::: prospect.cronistaNews
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}

  def getAllLinkedInData =Action { implicit request => {
    var info = List[LinkedinUserProfile]()
    var prospects = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    prospects = Await.result(all, Duration.Inf)
    for (prospect <- prospects) {
      info = prospect.linkedInProfiles ::: info
    }
    if(info.isEmpty){
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    }else{
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(info)) + "}")
    }
  }}


  def getOneInfobaeData(id:String) =Action {implicit request =>{
    val find = prospectService.find(id)
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

  def getOneLaNacionData(id:String) =Action {implicit request =>{
    val find = prospectService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if (one.nacionNews.isEmpty) {
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.nacionNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}

  def getOneClarinData(id:String) =Action {implicit request =>{
    val find = prospectService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if(one.clarinNews.isEmpty){
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.clarinNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}

  def getOneElCronistaData(id:String) =Action {implicit request =>{
    val find = prospectService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if (one.cronistaNews.isEmpty) {
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.cronistaNews)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}


  def getOneLinkedinData(id:String) =Action {implicit request =>{
    val find = prospectService.find(id)
    try {
      val one = Await.result(find, Duration.Inf)
      if (one.linkedInProfiles.isEmpty) {
        Ok("{\"type\":\"success\",\"value\": \"The graduate exists, but there is no data\" }")
      }else{
        Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(find.value.get.get.linkedInProfiles)) + "}")
      }
    }catch{
      case e: Exception => Ok("{\"type\":\"error\",\"value\": \"The graduate couldn't be found\" }")
    }
  }}


  def getAllProspectsByCareer(career: String) = Action { implicit request => {
    var info = Seq[Prospect]()
    val all: Future[Seq[Prospect]] = prospectService.all()
    info = Await.result(all,Duration.Inf)
    val selectInfo = info.filter(_.title.toLowerCase.contains(career.toLowerCase))
    if(selectInfo.isEmpty) {
      Ok("{\"type\":\"success\",\"value\": \"There is no data\" }")
    } else {
      Ok("{\"type\":\"success\",\"value\": " + Json.prettyPrint(Json.toJson(selectInfo)) + "}")
    }
  }
  }
}
