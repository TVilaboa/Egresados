package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import com.mongodb.client.result.UpdateResult
import enums.{InstitutionSector, InstitutionType}
import models.Institution
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo

import scala.concurrent.Future

/**
  * Created by franco on 27/07/17.
  */
@ImplementedBy(classOf[MongoInstitutionDao])
trait InstitutionDao {
  def all(): Future[Seq[Institution]]

  def find(institutionId : String): Future[Institution]

  def findByName(name: String): Future[Institution]

  def save(institution: Institution): Future[Completed]

  def update(institution: Institution): Future[UpdateResult]

  def drop(institution: Institution) : Future[Institution]

}

@Singleton
class MongoInstitutionDao @Inject()(mongo: Mongo) extends InstitutionDao {

  private val data : MongoCollection[Document] = mongo.db.getCollection("institutions")

  override def all(): Future[Seq[Institution]] = data.find().toFuture().map(doc => doc.map(transformDocument))

  override def find(institutionId: String): Future[Institution] = {
    data.find(equal("_id", institutionId)).head().map[Institution]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def findByName(name: String): Future[Institution] = {
    data.find(equal("name", name)).head().map[Institution]((doc: Document) => {
      transformDocument(doc)
    })
  }

  override def save(institution: Institution): Future[Completed] = {
    val json: String = Json.toJson(institution).toString
    val doc: Document = Document(json)
    data.insertOne(doc).head()
  }

  override def update(institution: Institution): Future[UpdateResult] = data.replaceOne(equal("_id", institution._id), Document(Json.toJson(institution).toString)).head()

  override def drop(institution: Institution): Future[Institution] = {
    data.findOneAndDelete(equal("_id", institution._id)).head().map[Institution]((doc: Document) => {
      transformDocument(doc)
    })
  }

  private def transformDocument(document: Document) : Institution = {
    Institution(document.get("_id").get.asString().getValue,
                document.get("name").get.asString().getValue,
                document.get("address").get.asString().getValue,
      document.get("active").get.asBoolean().getValue,
      if (document.get("institutionType").isDefined) InstitutionType.withName(document.get("institutionType").get.asString().getValue) else InstitutionType.Unspecified,
      if (document.get("sector").isDefined) InstitutionSector.withName(document.get("sector").get.asString().getValue) else InstitutionSector.Unspecified
    )
  }
}