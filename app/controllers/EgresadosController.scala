package controllers

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import actions.SecureAction
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.github.tototoshi.csv.CSVReader
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.AuthForms.{LoginData, SignupData}
import forms.GraduateForms.GraduateData
import io.netty.util.Mapping
import models._
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import play.api.libs.json.{JsValue, Json}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController @Inject()(graduateService: GraduateService,sessionService: SessionService,
                                    secureAction: SecureAction, laNacionNewsService: LaNacionNewsService,
                                    linkedinUserProfileService: LinkedinUserProfileService,
                                    infobaeNewsService: InfobaeNewsService,
                                    val messagesApi: MessagesApi) extends Controller {

  val graduateForm = Form(
    mapping(
      "_id" -> text(),
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
        "author" -> text())(News.apply)(News.unapply)),
      "infobaeNews" -> list(mapping("_id" -> text(),
        "url" -> text(),
        "title" -> text(),
        "date" -> text(),
        "tuft" -> text(),
        "author" -> text())(News.apply)(News.unapply)),
      "clarinNews" -> list(mapping("_id" -> text(),
        "url" -> text(),
        "title" -> text(),
        "date" -> text(),
        "tuft" -> text(),
        "author" -> text())(News.apply)(News.unapply)),
      "elCronistaNews" -> list(mapping("_id" -> text(),
        "url" -> text(),
        "title" -> text(),
        "date" -> text(),
        "tuft" -> text(),
        "author" -> text())(News.apply)(News.unapply)),
      "linkedinUserProfile" -> mapping("_id" -> text(),
        "actualPosition" -> text(),
        "jobList" -> list(mapping("_id" -> text(),
          "position" -> text(),
          "workPlace" -> text(),
          "workUrl" -> text(),
          "activityPeriod" -> text(),
          "jobDescription" -> text()) (LinkedinJob.apply) (LinkedinJob.unapply)),
        "educationList" -> list(mapping("_id" -> text(),
          "institute" -> text(),
          "instituteUrl" -> text(),
          "title" -> text(),
          "educationPeriod" -> text(),
          "educationDescription" -> text()) (LinkedinEducation.apply) (LinkedinEducation.unapply)),
        "profileUrl" -> text()) (LinkedinUserProfile.apply) (LinkedinUserProfile.unapply)
    )(Graduate.apply)(Graduate.unapply)
  )

  def showSearchForm = Action{

    val graduates : List[Graduate]= Await.result(graduateService.all(), Duration.Inf).toList

    Ok(views.html.search.render(graduates, graduateForm, true, Map[String,String]()))
  }

  def search = Action { implicit request => {

    val firstName : String = graduateForm.bindFromRequest.data("firstName")
    val lastName : String = graduateForm.bindFromRequest.data("lastName")
    val gradDate : String = graduateForm.bindFromRequest.data("graduationDate")
    val career : String = graduateForm.bindFromRequest.data("career")
    val identification : String = graduateForm.bindFromRequest.data("identification")
    val studentCode : String = graduateForm.bindFromRequest.data("studentCode")

    var graduates: List[Graduate] = Await.result(graduateService.all(), Duration.Inf).toList

    if(firstName.nonEmpty)
      graduates = graduates.filter(x => x.firstName.toLowerCase.contains(firstName.toLowerCase))
    if(lastName.nonEmpty)
      graduates = graduates.filter(x => x.lastName.toLowerCase.contains(lastName.toLowerCase))
    if(gradDate.nonEmpty){
      val format = new SimpleDateFormat("dd/MM/yyyy")
      val date = new DateTime(gradDate).toDate
      graduates = graduates.filter(x => x.graduationDate.toLowerCase.contains(format.format(date).toLowerCase))
    }
    if(career.nonEmpty)
      graduates = graduates.filter(x => x.career.toLowerCase.contains(career.toLowerCase))
    if(identification.nonEmpty)
      graduates = graduates.filter(x => x.documentId.toLowerCase.contains(identification.toLowerCase))
    if(studentCode.nonEmpty)
      graduates = graduates.filter(x => x.studentCode.toLowerCase.contains(studentCode.toLowerCase))

    Ok(views.html.search.render(graduates, graduateForm, false, graduateForm.bindFromRequest.data))
  }
  }

  def showGraduateForm = Action { implicit request => {
    Ok(views.html.addGraduate.render())
  }
  }

  def save = secureAction { implicit request => {
    Redirect("/")
  }
  }

  def renderValidate(id:String) = Action {
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
      Ok(views.html.validateGraduateLinks.render(graduate))
  }


  def addGraduate = Action.async { implicit request =>

    try {
      val graduate = Graduate(
        UUID.randomUUID().toString,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("firstName").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("lastName").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("dni").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("birthday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("entryday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("graduationday").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("career").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("studentcode").head,
        List[News](),
        List[News](),
        List[News](),
        List[News](),
        LinkedinUserProfile(UUID.randomUUID().toString,
          "",
          List[LinkedinJob](),
          List[LinkedinEducation](),
          ""
        )
      )

      graduateService.save(graduate).map((_) => {
        Ok(Json.toJson(graduate))
      }).recoverWith {
        case e: MongoWriteException => Future {
          Forbidden
        }
        case e => Future {
          Forbidden
        }
      }
    } catch {
      case e: IOException => Future {
        Ok("{body:\"IOException happended\"}")
      }
    }
  }

  def showProfile(id:String) = Action {
      var graduate: Option[Graduate] = None
      val result: Future[Graduate] = graduateService.find(id)
      result onSuccess {
        case grad: Graduate => {
          println("Success")
        }
      }
      result onFailure {
        case _ => {
          println("Error")

        }
      }
      graduate = Option(Await.result(result, Duration.Inf))
      Ok(views.html.graduateProfile.render(graduate))
  }

  def showUpdatingForm (id:String) = Action {
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
    graduate = Option(Await.result(result, Duration.Inf))
    Ok(views.html.updateGraduate.render(graduate.get))
  }

  def update (id:String) = Action { implicit request =>

    val graduate = Graduate(
      id,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("firstName").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("lastName").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("dni").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("birthday").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("entryday").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("graduationday").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("career").head,
      request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("studentcode").head,
      List[News](),
      List[News](),
      List[News](),
      List[News](),
      LinkedinUserProfile(UUID.randomUUID().toString,
        "",
        List[LinkedinJob](),
        List[LinkedinEducation](),
        ""
      )
    )
    val updatedGraduate: Graduate = mergeGraduate(graduate)

    Await.result(graduateService.update(updatedGraduate),Duration.Inf)
    Redirect("/profile/" + graduate._id)


  }


  def mergeGraduate (graduate: Graduate): Graduate ={


    val id = graduate._id
    var name = graduate.firstName
    var lastName = graduate.lastName
    var dni = graduate.documentId
    var code = graduate.studentCode
    var bday = graduate.birthDate
    var eday = graduate.entryDate
    var gday = graduate.graduationDate
    var career = graduate.career


    val original: Graduate = Await.result(graduateService.find(id), Duration.Inf)
    val laNacionNews = original.laNacionNews
    val infobaeNews = original.infobaeNews
    val clarinNews = original.clarinNews
    val elCronistaNews = original.elCronistaNews
    val linkedInData = original.linkedinUserProfile

    if(name == "") name = original.firstName
    if(lastName == "") lastName = original.lastName
    if(dni == "") dni = original.documentId
    if(code == "") code = original.studentCode
    if(bday == "") bday = original.birthDate
    if(eday == "") eday = original.entryDate
    if(gday == "") gday = original.graduationDate
    if(career == "") career = original.career

    val updatedGraduate = Graduate(
      id,
      name,
      lastName,
      dni,
      bday,
      eday,
      gday,
      career,
      code,
      laNacionNews,
      infobaeNews,
      clarinNews,
      elCronistaNews,
      linkedInData
    )
    updatedGraduate
  }

  def showProfileTest = Action {
    Ok(views.html.graduateProfile.render(Option(Await.result(graduateService.find("456"),Duration.Inf))))
  }

  def getLinkedInUrlStats = secureAction{
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    graduates = Await.result(all,Duration.Inf)
    val links : Seq[(String,String,String,String)] = Await.result(graduateService.getNumberWithLinks, Duration.Inf)

    Ok(views.html.links(links,graduates))
  }

  def showimportCSV = secureAction { implicit request => {
    Ok(views.html.importCSV.render(Seq[Graduate](),""))
  }
  }

  def importCSV = Action(parse.multipartFormData) { implicit request => {
    request.body.file("csv").map { csv =>
      val filename = csv.filename
      val contentType = csv.contentType
      val csvFile = csv.ref.file

      val reader = CSVReader.open(csvFile)
      val info = reader.allWithHeaders()
      var graduatesCSV : List[Graduate] = List[Graduate]()

      var message : String = "You need to upload a file!"

      if(info.nonEmpty)
        message = ""
      for(l <- info){
        val firstName = l.getOrElse("Nombre", "")
        val lastName = l.getOrElse("Apellido", "")
        val documentId = l.getOrElse("DNI", "")
        val birthDate = l.getOrElse("Fecha de nacimiento", "")
        val studentCode = l.getOrElse("Legajo","")

        if(!documentId.equals("")) {
          graduatesCSV = Graduate("",
            firstName,
            lastName,
            documentId,
            birthDate,
            "",
            "",
            "",
            studentCode,
            List[News](),
            List[News](),
            List[News](),
            List[News](),
            LinkedinUserProfile(UUID.randomUUID().toString,
              "",
              List[LinkedinJob](),
              List[LinkedinEducation](),
              ""
            )
          ) :: graduatesCSV

            var graduateDB: Option[Graduate] = None
            val result: Future[Graduate] = graduateService.findByDocumentId(documentId)
            result onSuccess {
              case grad: Graduate => {
                graduateDB = Option(grad)
                val isGraduteInDB : Boolean = graduateDB.isDefined
                if(isGraduteInDB){
                  graduateService.update(Graduate(graduateDB.get._id,firstName, lastName, documentId, birthDate, "", "", "", studentCode, graduateDB.get.laNacionNews,graduateDB.get.infobaeNews,graduateDB.get.clarinNews,graduateDB.get.elCronistaNews,graduateDB.get.linkedinUserProfile))
                }
                else {
                  val graduate : Graduate = Graduate(UUID.randomUUID().toString, firstName, lastName, documentId, birthDate, "", "", "", studentCode, List[News](), List[News](),List[News](),List[News](), LinkedinUserProfile(UUID.randomUUID().toString, "", List[LinkedinJob](), List[LinkedinEducation](), ""))
                  graduateService.save(graduate)
                }

              }
            }
            result onFailure {
              case _ => {
                val graduate : Graduate = Graduate(UUID.randomUUID().toString, firstName, lastName, documentId, birthDate, "", "", "", studentCode,List[News](), List[News](),List[News](),List[News](), LinkedinUserProfile(UUID.randomUUID().toString, "", List[LinkedinJob](), List[LinkedinEducation](), ""))
                graduateService.save(graduate)
              }
            }
            Await.ready(result, Duration.Inf)
        }

      }
      Ok(views.html.importCSV.render(graduatesCSV,message))
    }.getOrElse {
      Redirect(routes.Application.homeFeed()).flashing(
        "error" -> "Missing file")
    }
  }
  }

  def deleteGraduate(id:String) = Action {
    //Get graduate from DB.
    val graduate : Graduate = Await.result(graduateService.find(id),Duration.Inf)
    val laNacionNewsList: List[News] = graduate.laNacionNews
    val linkedinUserProfile: LinkedinUserProfile = graduate.linkedinUserProfile
    val infobaeNewsList: List[News] = graduate.infobaeNews

    //Delete Linkedin User Profile from DB.
    linkedinUserProfileService.drop(linkedinUserProfile)

    //Delete La Nacion News from DB.
    for(laNacionNews <- laNacionNewsList) {
      laNacionNewsService.drop(laNacionNews)
    }

    //Delete Infoabe News from DB.
    for(infobaeNews <- infobaeNewsList) {
      infobaeNewsService.drop(infobaeNews)
    }
    //Delete Graduate from DB.
    graduateService.drop(graduate)
    Redirect("/egresados/search")
  }

  def validateLinks = Action { implicit request =>
    val laNacionLinks = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("laNacionLinks[]")
    val infobaeLinks = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("infobaeLinks[]")
    val linkedInLinks = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("linkedInLinks[]")
    val id = request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("id").head

    val graduate: Graduate = Await.result(graduateService.find(id),Duration.Inf)

    var laNacionNews = List[News]()
    for(a <- laNacionLinks.get.indices){
      val news: List[News] = List(Await.result(laNacionNewsService.findByUrl(request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("laNacionLinks[]").get(a)),Duration.Inf))
      laNacionNews = laNacionNews.++(news)
    }

    graduate.copy(laNacionNews = laNacionNews)
    val newGraduate = graduate.copy(laNacionNews = laNacionNews)

    Await.result(graduateService.update(newGraduate), Duration.Inf)

    Ok(Json.obj("status" -> "success"))
  }
}
