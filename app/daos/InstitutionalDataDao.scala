package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.mongodb.client.result.UpdateResult
import enums.{InstitutionSector, InstitutionType}
import models.{Institution, InstitutionalData}
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters.equal
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@ImplementedBy(classOf[MongoInstitutionalDataDao])
trait InstitutionalDataDao {
  def all(): Future[Seq[InstitutionalData]]

  def find(dataId : String): Future[InstitutionalData]

  def save(institutionalData: InstitutionalData): Future[Completed]

  def update(institutionalData: InstitutionalData): Future[UpdateResult]

  def drop(institutionalData: InstitutionalData) : Future[InstitutionalData]

}

@Singleton
class MongoInstitutionalDataDao @Inject()(mongo: Mongo) extends InstitutionalDataDao {

  private val data : MongoCollection[Document] = mongo.db.getCollection("institutionalData")

  override def all() = data.find().toFuture().map(doc => doc.map(transformDocument))

  override def find(dataId: String) = {
    data.find(equal("_id", dataId)).head().map[InstitutionalData]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def save(institutionalData: InstitutionalData) = {
    val json: String = Json.toJson(institutionalData).toString
    val doc: Document = Document(json)
    data.insertOne(doc).head()
  }

  override def update(institutionalData: InstitutionalData) = {
    data.replaceOne(equal("_id", institutionalData._id), Document(Json.toJson(institutionalData).toString)).head()
  }

  override def drop(institutionalData: InstitutionalData) = {
    data.findOneAndDelete(equal("_id", institutionalData._id)).head().map[InstitutionalData]((doc: Document) => {
      transformDocument(doc)
    })
  }

  def transformDocument(document: Document): InstitutionalData = {

    val institution: Institution = transformInstitution(document.get("institution").get.asDocument()) match {
      case Success(item) => item
      case Failure(exception) => Institution.DEFAULT_EMPTY
    }

    InstitutionalData(document.get("id").get.toString,
      document.get("entryDate").get.toString,
      document.get("exitDate").get.toString,
      document.get("title").get.toString,
      document.get("institutionCode").get.toString,
      institution)
  }

  def transformInstitution(document: BsonDocument): Try[Institution] = Try{
    Institution(document.get("_id").asString().getValue,
                document.get("name").asString().getValue,
                document.get("address").asString().getValue,
                document.get("active").asBoolean().getValue,
                InstitutionType.withName(document.get("institutionType").asString().getValue),
                InstitutionSector.withName(document.get("sector").asString().getValue))
  }
}
