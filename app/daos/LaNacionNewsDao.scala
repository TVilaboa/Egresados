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

@ImplementedBy(classOf[MongoLaNacionNewsDao])
trait LaNacionNewsDao {

  def all(): Future[Seq[News]]

  def find(laNacionNewsId: String): Future[News]

  def findByUrl(laNacionNewsUrl: String): Future[News]

  def findByTitle(laNacionNewsTitle: String): Future[News]

  def findByDate(laNacionNewsDate: String): Future[News]

  def findByTuft(laNacionNewsTuft: String): Future[News]
  
  def findByAuthor(laNacionNewsAuthor: String): Future[News]

  def update(laNacionNews: News): Future[UpdateResult]

  def save(laNacionNews: News): Future[Completed]

  def drop(laNacionNews: News) : Future[News]
}

@Singleton
class MongoLaNacionNewsDao @Inject()(mongo: Mongo) extends LaNacionNewsDao {
  private val newsLaNacion: MongoCollection[Document] = mongo.db.getCollection("lanacionNews")

  override def all(): Future[Seq[News]] = {
    newsLaNacion.find().toFuture().map(doc => doc.map(documentToLaNacionNews))
  }

  override def find(laNacionNewsId: String): Future[News] = {
    newsLaNacion.find(equal("_id", laNacionNewsId)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByUrl(laNacionNewsUrl: String): Future[News] = {
    newsLaNacion.find(equal("url", laNacionNewsUrl)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByTitle(laNacionNewsTitle: String): Future[News] = {
    newsLaNacion.find(equal("title", laNacionNewsTitle)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByDate(laNacionNewsDate: String): Future[News] = {
    newsLaNacion.find(equal("date", laNacionNewsDate)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByTuft(laNacionNewsTuft: String): Future[News] = {
    newsLaNacion.find(equal("tuft", laNacionNewsTuft)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def findByAuthor(laNacionNewsAuthor: String): Future[News] = {
    newsLaNacion.find(equal("author", laNacionNewsAuthor)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  override def update(laNacionNews: News): Future[UpdateResult] = {
    newsLaNacion.updateOne(equal("_id", laNacionNews._id), Document(Json.toJson(laNacionNews).toString)).head()
  }

  override def save(laNacionNews: News): Future[Completed] = {
    val laNacionNewsSaver: String = Json.toJson(laNacionNews).toString
    val doc: Document = Document(laNacionNewsSaver)
    newsLaNacion.insertOne(doc).head()
  }

  override def drop(laNacionNews: News) : Future[News] = {
    newsLaNacion.findOneAndDelete(equal("_id", laNacionNews._id)).head().map[News]((doc: Document) => {
      documentToLaNacionNews(doc)
    })
  }

  private def documentToLaNacionNews(doc: Document): News = {
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

