package daos

import java.util

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.{LinkedinEducation, LinkedinJob, LinkedinUserProfile}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.UpdateResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo
import scala.concurrent.Future

/**
  * Created by Ignacio Laner on 20/09/2016.
  */

@ImplementedBy(classOf[MongoLinkedinUserProfileDao])
trait LinkedinUserProfileDao {

  def all(): Future[Seq[LinkedinUserProfile]]

  def find(linkedinUserProfileId: String): Future[LinkedinUserProfile]

  def findByActualPosition(linkedinUserProfilePosition: String): Future[LinkedinUserProfile]

  def findByJobList(linkedinUserJobList:List[LinkedinJob]): Future[LinkedinUserProfile]

  def findByEducationList(linkedinUserEducationList:List[LinkedinEducation]): Future[LinkedinUserProfile]

  def findByProfileUrl(linkedinUserProfileUrl: String): Future[LinkedinUserProfile]

  def update(linkedinUserProfile : LinkedinUserProfile): Future[UpdateResult]

  def save(linkedinUserProfile : LinkedinUserProfile): Future[Completed]

  def drop(linkedinUserProfile : LinkedinUserProfile) : Future[LinkedinUserProfile]
}

@Singleton
class MongoLinkedinUserProfileDao @Inject()(mongo: Mongo) extends LinkedinUserProfileDao {
  private val linkedinUserProfile: MongoCollection[Document] = mongo.db.getCollection("linkedinUserProfile")

  override def all(): Future[Seq[LinkedinUserProfile]] = {
    linkedinUserProfile.find().toFuture().map(doc => doc.map(documentToLinkedinUserProfile))
  }

  override def find(linkedinUserProfileId: String): Future[LinkedinUserProfile] = {
    linkedinUserProfile.find(equal("_id", linkedinUserProfileId)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  override def findByActualPosition(linkedinUserProfilePosition: String): Future[LinkedinUserProfile] = {
    linkedinUserProfile.find(equal("actualPosition", linkedinUserProfilePosition)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  override def findByJobList(linkedinUserJobList: List[LinkedinJob]): Future[LinkedinUserProfile] = {
    linkedinUserProfile.find(equal("jobList", linkedinUserJobList)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  override def findByEducationList(linkedinUserEducationList: List[LinkedinEducation]): Future[LinkedinUserProfile] = {
    linkedinUserProfile.find(equal("educationList", linkedinUserEducationList)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  override def findByProfileUrl(linkedinUserProfileUrl: String): Future[LinkedinUserProfile] = {
    linkedinUserProfile.find(equal("profileUrl", linkedinUserProfileUrl)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  override def update(linkedinProfile: LinkedinUserProfile): Future[UpdateResult] = {
    linkedinUserProfile.updateOne(equal("_id", linkedinProfile._id), Document(Json.toJson(linkedinProfile).toString)).head()
  }

  override def save(linkedinProfile: LinkedinUserProfile): Future[Completed] = {
    val linkedinProfileSaver: String = Json.toJson(linkedinProfile).toString
    val doc: Document = Document(linkedinProfileSaver)
    linkedinUserProfile.insertOne(doc).head()
  }

  override def drop(linkedinProfile: LinkedinUserProfile) : Future[LinkedinUserProfile] = {
    linkedinUserProfile.findOneAndDelete(equal("_id", linkedinProfile._id)).head().map[LinkedinUserProfile]((doc: Document) => {
      documentToLinkedinUserProfile(doc)
    })
  }

  private def documentToLinkedinUserProfile(doc: Document): LinkedinUserProfile = {

    import collection.JavaConverters._

    LinkedinUserProfile(
      doc.get("_id").get.asString().getValue,
      doc.get("actualPosition").get.asString().getValue,
      doc.get("jobList").get.asArray().getValues.asScala.toList.asInstanceOf[List[LinkedinJob]],
      doc.get("educationList").get.asArray().getValues.asScala.toList.asInstanceOf[List[LinkedinEducation]],
      doc.get("profileUrl").get.asString().getValue
    )
  }

}