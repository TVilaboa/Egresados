package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.ClarinNews
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

  def all(): Future[Seq[ClarinNews]]

  def find(ClarinNewsId: String): Future[ClarinNews]

  def findByUrl(ClarinNewsUrl: String): Future[ClarinNews]

  def findByTitle(ClarinNewsTitle: String): Future[ClarinNews]

  def findByDate(ClarinNewsDate: String): Future[ClarinNews]

  def findByTuft(ClarinNewsTuft: String): Future[ClarinNews]
  
  def findByAuthor(ClarinNewsAuthor: String): Future[ClarinNews]

  def update(ClarinNews: ClarinNews): Future[UpdateResult]

  def save(ClarinNews: ClarinNews): Future[Completed]

  def drop(ClarinNews: ClarinNews) : Future[ClarinNews]
}

@Singleton
class MongoClarinNewsDao @Inject()(mongo: Mongo) extends ClarinNewsDao {
  private val newsClarin: MongoCollection[Document] = mongo.db.getCollection("ClarinNews")

  override def all(): Future[Seq[ClarinNews]] = {
    newsClarin.find().toFuture().map(doc => doc.map(documentToClarinNews))
  }

  override def find(ClarinNewsId: String): Future[ClarinNews] = {
    newsClarin.find(equal("_id", ClarinNewsId)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByUrl(ClarinNewsUrl: String): Future[ClarinNews] = {
    newsClarin.find(equal("url", ClarinNewsUrl)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByTitle(ClarinNewsTitle: String): Future[ClarinNews] = {
    newsClarin.find(equal("title", ClarinNewsTitle)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByDate(ClarinNewsDate: String): Future[ClarinNews] = {
    newsClarin.find(equal("date", ClarinNewsDate)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByTuft(ClarinNewsTuft: String): Future[ClarinNews] = {
    newsClarin.find(equal("tuft", ClarinNewsTuft)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def findByAuthor(ClarinNewsAuthor: String): Future[ClarinNews] = {
    newsClarin.find(equal("author", ClarinNewsAuthor)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  override def update(ClarinNews: ClarinNews): Future[UpdateResult] = {
    newsClarin.updateOne(equal("_id", ClarinNews._id), Document(Json.toJson(ClarinNews).toString)).head()
  }

  override def save(ClarinNews: ClarinNews): Future[Completed] = {
    val ClarinNewsSaver: String = Json.toJson(ClarinNews).toString
    val doc: Document = Document(ClarinNewsSaver)
    newsClarin.insertOne(doc).head()
  }

  override def drop(ClarinNews: ClarinNews) : Future[ClarinNews] = {
    newsClarin.findOneAndDelete(equal("_id", ClarinNews._id)).head().map[ClarinNews]((doc: Document) => {
      documentToClarinNews(doc)
    })
  }

  private def documentToClarinNews(doc: Document): ClarinNews = {
    ClarinNews(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue
    )
  }
  
}

