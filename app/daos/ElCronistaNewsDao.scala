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

@ImplementedBy(classOf[MongoElCronistaNewsDao])
trait ElCronistaNewsDao {

  def all(): Future[Seq[News]]

  def find(ElCronistaNewsId: String): Future[News]

  def findByUrl(ElCronistaNewsUrl: String): Future[News]

  def findByTitle(ElCronistaNewsTitle: String): Future[News]

  def findByDate(ElCronistaNewsDate: String): Future[News]

  def findByTuft(ElCronistaNewsTuft: String): Future[News]
  
  def findByAuthor(ElCronistaNewsAuthor: String): Future[News]

  def update(ElCronistaNews: News): Future[UpdateResult]

  def save(ElCronistaNews: News): Future[Completed]

  def drop(ElCronistaNews: News) : Future[News]
}

@Singleton
class MongoElCronistaNewsDao @Inject()(mongo: Mongo) extends ElCronistaNewsDao {
  private val newsElCronista: MongoCollection[Document] = mongo.db.getCollection("ElCronistaNews")

  override def all(): Future[Seq[News]] = {
    newsElCronista.find().toFuture().map(doc => doc.map(documentToElCronistaNews))
  }

  override def find(ElCronistaNewsId: String): Future[News] = {
    newsElCronista.find(equal("_id", ElCronistaNewsId)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByUrl(ElCronistaNewsUrl: String): Future[News] = {
    newsElCronista.find(equal("url", ElCronistaNewsUrl)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByTitle(ElCronistaNewsTitle: String): Future[News] = {
    newsElCronista.find(equal("title", ElCronistaNewsTitle)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByDate(ElCronistaNewsDate: String): Future[News] = {
    newsElCronista.find(equal("date", ElCronistaNewsDate)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByTuft(ElCronistaNewsTuft: String): Future[News] = {
    newsElCronista.find(equal("tuft", ElCronistaNewsTuft)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByAuthor(ElCronistaNewsAuthor: String): Future[News] = {
    newsElCronista.find(equal("author", ElCronistaNewsAuthor)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def update(ElCronistaNews: News): Future[UpdateResult] = {
    newsElCronista.updateOne(equal("_id", ElCronistaNews._id), Document(Json.toJson(ElCronistaNews).toString)).head()
  }

  override def save(ElCronistaNews: News): Future[Completed] = {
    val ElCronistaNewsSaver: String = Json.toJson(ElCronistaNews).toString
    val doc: Document = Document(ElCronistaNewsSaver)
    newsElCronista.insertOne(doc).head()
  }

  override def drop(ElCronistaNews: News) : Future[News] = {
    newsElCronista.findOneAndDelete(equal("_id", ElCronistaNews._id)).head().map[News]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  private def documentToElCronistaNews(doc: Document): News = {
    News(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue
    )
  }

}

