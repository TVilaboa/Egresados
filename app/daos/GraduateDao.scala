package daos


import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.Graduate
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.UpdateResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo

import scala.concurrent.Future


/**
  * Created by Fede on 8/28/2016.
  */
@ImplementedBy(classOf[MongoGraduateDao])
trait GraduateDao {
  def find(graduateId: String): Future[Graduate]

  def findByDocumentId(documentId: String): Future[Graduate]

  def findByFirstName(firstName: String): Future[Graduate]

  def findByLastName(lastName: String): Future[Graduate]

  def update(graduate: Graduate): Future[UpdateResult]

  def save(graduate: Graduate): Future[Completed]

  def drop(graduate: Graduate) : Future[Completed]
}

@Singleton
class MongoGraduateDao @Inject()(mongo: Mongo) extends GraduateDao {
  private val graduates: MongoCollection[Document] = mongo.db.getCollection("graduate")

  override def find(graduateId: String): Future[Graduate] = {
    graduates.find(equal("_id", graduateId)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  override def findByDocumentId(documentId: String): Future[Graduate] = {
    graduates.find(equal("documentId", documentId)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  override def findByFirstName(firstName: String): Future[Graduate] = {
    graduates.find(equal("firstName", firstName)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  override def findByLastName(lastName: String): Future[Graduate] = {
    graduates.find(equal("lastName", lastName)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  override def update(graduate: Graduate): Future[UpdateResult] = {
    graduates.updateOne(equal("_id", graduate._id), Document(Json.toJson(graduate).toString)).head()
  }

  override def save(graduate: Graduate): Future[Completed] = {
    val graduateJson: String = Json.toJson(graduate).toString
    val doc: Document = Document(graduateJson)
    graduates.insertOne(doc).head()
  }

  override def drop(graduateId: Graduate) : Future[Graduate] = {
    graduates.findOneAndDelete(equal("_id", graduateId)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  private def documentToGraduate(doc: Document): Graduate = {
    Graduate(
      doc.get("_id").get.asString().getValue,
      doc.get("firstName").get.asString().getValue,
      doc.get("lastName").get.asString().getValue,
      doc.get("documentId").get.asString().getValue,
      doc.get("birthDate").get.asString().getValue,
      doc.get("entryDate").get.asString().getValue,
      doc.get("graduationDate").get.asString().getValue,
      doc.get("career").get.asString().getValue
    )
  }
}
