package controllers

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.UUID

import com.google.inject.Inject
import com.mongodb.MongoWriteException
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Created by franco on 27/07/17.
  */
class ProspectController @Inject()(prospectService: ProspectService,
                                   institutionService: InstitutionService,
                                   linkedInService: LinkedinUserProfileService,
                                   lanacionService: LaNacionNewsService,
                                   infobaeService: InfobaeNewsService,
                                   clarinService: ClarinNewsService,
                                   cronistaService: ElCronistaNewsService,
                                   val messagesApi: MessagesApi) extends Controller with I18nSupport{

  implicit val documentTypes: List[String] = List("dni","cuit","cuil")
  implicit val institutions : Seq[Institution] = Await.result(institutionService.all(),Duration.Inf)

  val form = Form(mapping(
    "_id" -> text(),
    "firstName" -> text(),
    "lastName" -> text(),
    "documentType" -> text(),
    "documentId" -> text(),
    "birthDate" -> text(),
    "entryDate" -> text(),
    "exitDate" -> text(),
    "institution"-> mapping("_id" -> text(),
                            "name" -> text(),
                            "address" -> text(),
                            "active" -> boolean)(Institution.apply)(Institution.unapply),
    "title" -> text(),
    "institutionCode" -> text(),
    "nacionNews" -> list(mapping("_id" -> text(),
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
    "cronistaNews" -> list(mapping("_id" -> text(),
                                   "url" -> text(),
                                   "title" -> text(),
                                   "date" -> text(),
                                   "tuft" -> text(),
                                   "author" -> text())(News.apply)(News.unapply)),
    "linkedInProfile" -> mapping("_id" -> text(),
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
                                 "profileUrl" -> text()) (LinkedinUserProfile.apply) (LinkedinUserProfile.unapply),
    "country" -> default(text,""),
    "primaryEmail" -> default(text,""),
    "secondaryEmail" -> default(text,"")
  )(Prospect.apply)(Prospect.unapply))

  def index = Action{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    Ok(com.prospects.views.html.index.render(prospects))
  }

  def lookup = Action{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    val default: Map[String,String] = Map("firstName"->"","lastName"->"","documentId"->"","title"->"","exitDate"->"","institutionCode"->"")

    Ok(com.prospects.views.html.search.render(prospects, form, default))
  }

  def search = Action{
    implicit request =>{
      val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

      val filter : Map[String,String] = form.bindFromRequest().data

      val format = new SimpleDateFormat("dd/MM/yyyy")
      val date = format.format(new DateTime(filter("exitDate")).toDate)

      val filtered : List[Prospect] = prospects.filter{x: Prospect =>
        x.firstName.toLowerCase.contains(filter("firstName").toLowerCase) &&
        x.lastName.toLowerCase.contains(filter("lastName").toLowerCase) &&
        x.title.toLowerCase.contains(filter("title").toLowerCase) &&
        x.institutionCode.toLowerCase.contains(filter("institutionCode").toLowerCase) &&
        x.documentId.toLowerCase.contains(filter("documentId").toLowerCase) &&
        x.exitDate.toLowerCase.contains(date.toLowerCase)
      }

      Ok(com.prospects.views.html.search.render(filtered, form, filter))
    }
  }

  def create = Action{
    val default: Map[String, String] = Map("_id"->"",
                                           "firstName"->"",
                                           "lastName"->"",
                                           "documentType"->"",
                                           "documentId"->"",
                                           "birthDate"->"",
                                           "entryDate"->"",
                                           "exitDate"->"",
                                           "institution"->"",
                                           "institutionCode"->"",
                                           "title"->"",
                                           "primaryEmail"->"",
                                           "secondaryEmail"->"",
                                           "country"->"")

    Ok(com.prospects.views.html.create.render(default, documentTypes, institutions))
  }

  def store = Action.async{
    implicit request =>{
      val uuid : String = UUID.randomUUID().toString

      val input : Map[String,String] = form.bindFromRequest().data

      if(form.bindFromRequest.hasErrors)
        Future{ BadRequest(com.prospects.views.html.create(input)) }
      else{

        val institution: Institution = Await.result(institutionService.find(input("institution")), Duration.Inf)

        val prospect: Prospect = Prospect(uuid,
                                          input("firstName"),
                                          input("lastName"),
                                          input("documentType"),
                                          input("documentId"),
                                          input("birthDate"),
                                          input("entryDate"),
                                          input("exitDate"),
                                          institution,
                                          input("institutionCode"),
                                          input("title"),
                                          List[News](),
                                          List[News](),
                                          List[News](),
                                          List[News](),
                                          LinkedinUserProfile(UUID.randomUUID().toString,
                                                              "",
                                                              List[LinkedinJob](),
                                                              List[LinkedinEducation](),
                                                              ""),

                                          input("country"),
                                          input("primaryEmail"),
                                          input("secondaryEmail")
        )

        try{
          prospectService.save(prospect).map((_) => {
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

  def show(id : String) = Action{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.show.render(Option(prospect)))
  }

  def edit(id : String) = Action{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.edit.render(prospect.toMap, documentTypes, institutions))
  }

  def update(id : String) = Action.async{
    implicit request => {
      val input : Map[String,String] = form.bindFromRequest().data

      val original: Prospect = Await.result(prospectService.find(id),Duration.Inf)

      if(form.bindFromRequest.hasErrors)
        Future{ BadRequest(com.prospects.views.html.create(input)) }
      else{
        val institution: Institution = Await.result(institutionService.find(input("institution")), Duration.Inf)

        val updated: Prospect = Prospect(id,
                                         input("firstName"),
                                         input("lastName"),
                                         input("documentType"),
                                         input("documentId"),
                                         input("birthDate"),
                                         input("entryDate"),
                                         input("exitDate"),
                                         institution,
                                         input("institutionCode"),
                                         input("title"),
                                         original.nacionNews,
                                         original.infobaeNews,
                                         original.clarinNews,
                                         original.cronistaNews,
                                         original.linkedInProfile,
                                         input("country"),
                                         input("primaryEmail"),
                                         input("secondaryEmail"))
        Await.result(prospectService.update(updated),Duration.Inf)
        Future{Redirect(routes.ProspectController.show(original._id))}
      }
    }
  }

  def delete(id : String) = Action{
    implicit request => {
      val prospect: Prospect = Await.result(prospectService.find(id),Duration.Inf)

      //Delete LinkedIn information
      val profile: LinkedinUserProfile = prospect.linkedInProfile
      linkedInService.drop(profile)

      //Delete News Information
      prospect.nacionNews.map(lanacionService.drop)
      prospect.infobaeNews.map(infobaeService.drop)
      prospect.clarinNews.map(clarinService.drop)
      prospect.cronistaNews.map(cronistaService.drop)

      //Delete Prospect
      prospectService.drop(prospect)
      Redirect(routes.ProspectController.index())
    }
  }

  def createBatch = Action{
    Ok("")
  }

  def storeBatch = Action{
    implicit request => {
      Ok("")
    }
  }
}
