package controllers

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.{Calendar, Date, UUID}

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
import scrapers._
import services._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
//TODO review all classes & views to check they are working accordingly and include new features for academicData and institutions create 
/**
  * Created by franco on 27/07/17.
  */
class ProspectController @Inject()(prospectService: ProspectService,
                                   institutionService: InstitutionService,
                                   institutionalDataService: InstitutionalDataService,
                                   linkedInService: LinkedinUserProfileService,
                                   linkedinUserProfileScraper: LinkedinUserProfileScraper,
                                   lanacionService: LaNacionNewsService,
                                   laNacionScraper: LaNacionScraper,
                                   infobaeService: InfobaeNewsService,
                                   infobaeScraper: InfobaeScraper,
                                   clarinService: ClarinNewsService,
                                   clarinScraper: ClarinScraper,
                                   cronistaService: ElCronistaNewsService,
                                   cronistaScraper: ElCronistaScraper,
                                   cache : DefaultCacheApi,
                                   val messagesApi: MessagesApi, secureAction: SecureAction) extends Controller with I18nSupport{

  final val dateTimeFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  final val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

  implicit val documentTypes: List[String] = List("dni","cuit","cuil")
  implicit val institutions : Seq[Institution] = Await.result(institutionService.all(),Duration.Inf)

  val form = Form(mapping(
    "_id" -> text(),
    "firstName" -> text.verifying(_.nonEmpty),
    "lastName" -> text.verifying(_.nonEmpty),
    "documentType" -> text(),
    "documentId" -> text(),
    "birthDate" -> text(),
    "workingData" -> mapping(
      "_id" -> text(),
      "entryDate" -> text(),
      "exitDate" -> text(),
      "title" -> text(),
      "institutionCode" -> text(),
      "institution" -> mapping("_id" -> text(),
                               "name" -> text(),
                               "address" -> text(),
                               "active" -> boolean,
                               "institutionType" -> default(enum(InstitutionType), InstitutionType.Unspecified),
                               "sector" -> default(enum(InstitutionSector), InstitutionSector.Unspecified)
                              )(Institution.apply)(Institution.unapply)
    )(InstitutionalData.apply)(InstitutionalData.unapply),
    "academicData" -> mapping(
      "_id" -> text(),
      "entryDate" -> text(),
      "exitDate" -> text(),
      "title" -> text(),
      "institutionCode" -> text(),
      "institution" -> mapping("_id" -> text(),
                               "name" -> text(),
                               "address" -> text(),
                               "active" -> boolean,
                               "institutionType" -> default(enum(InstitutionType), InstitutionType.Unspecified),
                               "sector" -> default(enum(InstitutionSector), InstitutionSector.Unspecified)
                              )(Institution.apply)(Institution.unapply)
    )(InstitutionalData.apply)(InstitutionalData.unapply),
    "nacionNews" -> list(mapping("_id" -> text(),
                                 "url" -> text(),
                                 "title" -> text(),
                                 "date" -> text(),
                                 "tuft" -> text(),
                                 "author" -> text(),
                                 "validated" -> boolean,
                                 "rejected" -> boolean)(News.apply)(News.unapply)),
    "infobaeNews" -> list(mapping("_id" -> text(),
                                  "url" -> text(),
                                  "title" -> text(),
                                  "date" -> text(),
                                  "tuft" -> text(),
                                  "author" -> text(),
                                  "validated" -> boolean,
                                  "rejected" -> boolean)(News.apply)(News.unapply)),
    "clarinNews" -> list(mapping("_id" -> text(),
                                 "url" -> text(),
                                 "title" -> text(),
                                 "date" -> text(),
                                 "tuft" -> text(),
                                 "author" -> text(),
                                 "validated" -> boolean,
                                 "rejected" -> boolean)(News.apply)(News.unapply)),
    "cronistaNews" -> list(mapping("_id" -> text(),
                                   "url" -> text(),
                                   "title" -> text(),
                                   "date" -> text(),
                                   "tuft" -> text(),
                                   "author" -> text(),
                                   "validated" -> boolean,
                                   "rejected" -> boolean)(News.apply)(News.unapply)),
    "linkedInProfiles" -> list(mapping("_id" -> text(),
                                       "actualPosition" -> text(),
                                       "jobList" -> list(mapping("_id" -> text(),
                                                                 "position" -> text(),
                                                                 "workPlace" -> text(),
                                                                 "workUrl" -> text(),
                                                                 "activityPeriod" -> text(),
                                                                 "jobDescription" -> text()
                                       )(LinkedinJob.apply)(LinkedinJob.unapply)),
                                       "educationList" -> list(mapping("_id" -> text(),
                                                                       "institute" -> text(),
                                                                       "instituteUrl" -> text(),
                                                                       "title" -> text(),
                                                                       "educationPeriod" -> text(),
                                                                       "educationDescription" -> text()
                                       )(LinkedinEducation.apply)(LinkedinEducation.unapply)),
                                       "profileUrl" -> text(),
                                       "validated" -> boolean,
                                       "rejected" -> boolean
    )(LinkedinUserProfile.apply)(LinkedinUserProfile.unapply)),
    "country" -> default(text,""),
    "primaryEmail" -> default(text,""),
    "secondaryEmail" -> default(text,""),
    "createdAt" -> default(text, ""),
    "updatedAt" -> default(text, ""),
    "errorDate" -> default(text, "")
  )(Prospect.apply)(Prospect.unapply))

  def index(message: String = "") =secureAction{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    Ok(com.prospects.views.html.index.render(prospects,message))
  }

  def lookup =secureAction{
    val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

    val default: Map[String,String] = Map("firstName" -> "",
                                          "lastName" -> "",
                                          "documentId" -> "",
                                          "title" -> "",
                                          "exitDate" -> "",
                                          "institutionCode" -> "")

    Ok(com.prospects.views.html.search.render(prospects, form, default))
  }

  // TODO: refactor search
  def search =secureAction{
    implicit request =>{
      val prospects : List[Prospect] = Await.result(prospectService.all(), Duration.Inf).toList

      val filter : Map[String,String] = form.bindFromRequest().data
      val titleFilter: String = filter("title").toLowerCase
      val codeFilter: String = filter("title").toLowerCase

      val date = dateFormat.format(new DateTime(filter("exitDate")).toDate)
      val dateFilter: String = date.toLowerCase

      val filtered : List[Prospect] = prospects.filter{x: Prospect =>
        x.firstName.toLowerCase.contains(filter("firstName").toLowerCase) &&
        x.lastName.toLowerCase.contains(filter("lastName").toLowerCase) &&
        (x.workingData.title.toLowerCase.contains(titleFilter) || x.academicData.title.toLowerCase.contains(titleFilter)) &&
        (x.workingData.institutionCode.toLowerCase.contains(codeFilter) || x.academicData.institutionCode.toLowerCase.contains(codeFilter)) &&
        (x.workingData.exitDate.toLowerCase.contains(dateFilter) || x.academicData.exitDate.toLowerCase.contains(dateFilter)) &&
        x.documentId.toLowerCase.contains(filter("documentId").toLowerCase)
      }

      Ok(com.prospects.views.html.search.render(filtered, form, filter))
    }
  }

  def create(message: String = "", default: Map[String, String] = null) =secureAction{
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

  def store = secureAction.async{
    implicit request =>{
      val uuid : String = UUID.randomUUID().toString

      val bindedForm = form.bindFromRequest()
      val input: Map[String, String] = bindedForm.data

      if (bindedForm.hasErrors)
        Future {
          BadRequest(com.prospects.views.html.create(input, message = bindedForm.errors.mkString))
        }
      else{

        val now : Date = Calendar.getInstance().getTime

        //TODO: apply changes in views
        //Working Data
        val workingInstitution: Institution = Await.result(institutionService.find(input("workingInstitution")), Duration.Inf)

        val workingData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
                                                               input("workingEntry"),
                                                               input("workingExit"),
                                                               input("workingTitle"),
                                                               input("workingCode"),
                                                               workingInstitution)

        //Academic Data
        val academicInstitution: Institution = Await.result(institutionService.find(input("academicInstitution")), Duration.Inf)

        val academicData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
                                                                input("academicEntry"),
                                                                input("academicExit"),
                                                                input("academicTitle"),
                                                                input("academicCode"),
                                                                academicInstitution)

        val prospect: Prospect = Prospect(uuid,
                                          input("firstName"),
                                          input("lastName"),
                                          input("documentType"),
                                          input("documentId"),
                                          input("birthDate"),
                                          workingData,
                                          academicData,
                                          List[News](), List[News](), List[News](), List[News](),
                                          List[LinkedinUserProfile](),
                                          input("country"),
                                          input("primaryEmail"),
                                          input("secondaryEmail"),
                                          dateTimeFormat.format(now), "", "")

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
      val bindedForm = form.bindFromRequest()
      val input: Map[String, String] = bindedForm.data

      val original: Prospect = Await.result(prospectService.find(id), Duration.Inf)

      if (bindedForm.hasErrors)
        Future {
          BadRequest(com.prospects.views.html.edit(input, message = bindedForm.errors.mkString))
        }
      else {
        //Working Data
        val workingInstitution: Institution = Await.result(institutionService.find(input("workingInstitution")), Duration.Inf)

        val workingData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
          input("workingEntry"),
          input("workingExit"),
          input("workingTitle"),
          input("workingCode"),
          workingInstitution)

        //Academic Data
        val academicInstitution: Institution = Await.result(institutionService.find(input("academicInstitution")), Duration.Inf)

        val academicData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
          input("academicEntry"),
          input("academicExit"),
          input("academicTitle"),
          input("academicCode"),
          academicInstitution)

        val now : Date = Calendar.getInstance().getTime

        val updated: Prospect = Prospect(id,
                                         input("firstName"),
                                         input("lastName"),
                                         input("documentType"),
                                         input("documentId"),
                                         input("birthDate"),
                                         workingData,
                                         academicData,
                                         original.nacionNews,
                                         original.infobaeNews,
                                         original.clarinNews,
                                         original.cronistaNews,
                                         original.linkedInProfiles,
                                         input("country"),
                                         input("primaryEmail"),
                                         input("secondaryEmail"),
                                         original.createdAt, dateTimeFormat.format(now), original.errorDate)

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
      val profiles = prospect.linkedInProfiles
      profiles.foreach(p => linkedInService.drop(p))

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


      val encodedForm = request.body.asFormUrlEncoded.getOrElse(Map())
      val selectedIds: Seq[String] = encodedForm.getOrElse("prospect[]._id", List[String]())
      val prospects : Seq[Prospect] = cache.get[Seq[Prospect]]("prospects")
      cache.remove("prospects")
      var selectedProspects: List[Prospect] = List[Prospect]()
      for (prospect <- prospects) {
        val formIndex = selectedIds.indexOf(prospect._id)
        if (formIndex >= 0) {
          val institutionName = encodedForm("prospect[].institution.name")(formIndex)

          val workingInstitution: Institution = institutions.find(i => i.name == institutionName).getOrElse(Institution.DEFAULT_EMPTY)
          val workingData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
                                                                 encodedForm("prospect[].entryDate")(formIndex),
                                                                 encodedForm("prospect[].exitDate")(formIndex),
                                                                 encodedForm("prospect[].title")(formIndex),
                                                                 "",
                                                                 workingInstitution)

          val updatedProspect = prospect.copy(firstName = encodedForm("prospect[].firstName")(formIndex),
            lastName = encodedForm("prospect[].lastName")(formIndex),
            documentType = encodedForm("prospect[].documentType")(formIndex),
            documentId = encodedForm("prospect[].documentId")(formIndex),
            birthDate = encodedForm("prospect[].birthDate")(formIndex),
            workingData = workingData,
            country = encodedForm("prospect[].country")(formIndex),
            primaryEmail = encodedForm("prospect[].primaryEmail")(formIndex),
            secondaryEmail = encodedForm("prospect[].secondaryEmail")(formIndex))
          selectedProspects = updatedProspect :: selectedProspects
        }
      }

      val existentProspects: List[Prospect] = Await.result(eventualProspects, Duration.Inf).toList

      val added: Seq[Prospect] = selectedProspects.filter(x => !existentProspects.exists(p => p._id == x._id))
      val updated: Seq[Prospect] = selectedProspects.filter(x => existentProspects.exists(p => p._id == x._id))

      val addedInstitutes: Seq[Institution] = (added ++ updated).map(_.workingData.institution).filter(i => !institutions.contains(i))

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

        val reader = CSVReader.open(csvFile, "Cp1252") //ANSI
        val existentProspects: List[Prospect] = Await.result(eventualProspects, Duration.Inf).toList
        reader.allWithHeaders().map{z =>

          //Get or create the institution
          val institution: Option[Institution] = z.get("Institucion") match{
            case Some(string) =>
              institutions.find(_.name.equals(string)) match {
                case Some(institute) =>
                  Option(institute)
                case None =>
                  val aux: Institution = Institution(name = string)
                  uploadInstitutes = uploadInstitutes :+ aux
                  Option(aux)
              }
            case None =>
              val institute : Institution = institutions.head
              Option(institute)
          }

          val workingData: InstitutionalData = InstitutionalData(UUID.randomUUID().toString,
                                                                 z.getOrElse("Ingreso", ""),
                                                                 z.getOrElse("Egreso", ""),
                                                                 z.getOrElse("Ingreso", ""),
                                                                 z.getOrElse("Ingreso", ""),
                                                                 institution.get)

          val academicData: InstitutionalData = InstitutionalData.DEFAULT_EMPTY

          val now : Date = Calendar.getInstance().getTime

          var csvProspect = Prospect(UUID.randomUUID().toString,
                                     z.getOrElse("Nombre", ""),
                                     z.getOrElse("Apellido", ""),
                                     z.getOrElse("Tipo", ""),
                                     z.getOrElse("Documento", ""),
                                     z.getOrElse("Nacimiento", ""),
                                     workingData,
                                     academicData,
                                     List[News](), List[News](), List[News](), List[News](),
                                     List[LinkedinUserProfile](),
                                     z.getOrElse("Pais", ""),
                                     z.getOrElse("Email_1", ""), z.getOrElse("Email_2", ""),
                                     dateTimeFormat.format(now), "", "")

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

  def postValidation(id: String) = secureAction(parse.json){
    implicit request: Request[JsValue] => {
      val prospect: Prospect = Await.result(prospectService.find(id),Duration.Inf)

      val links: Map[String, JsValue] = request.body match{
        case JsObject(fields) => fields.toMap
        case _ => Map[String, JsValue]()
      }

      val newsType : String = links("type").toString().replace("\"","")

      newsType match{
        case "lanacion" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString().replace("\"","")).toList
          val filtered: (List[News],List[News]) = prospect.nacionNews.partition(x=> items.contains(x._id))

          val validated: List[News] = filtered._1.map(_.copy(validated = true, rejected = false))
          val rejected: List[News] = filtered._2.map(_.copy(rejected = true, validated = false))
          val all: List[News] = rejected ++ validated
          all.map(news => lanacionService.update(news))
          val updated: Prospect = prospect.copy(nacionNews = all)
          prospectService.update(updated)

          Ok(Json.toJson(Map("status"->"OK", "erased"->"lanacion")))

        case "infobae" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString().replace("\"","")).toList
          val filtered: (List[News],List[News]) = prospect.infobaeNews.partition(x=> items.contains(x._id))

          val validated: List[News] = filtered._1.map(_.copy(validated = true, rejected = false))
          val rejected: List[News] = filtered._2.map(_.copy(rejected = true, validated = false))
          val all: List[News] = rejected ++ validated
          all.map(news => infobaeService.update(news))
          val updated: Prospect = prospect.copy(infobaeNews = all)
          prospectService.update(updated)

          Ok(Json.toJson(Map("status"->"OK", "erased"->"infobae")))

        case "clarin" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString().replace("\"","")).toList
          val filtered: (List[News],List[News]) = prospect.clarinNews.partition(x=> items.contains(x._id))

          val validated: List[News] = filtered._1.map(_.copy(validated = true, rejected = false))
          val rejected: List[News] = filtered._2.map(_.copy(rejected = true, validated = false))
          val all: List[News] = rejected ++ validated
          all.map(news => clarinService.update(news))
          val updated: Prospect = prospect.copy(clarinNews = all)
          prospectService.update(updated)

          Ok(Json.toJson(Map("status"->"OK", "erased"->"clarin")))

        case "elcronista" =>
          val items : List[String] = links("links").asInstanceOf[JsArray].value.map(x=>x.toString().replace("\"","")).toList
          val filtered: (List[News],List[News]) = prospect.cronistaNews.partition(x=> items.contains(x._id))

          val validated: List[News] = filtered._1.map(_.copy(validated = true, rejected = false))
          val rejected: List[News] = filtered._2.map(_.copy(rejected = true, validated = false))
          val all: List[News] = rejected ++ validated
          all.map(news => cronistaService.update(news))
          val updated: Prospect = prospect.copy(cronistaNews = all)
          prospectService.update(updated)

          Ok(Json.toJson(Map("status"->"OK", "erased"->"elcronista")))

        case "linkedin" =>
          val items: List[String] = links("links").asInstanceOf[JsArray].value.map(x => x.toString().replace("\"", "")).toList
          val filtered: (List[LinkedinUserProfile], List[LinkedinUserProfile]) = prospect.linkedInProfiles.partition(x => items.contains(x._id))

          val validated: List[LinkedinUserProfile] = filtered._1.map(_.copy(validated = true, rejected = false))
          val rejected: List[LinkedinUserProfile] = filtered._2.map(_.copy(rejected = true, validated = false))
          val all: List[LinkedinUserProfile] = rejected ++ validated
          all.map(profile => linkedInService.update(profile))
          val updated: Prospect = prospect.copy(linkedInProfiles = all)
          prospectService.update(updated)

          Ok(Json.toJson(Map("status" -> "OK", "erased" -> "linkedin")))

        case _ =>
          Ok(Json.toJson(Map("status"->"nothing")))
      }
    }
  }

  def addLink(id : String) = secureAction.async(parse.json){
    implicit request : Request[JsValue]=>{

      val data: Map[String, JsValue] = request.body match{
        case JsObject(fields) => fields.toMap
        case _ => Map[String, JsValue]()
      }
      val link : String = data("link").toString().replace("\"","")
      val source : String = data("source").toString().replace("\"","")

      val prospect : Future[Prospect] = prospectService.find(id)

      prospect.map{p =>

        source match {
          case "lanacion" =>
            val news : Option[News] = laNacionScraper.getArticleData(link, Option(p.getFullName),0)
            news match{
              case Some(x) =>
                val newsList : List[News] = p.nacionNews
                lanacionService.save(x)
                prospectService.update(p.copy(nacionNews = newsList :+ x))
                Ok(Json.toJson(Map("status" -> "OK")))
              case None =>
                Ok(Json.toJson(Map("status" -> "nothing")))
            }
          case "infobae" =>
            val news : Option[News] = infobaeScraper.getArticleData(link, Option(p.getFullName),0)
            news match{
              case Some(x) =>
                val newsList : List[News] = p.infobaeNews
                infobaeService.save(x)
                prospectService.update(p.copy(infobaeNews = newsList :+ x))
                Ok(Json.toJson(Map("status" -> "OK")))
              case None =>
                Ok(Json.toJson(Map("status" -> "nothing")))
            }
          case "clarin" =>
            val news : Option[News] = laNacionScraper.getArticleData(link, Option(p.getFullName),0)
            news match{
              case Some(x) =>
                val newsList : List[News] = p.clarinNews
                clarinService.save(x)
                prospectService.update(p.copy(clarinNews = newsList :+ x))
                Ok(Json.toJson(Map("status" -> "OK")))
              case None =>
                Ok(Json.toJson(Map("status" -> "nothing")))
            }
          case "elcronista" =>
            val news : Option[News] = cronistaScraper.getArticleData(link, Option(p.getFullName),0)
            news match{
              case Some(x) =>
                val newsList : List[News] = p.cronistaNews
                cronistaService.save(x)
                prospectService.update(p.copy(cronistaNews = newsList :+ x))
                Ok(Json.toJson(Map("status" -> "OK")))
              case None =>
                Ok(Json.toJson(Map("status" -> "nothing")))
            }
          case "linkedin" =>
            val profile : Option[LinkedinUserProfile] = linkedinUserProfileScraper.getLinkedinProfile(link,0)
            profile match {
              case Some(x) =>
                val profiles : List[LinkedinUserProfile] = p.linkedInProfiles
                linkedInService.save(x)
                prospectService.update(p.copy(linkedInProfiles = profiles :+ x))
                Ok(Json.toJson(Map("status" -> "OK")))
              case None => Ok(Json.toJson(Map("status"->"nothing")))
            }
          case _ => Ok(Json.toJson(Map("status"->"nothing")))
        }
      }

    }
  }
}
