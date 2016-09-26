package daos


import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.{LaNacionNews, Graduate}
import org.bson.{BsonArray, BsonValue}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.UpdateResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo
import collection.JavaConversions._

import scala.concurrent.Future


/**
  * Created by Fede on 8/28/2016.
  */
@ImplementedBy(classOf[MongoGraduateDao])
trait GraduateDao {
  def all(): Future[Seq[Graduate]]
  
  def find(graduateId: String): Future[Graduate]

  def findByDocumentId(documentId: String): Future[Graduate]

  def findByFirstName(firstName: String): Future[Graduate]

  def findByLastName(lastName: String): Future[Graduate]

  def findByStudentCode(studentCode: String): Future[Graduate]

  def update(graduate: Graduate): Future[UpdateResult]

  def save(graduate: Graduate): Future[Completed]

  def drop(graduate: Graduate) : Future[Graduate]
}

@Singleton
class MongoGraduateDao @Inject()(mongo: Mongo) extends GraduateDao {
  private val graduates: MongoCollection[Document] = mongo.db.getCollection("graduate")

  override def all(): Future[Seq[Graduate]] = {
    graduates.find().toFuture().map(doc => doc.map(documentToGraduate))
  }

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

  override def findByStudentCode(studentCode: String): Future[Graduate] = {
    graduates.find(equal("studentCode", studentCode)).head().map[Graduate]((doc: Document) => {
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
    graduates.replaceOne(equal("_id", graduate._id), Document(Json.toJson(graduate).toString)).head()
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
    var nacionNews : List[LaNacionNews] =  List[LaNacionNews]()
    try{
      nacionNews = bsonToListLanacion(doc.get("laNacionNews").get.asArray())
    } catch {
      case _ => {
        println("Error: El egresado no tiene la lista de noticias generada")

      }
    }
    Graduate(
      doc.get("_id").get.asString().getValue,
      doc.get("firstName").get.asString().getValue,
      doc.get("lastName").get.asString().getValue,
      doc.get("documentId").get.asString().getValue,
      doc.get("birthDate").get.asString().getValue,
      doc.get("entryDate").get.asString().getValue,
      doc.get("graduationDate").get.asString().getValue,
      doc.get("career").get.asString().getValue,
      doc.get("studentCode").get.asString().getValue,
      nacionNews
     )
  }

  private def bsonToListLanacion(bson : BsonArray) : List[LaNacionNews] ={
    var news = List[LaNacionNews]()
    for(bsonV : BsonValue <- bson.getValues){
      var doc = bsonV.asDocument()
      news = news :+ LaNacionNews(doc.get("_id").asString().getValue,doc.get("url").asString().getValue,doc.get("title").asString().getValue,
        doc.get("date").asString().getValue,doc.get("tuft").asString().getValue,doc.get("author").asString().getValue)
    }
    return news
  }

}

