package controllers

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.UUID

import com.github.tototoshi.csv.CSVReader
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Request}
import services._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

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

  def index(message: String = "") = Action{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    Ok(com.prospects.views.html.index.render(prospects,message))
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

  def create(message: String = "",default: Map[String, String] = null) = Action{
    val prospect: Map[String, String]= if(default != null) default else Map("_id"->"",
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



    Ok(com.prospects.views.html.create.render(prospect,message, documentTypes, institutions))
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

        val prospects: List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

        if (!prospects.exists(g => (g.primaryEmail != "" && g.secondaryEmail != "" && g._id != prospect._id) && (g.primaryEmail == prospect.primaryEmail || g.secondaryEmail == prospect.primaryEmail
          || g.primaryEmail == prospect.secondaryEmail || g.secondaryEmail == prospect.secondaryEmail))) {


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
        else
        {
          val message = "Primary or secondary emails are already taken by another prospect"


           create(message,prospect.toMap).apply(request)


        }


      }
    }
  }

  def show(id : String) = Action{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.show.render(Option(prospect)))
  }

  def edit(id : String,message: String = "") = Action{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.edit.render(prospect.toMap,message, documentTypes, institutions))
  }

  def update(id : String) = Action.async {
    implicit request => {
      val input: Map[String, String] = form.bindFromRequest().data

      val original: Prospect = Await.result(prospectService.find(id), Duration.Inf)

      if (form.bindFromRequest.hasErrors)
        Future {
          BadRequest(com.prospects.views.html.create(input))
        }
      else {
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
        val prospects: List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

        if (!prospects.exists(g => (g.primaryEmail != "" && g.secondaryEmail != "" && g._id != updated._id) && (g.primaryEmail == updated.primaryEmail || g.secondaryEmail == updated.primaryEmail
          || g.primaryEmail == updated.secondaryEmail || g.secondaryEmail == updated.secondaryEmail))) {


          Await.result(prospectService.update(updated), Duration.Inf)
          Future {
            Redirect(routes.ProspectController.show(original._id))
          }
        }
          else
          {
            val message = "Primary or secondary emails are already taken by another prospect"
            Future {
              //Redirect("/update/" + graduate._id + "?message=" + URLEncoder.encode(message, "UTF-8") )
              Redirect(routes.ProspectController.edit(original._id,message))

            }
          }
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
      Redirect(routes.ProspectController.index(prospect.getFullName + " has been deleted successfully"))
    }
  }

  def createBatch = Action{
    Ok(com.prospects.views.html.batch_upload.render())
  }

  def storeBatch = Action{
    implicit request => {
      Ok("")
    }
  }

  def uploadFile = Action(parse.multipartFormData){
    implicit request =>{

      var uploadInstitutes : Seq[Institution] = Seq[Institution]()

      val data = request.body.files.flatMap{x =>

        val filename = x.filename
        val contentType = x.contentType
        val csvFile = x.ref.file

        val reader = CSVReader.open(csvFile)

        reader.allWithHeaders().map{z =>

          //Get or create the institution
          val institution: Option[Institution] = z.get("Institucion") match{
            case Some(string) =>
              institutions.find(_.name.equals(string)) match {
                case Some(institute) =>
                  uploadInstitutes = uploadInstitutes :+ institute
                  Option(institute)
                case None =>
                  val aux : Institution = Institution(UUID.randomUUID().toString,string,"", active = true)
                  uploadInstitutes = uploadInstitutes :+ aux
                  Option(aux)
              }
            case None =>
              val institute : Institution = institutions.head
              uploadInstitutes = uploadInstitutes :+ institute
              Option(institute)
          }

          Prospect(UUID.randomUUID().toString,
                   z.getOrElse("Nombre",""),
                   z.getOrElse("Apellido",""),
                   z.getOrElse("Tipo",""),
                   z.getOrElse("Documento",""),
                   z.getOrElse("Nacimiento",""),
                   z.getOrElse("Ingreso",""),
                   z.getOrElse("Egreso",""),
                   institution.get,
                   z.getOrElse("Legajo",""),
                   z.getOrElse("Titulo",""),
                   List[News](),
                   List[News](),
                   List[News](),
                   List[News](),
                   LinkedinUserProfile(UUID.randomUUID().toString,"",List[LinkedinJob](),List[LinkedinEducation](),""),
                   z.getOrElse("Pais",""),
                   z.getOrElse("Email_1",""),
                   z.getOrElse("Email_2","")
          )
        }
      }

      val map : Map[String, JsValue] = Map("status"->Json.toJson("OK"),
                                           "items" ->Json.toJson(data.map(x=> Json.toJson(x.toMap))),
                                           "institutions" -> Json.toJson(uploadInstitutes.map(x=> Json.toJson(x.toMap))))

      Ok(Json.toJson(Map("status"->Json.toJson("OK"))))

    }
  }

  def loadPotentialProspects = Action{

//    val prospects : Seq[Map[String,String]] = Seq[Map[String,String]]()
    val prospects : Seq[Prospect] = Await.result(prospectService.all(), Duration.Inf)

    val data : Map[String,Seq[JsValue]] = Map("data"->prospects.map(_.toJson))
    Ok(Json.toJson(data))
  }

  def showValidation(id: String) = Action{
    val prospect: Prospect = Await.result(prospectService.find(id),Duration.Inf)
    Ok(com.prospects.views.html.link_validation.render(Option(prospect)))
  }

  def postValidation(id: String) = Action(parse.json){
    implicit request: Request[JsValue] => {
      val prospect: Prospect = Await.result(prospectService.find(id),Duration.Inf)

      val links: Map[String, JsValue] = request.body match{
        case JsObject(fields) => fields.toMap
        case _ => Map[String, JsValue]()
      }

      links("type").toString() match{
        case "lanacion" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString()).toList
          val filtered: (List[News],List[News]) = prospect.nacionNews.partition(x=> items.contains(x._id))
          val updated : Prospect = prospect.copy(nacionNews = filtered._1)
          prospectService.update(updated)
          filtered._2.map(x => lanacionService.drop(x))
          Ok(Json.toJson(Map("status"->"OK", "erased"->"lanacion")))

        case "infobae" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString()).toList
          val filtered: (List[News],List[News]) = prospect.infobaeNews.partition(x=> items.contains(x._id))
          val updated : Prospect = prospect.copy(infobaeNews = filtered._1)
          prospectService.update(updated)
          filtered._2.map(x => infobaeService.drop(x))
          Ok(Json.toJson(Map("status"->"OK", "erased"->"infobae")))

        case "clarin" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString()).toList
          val filtered: (List[News],List[News]) = prospect.clarinNews.partition(x=> items.contains(x._id))
          val updated : Prospect = prospect.copy(clarinNews = filtered._1)
          prospectService.update(updated)
          filtered._2.map(x => clarinService.drop(x))
          Ok(Json.toJson(Map("status"->"OK", "erased"->"clarin")))

        case "elcronista" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString()).toList
          val filtered: (List[News],List[News]) = prospect.cronistaNews.partition(x=> items.contains(x._id))
          val updated : Prospect = prospect.copy(cronistaNews = filtered._1)
          prospectService.update(updated)
          filtered._2.map(x => cronistaService.drop(x))
          Ok(Json.toJson(Map("status"->"OK", "erased"->"elcronista")))

        case _ =>
          Ok(Json.toJson(Map("status"->"nothing")))
      }
    }
  }
}
