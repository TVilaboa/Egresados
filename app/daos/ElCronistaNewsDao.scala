package daos

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.ElCronistaNews
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

  def all(): Future[Seq[ElCronistaNews]]

  def find(ElCronistaNewsId: String): Future[ElCronistaNews]

  def findByUrl(ElCronistaNewsUrl: String): Future[ElCronistaNews]

  def findByTitle(ElCronistaNewsTitle: String): Future[ElCronistaNews]

  def findByDate(ElCronistaNewsDate: String): Future[ElCronistaNews]

  def findByTuft(ElCronistaNewsTuft: String): Future[ElCronistaNews]
  
  def findByAuthor(ElCronistaNewsAuthor: String): Future[ElCronistaNews]

  def update(ElCronistaNews: ElCronistaNews): Future[UpdateResult]

  def save(ElCronistaNews: ElCronistaNews): Future[Completed]

  def drop(ElCronistaNews: ElCronistaNews) : Future[ElCronistaNews]
}

@Singleton
class MongoElCronistaNewsDao @Inject()(mongo: Mongo) extends ElCronistaNewsDao {
  private val newsElCronista: MongoCollection[Document] = mongo.db.getCollection("ElCronistaNews")

  override def all(): Future[Seq[ElCronistaNews]] = {
    newsElCronista.find().toFuture().map(doc => doc.map(documentToElCronistaNews))
  }

  override def find(ElCronistaNewsId: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("_id", ElCronistaNewsId)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByUrl(ElCronistaNewsUrl: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("url", ElCronistaNewsUrl)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByTitle(ElCronistaNewsTitle: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("title", ElCronistaNewsTitle)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByDate(ElCronistaNewsDate: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("date", ElCronistaNewsDate)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByTuft(ElCronistaNewsTuft: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("tuft", ElCronistaNewsTuft)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def findByAuthor(ElCronistaNewsAuthor: String): Future[ElCronistaNews] = {
    newsElCronista.find(equal("author", ElCronistaNewsAuthor)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  override def update(ElCronistaNews: ElCronistaNews): Future[UpdateResult] = {
    newsElCronista.updateOne(equal("_id", ElCronistaNews._id), Document(Json.toJson(ElCronistaNews).toString)).head()
  }

  override def save(ElCronistaNews: ElCronistaNews): Future[Completed] = {
    val ElCronistaNewsSaver: String = Json.toJson(ElCronistaNews).toString
    val doc: Document = Document(ElCronistaNewsSaver)
    newsElCronista.insertOne(doc).head()
  }

  override def drop(ElCronistaNews: ElCronistaNews) : Future[ElCronistaNews] = {
    newsElCronista.findOneAndDelete(equal("_id", ElCronistaNews._id)).head().map[ElCronistaNews]((doc: Document) => {
      documentToElCronistaNews(doc)
    })
  }

  private def documentToElCronistaNews(doc: Document): ElCronistaNews = {
    ElCronistaNews(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue
    )
  }
  
}

