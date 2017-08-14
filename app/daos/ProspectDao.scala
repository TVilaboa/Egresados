package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import enums.{InstitutionSector, InstitutionType}
import models._
import org.mongodb.scala._
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo

import scala.collection.JavaConversions._
import scala.concurrent.Future

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

  def findByInstitutionCode(institutionCode: String): Future[Prospect]

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

  override def findByInstitutionCode(institutionCode: String): Future[Prospect] = {
    data.find(equal("institutionCode", institutionCode)).head().map[Prospect]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByInstitution(_institutionId: String): Future[Seq[Prospect]] = {
    data.find(equal("institution._id", _institutionId)).toFuture().map(doc => doc.map(transformDocument))
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
    val nacionNews: List[News] = try{ transformNews(document.get("nacionNews").get.asArray())} catch {case  e : Exception => List[News]()}
    val infobaeNews: List[News] = try{ transformNews(document.get("infobaeNews").get.asArray())} catch {case  e : Exception => List[News]()}
    val cronistaNews: List[News] = try{ transformNews(document.get("cronistaNews").get.asArray())} catch {case  e : Exception => List[News]()}
    val clarinNews: List[News] = try{ transformNews(document.get("clarinNews").get.asArray())} catch {case  e : Exception => List[News]()}

    //Get institution
    val institution: Institution = try {
      transformInstitution(document.get("institution").get.asDocument())
    } catch {
      case e: Exception => Institution("", "", "", active = false, null, null)
    }

    //Get LinkedInProfile
    val linkedInProfile : LinkedinUserProfile = try{ transformProfile(document.get("linkedInProfile").get.asDocument())} catch {case  e : Exception => LinkedinUserProfile("","",Nil,Nil,"")}

    //Get Country
    val country : String = try{ document.get("country").get.asString().getValue} catch {case  e : Exception => "" }

    //Get Emails
    val primaryEmail : String = try{ document.get("primaryEmail").get.asString().getValue} catch {case  e : Exception => "" }
    val secondaryEmail : String = try{ document.get("secondaryEmail").get.asString().getValue} catch {case  e : Exception => "" }

    //Generate Prospect
    Prospect(
      document.get("_id").get.asString().getValue,
      document.get("firstName").get.asString().getValue,
      document.get("lastName").get.asString().getValue,
      document.get("documentType").get.asString().getValue,
      document.get("documentId").get.asString().getValue,
      document.get("birthDate").get.asString().getValue,
      document.get("entryDate").get.asString().getValue,
      document.get("exitDate").get.asString().getValue,
      institution,
      document.get("institutionCode").get.asString().getValue,
      document.get("title").get.asString().getValue,
      nacionNews,
      infobaeNews,
      clarinNews,
      cronistaNews,
      linkedInProfile,
      country,
      primaryEmail,
      secondaryEmail
    )
  }

  private def transformNews(bson : BsonArray): List[News] = {
    bson.getValues.toList.map(_.asDocument()).map{x =>
      News( x.get("_id").asString().getValue,
            x.get("url").asString().getValue,
            x.get("title").asString().getValue,
            x.get("date").asString().getValue,
            x.get("tuft").asString().getValue,
            x.get("author").asString().getValue)
    }
  }

  private def transformInstitution(bson : BsonDocument): Institution = {
    Institution(bson.get("_id").asString().getValue, bson.get("name").asString().getValue, bson.get("address").asString().getValue, bson.get("active").asBoolean().getValue,
      InstitutionType.withName(bson.get("institutionType").asString().getValue),
      InstitutionSector.withName(bson.get("sector").asString().getValue))
  }

  private def transformProfile(bson : BsonDocument): LinkedinUserProfile = {
    LinkedinUserProfile(bson.get("_id").asString().getValue,
                        bson.get("actualPosition").asString().getValue,
                        transformJobs(bson.get("jobList").asArray()),
                        transformEducation(bson.get("educationList").asArray()),
                        bson.get("profileUrl").asString().getValue)
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

