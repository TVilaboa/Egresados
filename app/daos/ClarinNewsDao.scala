package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.News
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.UpdateResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo
import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */

@ImplementedBy(classOf[MongoClarinNewsDao])
trait ClarinNewsDao {

  def all(): Future[Seq[News]]

  def find(ClarinNewsId: String): Future[News]

  def findByUrl(ClarinNewsUrl: String): Future[News]

  def findByTitle(ClarinNewsTitle: String): Future[News]

  def findByDate(ClarinNewsDate: String): Future[News]

  def findByTuft(ClarinNewsTuft: String): Future[News]
  
  def findByAuthor(ClarinNewsAuthor: String): Future[News]

  def update(ClarinNews: News): Future[UpdateResult]

  def save(ClarinNews: News): Future[Completed]

  def drop(ClarinNews: News) : Future[News]
}

@Singleton
class MongoClarinNewsDao @Inject()(mongo: Mongo) extends ClarinNewsDao {
  private val newsClarin: MongoCollection[Document] = mongo.db.getCollection("ClarinNews")

  override def all(): Future[Seq[News]] = {
    newsClarin.find().toFuture().map(doc => doc.map(documentToClarinNews))
  }

  override def find(ClarinNewsId: String): Future[News] = {
    newsClarin.find(equal("_id", ClarinNewsId)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByUrl(ClarinNewsUrl: String): Future[News] = {
    newsClarin.find(equal("url", ClarinNewsUrl)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByTitle(ClarinNewsTitle: String): Future[News] = {
    newsClarin.find(equal("title", ClarinNewsTitle)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByDate(ClarinNewsDate: String): Future[News] = {
    newsClarin.find(equal("date", ClarinNewsDate)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByTuft(ClarinNewsTuft: String): Future[News] = {
    newsClarin.find(equal("tuft", ClarinNewsTuft)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByAuthor(ClarinNewsAuthor: String): Future[News] = {
    newsClarin.find(equal("author", ClarinNewsAuthor)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def update(ClarinNews: News): Future[UpdateResult] = {
    newsClarin.updateOne(equal("_id", ClarinNews._id), Document(Json.toJson(ClarinNews).toString)).head()
  }

  override def save(ClarinNews: News): Future[Completed] = {
    val ClarinNewsSaver: String = Json.toJson(ClarinNews).toString
    val doc: Document = Document(ClarinNewsSaver)
    newsClarin.insertOne(doc).head()
  }

  override def drop(ClarinNews: News) : Future[News] = {
    newsClarin.findOneAndDelete(equal("_id", ClarinNews._id)).head().map[News]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  private def documentToClarinNews(doc: Document): News = {
    val validated : Boolean = try{doc.get("validated").get.asBoolean().getValue} catch {case e:Exception => false}

    News(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue,
      validated
    )
  }
  
}

