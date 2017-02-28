package daos


import java.util.UUID

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models._
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

  def getNumberWithLinks() : Future[Seq[(String,String,String,String)]]
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

  override def drop(graduate: Graduate) : Future[Graduate] = {
    graduates.findOneAndDelete(equal("_id", graduate._id)).head().map[Graduate]((doc: Document) => {
      documentToGraduate(doc)
    })
  }

  private def documentToGraduate(doc: Document): Graduate = {
    var nacionNews : List[LaNacionNews] =  List[LaNacionNews]()

    var infobaeNews : List[InfobaeNews] =  List[InfobaeNews]()

    var clarinNews : List[ClarinNews] =  List[ClarinNews]()

    var elCronistaNews : List[ElCronistaNews] =  List[ElCronistaNews]()

    var linkedinUserProfile: LinkedinUserProfile = LinkedinUserProfile(
                                                      UUID.randomUUID().toString,
                                                      "",
                                                      List[LinkedinJob](),
                                                      List[LinkedinEducation](),
                                                      ""
                                                    )

    try{
      nacionNews = bsonToListLanacion(doc.get("laNacionNews").get.asArray())
    } catch {
      case e: Exception => {
        println("Error: El egresado no tiene la lista de noticias generada")
      }
    }

    try{
      infobaeNews = bsonToListInfobae(doc.get("infobaeNews").get.asArray())
    } catch {
      case e: Exception => {
        println("Error: El egresado no tiene la lista de noticias generada")

      }
    }

    try{
      clarinNews = bsonToListClarin(doc.get("clarinNews").get.asArray())
    } catch {
      case e: Exception => {
        println("Error: El egresado no tiene la lista de noticias generada")

      }
    }

    try{
      elCronistaNews = bsonToListElCronista(doc.get("elCronistaNews").get.asArray())
    } catch {
      case e: Exception => {
        println("Error: El egresado no tiene la lista de noticias generada")

      }
    }


    try{
      linkedinUserProfile = bsonToLinkedinUserProfile(doc.get("linkedinUserProfile").get)
    } catch {
      case e: IllegalStateException => {
        println("Error: El egresado no tiene el usuario de linkedin generado")
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
      nacionNews,
      infobaeNews,
      clarinNews,
      elCronistaNews,
      linkedinUserProfile
    )
  }

  private def bsonToListLanacion(bson : BsonArray) : List[LaNacionNews] ={
    var news = List[LaNacionNews]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      news = news :+ LaNacionNews(doc.get("_id").asString().getValue,doc.get("url").asString().getValue,doc.get("title").asString().getValue,
        doc.get("date").asString().getValue,doc.get("tuft").asString().getValue,doc.get("author").asString().getValue)
    }
    news
  }

  private def bsonToListInfobae(bson : BsonArray) : List[InfobaeNews] ={
    var news = List[InfobaeNews]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      news = news :+ InfobaeNews(doc.get("_id").asString().getValue,doc.get("url").asString().getValue,doc.get("title").asString().getValue,
        doc.get("date").asString().getValue,doc.get("tuft").asString().getValue,doc.get("author").asString().getValue)
    }
    news
  }

  private def bsonToListClarin(bson : BsonArray) : List[ClarinNews] ={
    var news = List[ClarinNews]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      news = news :+ ClarinNews(doc.get("_id").asString().getValue,doc.get("url").asString().getValue,doc.get("title").asString().getValue,
        doc.get("date").asString().getValue,doc.get("tuft").asString().getValue,doc.get("author").asString().getValue)
    }
    news
  }

  private def bsonToListElCronista(bson : BsonArray) : List[ElCronistaNews] ={
    var news = List[ElCronistaNews]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      news = news :+ ElCronistaNews(doc.get("_id").asString().getValue,doc.get("url").asString().getValue,doc.get("title").asString().getValue,
        doc.get("date").asString().getValue,doc.get("tuft").asString().getValue,doc.get("author").asString().getValue)
    }
    news
  }

  private def bsonToLinkedinUserProfile(bson : BsonValue) : LinkedinUserProfile ={
    var doc = bson.asDocument()
    LinkedinUserProfile(doc.get("_id").asString().getValue,doc.get("actualPosition").asString().getValue, bsonToListJobs(doc.get("jobList").asArray()), bsonToListEducation(doc.get("educationList").asArray()), doc.get("profileUrl").asString().getValue)
  }

  private def bsonToListJobs(bson : BsonArray) : List[LinkedinJob] ={
    var jobs = List[LinkedinJob]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      var position: String = null
      var workplace: String = null
      var workUrl: String = null
      var activityPeriod: String = null
      var jobDescription: String = null
      if(!doc.get("position").isNull()) {
        position = doc.get("position").asString().getValue()
      } else {
        position = "No hay informacion"
      }
      if(!doc.get("workplace").isNull()) {
        workplace = doc.get("workplace").asString().getValue()
      } else {
        workplace = "No hay informacion"
      }
      if(!doc.get("workUrl").isNull()) {
        workUrl = doc.get("workUrl").asString().getValue()
      } else {
        workUrl = "No hay informacion"
      }
      if(!doc.get("activityPeriod").isNull()) {
        activityPeriod = doc.get("activityPeriod").asString().getValue()
      } else {
        activityPeriod = "No hay informacion"
      }
      if(!doc.get("jobDescription").isNull()) {
        jobDescription = doc.get("jobDescription").asString().getValue()
      } else {
        jobDescription = "No hay informacion"
      }
      jobs = jobs :+ LinkedinJob(doc.get("_id").asString().getValue(),position,workplace, workUrl, activityPeriod, jobDescription)
    }
    jobs
  }

  private def bsonToListEducation(bson : BsonArray) : List[LinkedinEducation] ={
    var educationList = List[LinkedinEducation]()
    for(bsonV : BsonValue <- bson.getValues){
      val doc = bsonV.asDocument()
      var institute: String = null
      var instituteUrl: String = null
      var title: String = null
      var educationPeriod: String = null
      var educationDescription: String = null
      if(!doc.get("institute").isNull()) {
        institute = doc.get("institute").asString().getValue()
      } else {
        institute = "No hay informacion"
      }
      if(!doc.get("instituteUrl").isNull()) {
        instituteUrl = doc.get("instituteUrl").asString().getValue()
      } else {
        instituteUrl = "No hay informacion"
      }
      if(!doc.get("title").isNull()) {
        title = doc.get("title").asString().getValue()
      } else {
        title = "No hay informacion"
      }
      if(!doc.get("educationPeriod").isNull()) {
        educationPeriod = doc.get("educationPeriod").asString().getValue()
      } else {
        educationPeriod = "No hay informacion"
      }
      if(!doc.get("educationDescription").isNull()) {
        educationDescription = doc.get("educationDescription").asString().getValue()
      } else {
        educationDescription = "No hay informacion"
      }
      educationList = educationList :+ LinkedinEducation(doc.get("_id").asString().getValue(),institute,instituteUrl, title,educationPeriod,educationDescription)
    }
    educationList
  }
  override def getNumberWithLinks() : Future[Seq[(String, String, String, String)]] = {

    graduates.find().toFuture().map(doc => doc.filter(x => x.get("linkedinUserProfile").isDefined).filter(x => x.get("linkedinUserProfile").get.asDocument().get("profileUrl").asString().getValue.nonEmpty).map(y => (y.get("_id").get.asString().getValue,y.get("firstName").get.asString().getValue,y.get("lastName").get.asString().getValue,y.get("linkedinUserProfile").get.asDocument().get("profileUrl").asString().getValue)))
  }
}

