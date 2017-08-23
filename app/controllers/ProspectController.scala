package controllers

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.UUID

import actions.SecureAction
import com.github.tototoshi.csv.CSVReader
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import enums.EnumPlayUtils.enum
import enums.{InstitutionSector, InstitutionType}
import models._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import play.api.mvc.{Controller, Request}
import play.cache.DefaultCacheApi
import services._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

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
                                   cache : DefaultCacheApi,
                                   val messagesApi: MessagesApi, secureAction: SecureAction) extends Controller with I18nSupport{

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
    "institution" -> mapping(
      "_id" -> text(),
      "name" -> text.verifying(_.nonEmpty),
      "address" -> text.verifying(_.nonEmpty),
      "active" -> boolean,
      "institutionType" -> enum(InstitutionType),
      "sector" -> enum(InstitutionSector))(Institution.apply)(Institution.unapply),
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

  def index(message: String = "") =secureAction{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    Ok(com.prospects.views.html.index.render(prospects,message))
  }

  def lookup =secureAction{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    val default: Map[String,String] = Map("firstName"->"","lastName"->"","documentId"->"","title"->"","exitDate"->"","institutionCode"->"")

    Ok(com.prospects.views.html.search.render(prospects, form, default))
  }

  def search =secureAction{
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

  def create(message: String = "",default: Map[String, String] = null) =secureAction{
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

  def store =secureAction.async{
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

        if (!prospects.exists(g => (g._id != prospect._id) && prospectService.matchGraduates(prospect, g))) {


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

  def show(id : String) =secureAction{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.show.render(Option(prospect)))
  }

  def edit(id : String,message: String = "") =secureAction{
    val prospect: Prospect = Await.result(prospectService.find(id), Duration.Inf)
    Ok(com.prospects.views.html.edit.render(prospect.toMap,message, documentTypes, institutions))
  }

  def update(id : String) =secureAction.async {
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

        if (!prospects.exists(g => (g._id != updated._id) && prospectService.matchGraduates(updated, g))) {


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




  def delete(id : String) =secureAction{
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

  def createBatch =secureAction{
    Ok(com.prospects.views.html.batch_upload.render())
  }

  def storeBatch =secureAction{
    implicit request => {
      //Primera llamada para que los cargue mientras se procesa lo otro
      val eventualProspects = prospectService.all()


      val selected : Seq[String] = request.body.asFormUrlEncoded.get("prospect[]")
      val prospects : Seq[Prospect] = cache.get[Seq[Prospect]]("prospects")
      cache.remove("prospects")


      val existentProspects: List[Prospect] = Await.result(eventualProspects, Duration.Inf).toList

      val added: Seq[Prospect] = prospects.filter(x => selected.contains(x._id) && !existentProspects.exists(p => p._id == x._id))
      val updated: Seq[Prospect] = prospects.filter(x => selected.contains(x._id) && existentProspects.exists(p => p._id == x._id))

      val addedInstitutes : Seq[Institution] =  added.map(_.institution).filter(i=> !institutions.contains(i))

      addedInstitutes.map(institutionService.save)

      added.map(prospectService.save)
      updated.map(prospectService.update)

      Redirect(routes.ProspectController.index(s"Successfully added ${added.size} and updated ${updated.size} prospects  "))
    }
  }


  def uploadFile =secureAction(parse.multipartFormData){
    implicit request =>{
      val eventualProspects = prospectService.all()
      var uploadInstitutes : Seq[Institution] = Seq[Institution]()

      val data = request.body.files.flatMap{x =>

        val filename = x.filename
        val contentType = x.contentType
        val csvFile = x.ref.file

        val reader = CSVReader.open(csvFile)
        val existentProspects: List[Prospect] = Await.result(eventualProspects, Duration.Inf).toList
        reader.allWithHeaders().map{z =>

          //Get or create the institution
          val institution: Option[Institution] = z.get("Institucion") match{
            case Some(string) =>
              institutions.find(_.name.equals(string)) match {
                case Some(institute) =>
                  Option(institute)
                case None =>
                  val aux: Institution = Institution(UUID.randomUUID().toString, string, "", active = true, null, null)
                  uploadInstitutes = uploadInstitutes :+ aux
                  Option(aux)
              }
            case None =>
              val institute : Institution = institutions.head
              Option(institute)
          }


          var csvProspect = Prospect(UUID.randomUUID().toString,
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

          val headOption = existentProspects.find(p => prospectService.matchGraduates(csvProspect, p))
          if (headOption.isDefined) {
            csvProspect = csvProspect.copy(_id = headOption.head._id)
          }
          csvProspect
        }
      }

      cache.set("prospects", data, 600)
      cache.set("institutions", uploadInstitutes, 600)

      Ok(Json.toJson(Map("status"->Json.toJson("OK"))))
    }
  }

  def loadPotentialProspects =secureAction{

    val prospects : Seq[Prospect] = cache.get[Seq[Prospect]]("prospects")

    prospects match{
      case x: Seq[Prospect] =>
        val data : JsValue = Json.toJson(prospects.map(_.toJson))
        Ok(data)
      case null => Ok(Json.toJson(Seq[JsValue]()))
    }
  }

  def showValidation(id: String) =secureAction{
    val prospect: Prospect = Await.result(prospectService.find(id),Duration.Inf)
    Ok(com.prospects.views.html.link_validation.render(Option(prospect)))
  }

  def postValidation(id: String) =secureAction(parse.json){
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
