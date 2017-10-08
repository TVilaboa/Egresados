package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import enums.{InstitutionSector, InstitutionType}
import models._
import org.bson.BsonValue
import org.mongodb.scala._
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by franco on 27/07/17.
  */
@ImplementedBy(classOf[MongoProspectDao])
trait ProspectDao {
  def all(): Future[Seq[Prospect]]

  def find(_id: String): Future[Prospect]

  def findByDocumentId(documentId: String): Future[Prospect]

  def findByFirstName(firstName: String): Future[Prospect]

  def findByLastName(lastName: String): Future[Prospect]

  def findByInstitution(_institutionId: String): Future[Seq[Prospect]]

  def save(prospect: Prospect): Future[Completed]

  def update(prospect: Prospect): Future[UpdateResult]

  def drop(prospect: Prospect) : Future[Prospect]
}

@Singleton
class MongoProspectDao @Inject()(mongo: Mongo) extends ProspectDao {

  private val data : MongoCollection[Document] = mongo.db.getCollection("prospects")

  override def all(): Future[Seq[Prospect]] = data.find().toFuture().map(doc => doc.map(transformDocument))

  override def find(_id: String): Future[Prospect] = {
    data.find(equal("_id", _id)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByDocumentId(documentId: String): Future[Prospect] = {
    data.find(equal("documentId", documentId)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByFirstName(firstName: String): Future[Prospect] = {
    data.find(equal("firstName", firstName)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByLastName(lastName: String): Future[Prospect] = {
    data.find(equal("lastName", lastName)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByInstitution(_institutionId: String): Future[Seq[Prospect]] = {
    for{
      workingData <- data.find(equal("workingData.institution._id", _institutionId)).toFuture().map(doc => doc.map(transformDocument))
      academicData <- data.find(equal("academicData.institution._id", _institutionId)).toFuture().map(doc => doc.map(transformDocument))
    } yield (workingData ++ academicData).distinct
  }

  override def save(prospect: Prospect): Future[Completed] = {
    val json: String = Json.toJson(prospect).toString
    val doc: Document = Document(json)
    data.insertOne(doc).head()
  }

  override def update(prospect: Prospect): Future[UpdateResult] = data.replaceOne(equal("_id", prospect._id), Document(Json.toJson(prospect).toString)).head()

  override def drop(prospect: Prospect): Future[Prospect] = {
    data.findOneAndDelete(equal("_id", prospect._id)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  private def transformDocument(document: Document) : Prospect = {
    //Get all News for prospect
    val nacionNews: List[News] = Try(transformNews(document.get("nacionNews").get.asArray())) match{
      case Success(item) => item
      case Failure(exception) => List[News]()
    }

    val infobaeNews: List[News] = Try(transformNews(document.get("infobaeNews").get.asArray())) match{
      case Success(item) => item
      case Failure(exception) => List[News]()
    }

    val cronistaNews: List[News] = Try(transformNews(document.get("cronistaNews").get.asArray())) match{
      case Success(item) => item
      case Failure(exception) => List[News]()
    }

    val clarinNews: List[News] = Try(transformNews(document.get("clarinNews").get.asArray())) match{
      case Success(item) => item
      case Failure(exception) => List[News]()
    }

    //Working Data
    val workingDoc : BsonDocument = document.get("workingData") match {
      case Some(doc) => doc.asDocument()
      case None => null
    }
    val workingData: InstitutionalData = transformInstitutionalData(workingDoc) match{
      case Success(item) => item
      case Failure(exception) => InstitutionalData.DEFAULT_EMPTY
    }

    //Academic Data
    val academicDoc = document.get("workingData") match {
      case Some(doc) => doc.asDocument()
      case None => null
    }
    val academicData: InstitutionalData = transformInstitutionalData(academicDoc) match{
      case Success(item) => item
      case Failure(exception) => InstitutionalData.DEFAULT_EMPTY
    }

    //Get LinkedInProfile
    val linkedInProfile: List[LinkedinUserProfile] = Try(transformProfiles(document.get("linkedInProfiles").get.asArray())) match {
      case Success(item) => item
      case Failure(exception) => List[LinkedinUserProfile]()
    }

    //Get Country
    val country : String = Try(document.get("country").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    //Get Emails
    val primaryEmail : String = Try(document.get("primaryEmail").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    val secondaryEmail : String = Try(document.get("secondaryEmail").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    //Get Dates
    val createdAt : String = Try(document.get("createdAt").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    val updatedAt : String = Try(document.get("updatedAt").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    val errorDate : String = Try(document.get("errorDate").get.asString().getValue) match{
      case Success(item) => item
      case Failure(exception) => ""
    }

    //Generate Prospect
    Prospect(document.get("_id").get.asString().getValue,
             document.get("firstName").get.asString().getValue,
             document.get("lastName").get.asString().getValue,
             document.get("documentType").get.asString().getValue,
             document.get("documentId").get.asString().getValue,
             document.get("birthDate").get.asString().getValue,
             workingData,
             academicData,
             nacionNews, infobaeNews, clarinNews, cronistaNews,
             linkedInProfile,
             country,
             primaryEmail, secondaryEmail,
             createdAt, updatedAt, errorDate)
  }

  private def transformNews(bson : BsonArray): List[News] = {
    bson.getValues.toList.map(_.asDocument()).map{x =>
      val validated : Boolean = try{x.get("validated").asBoolean().getValue} catch {case e:Exception => false}
      val rejected: Boolean = try {
        x.get("rejected").asBoolean().getValue
      } catch {
        case e: Exception => false
      }

      News(
        x.get("_id").asString().getValue,
        x.get("url").asString().getValue,
        x.get("title").asString().getValue,
        x.get("date").asString().getValue,
        x.get("tuft").asString().getValue,
        x.get("author").asString().getValue,
        validated,
        rejected
      )

    }
  }

  private def transformInstitutionalData(bson : BsonDocument): Try[InstitutionalData] = Try{

    val institution: Institution = transformInstitution(bson.get("institution").asDocument()) match {
      case Success(item) => item
      case Failure(exception) => Institution.DEFAULT_EMPTY
    }

    InstitutionalData(bson.get("_id").asString().getValue,
                      bson.get("entryDate").asString().getValue,
                      bson.get("exitDate").asString().getValue,
                      bson.get("title").asString().getValue,
                      bson.get("institutionCode").asString().getValue,
                      institution)
  }

  private def transformInstitution(bson : BsonDocument): Try[Institution] = Try{
    Institution(bson.get("_id").asString().getValue,
                bson.get("name").asString().getValue,
                bson.get("address").asString().getValue,
                bson.get("active").asBoolean().getValue,
                InstitutionType.withName(bson.get("institutionType").asString().getValue),
                InstitutionSector.withName(bson.get("sector").asString().getValue))
  }

  private def transformProfiles(bson: BsonArray): List[LinkedinUserProfile] = {
    bson.getValues.toList.map(_.asDocument()).map { x =>

      val validated: Boolean = Try(x.get("validated").asBoolean().getValue) match {
        case Success(element)=> element
        case Failure(exception) => false
      }

      val rejected: Boolean = Try(x.get("rejected").asBoolean().getValue) match {
        case Success(element)=> element
        case Failure(exception) => false
      }

      LinkedinUserProfile(x.get("_id").asString().getValue,
                          x.get("actualPosition").asString().getValue,
                          transformJobs(x.get("jobList").asArray()),
                          transformEducation(x.get("educationList").asArray()),
                          x.get("profileUrl").asString().getValue,
                          validated,
                          rejected)

    }.sortBy(p => !p.validated)

  }

  private def transformJobs(bson : BsonArray): List[LinkedinJob] = {
    bson.getValues.toList.map(_.asDocument()).map{x =>
      LinkedinJob(x.get("_id").asString().getValue,
                  x.get("position").asString().getValue,
                  x.get("workplace").asString().getValue,
                  x.get("workUrl").asString().getValue,
                  x.get("activityPeriod").asString().getValue,
                  x.get("jobDescription").asString().getValue)
    }
  }

  private def transformEducation(bson : BsonArray): List[LinkedinEducation] = {
    bson.getValues.toList.map(_.asDocument()).map{x =>
      LinkedinEducation(x.get("_id").asString().getValue,
                        x.get("institute").asString().getValue,
                        x.get("instituteUrl").asString().getValue,
                        x.get("title").asString().getValue,
                        x.get("educationPeriod").asString().getValue,
                        x.get("educationDescription").asString().getValue)
    }
  }

}

