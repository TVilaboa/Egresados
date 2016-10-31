package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.LaNacionNews
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

@ImplementedBy(classOf[MongoLaNacionNewsDao])
trait LaNacionNewsDao {

  def all(): Future[Seq[LaNacionNews]]

  def find(laNacionNewsId: String): Future[LaNacionNews]

  def findByUrl(laNacionNewsUrl: String): Future[LaNacionNews]

  def findByTitle(laNacionNewsTitle: String): Future[LaNacionNews]

  def findByDate(laNacionNewsDate: String): Future[LaNacionNews]

  def findByTuft(laNacionNewsTuft: String): Future[LaNacionNews]
  
  def findByAuthor(laNacionNewsAuthor: String): Future[LaNacionNews]

  def update(laNacionNews: LaNacionNews): Future[UpdateResult]

  def save(laNacionNews: LaNacionNews): Future[Completed]

  def drop(laNacionNews: LaNacionNews) : Future[LaNacionNews]
}

@Singleton
class MongoLaNacionNewsDao @Inject()(mongo: Mongo) extends LaNacionNewsDao {
  private val newsLaNacion: MongoCollection[Document] = mongo.db.getCollection("lanacionNews")

  override def all(): Future[Seq[LaNacionNews]] = {
    newsLaNacion.find().toFuture().map(doc => doc.map(documentToLaNacionNews))
  }

  override def find(laNacionNewsId: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("_id", laNacionNewsId)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByUrl(laNacionNewsUrl: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("url", laNacionNewsUrl)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByTitle(laNacionNewsTitle: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("title", laNacionNewsTitle)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByDate(laNacionNewsDate: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("date", laNacionNewsDate)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByTuft(laNacionNewsTuft: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("tuft", laNacionNewsTuft)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByAuthor(laNacionNewsAuthor: String): Future[LaNacionNews] = {
    newsLaNacion.find(equal("author", laNacionNewsAuthor)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def update(laNacionNews: LaNacionNews): Future[UpdateResult] = {
    newsLaNacion.updateOne(equal("_id", laNacionNews._id), Document(Json.toJson(laNacionNews).toString)).head()
  }

  override def save(laNacionNews: LaNacionNews): Future[Completed] = {
    val laNacionNewsSaver: String = Json.toJson(laNacionNews).toString
    val doc: Document = Document(laNacionNewsSaver)
    newsLaNacion.insertOne(doc).head()
  }

  override def drop(laNacionNews: LaNacionNews) : Future[LaNacionNews] = {
    newsLaNacion.findOneAndDelete(equal("_id", laNacionNews._id)).head().map[LaNacionNews]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  private def documentToLaNacionNews(doc: Document): LaNacionNews = {
    LaNacionNews(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue
    )
  }
  
}

