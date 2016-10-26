package daos


import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.{InfobaeNews, LaNacionNews}
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.UpdateResult
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import services.Mongo
import scala.concurrent.Future

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */

@ImplementedBy(classOf[MongoInfobaeNewsDao])
trait InfobaeNewsDao {

  def all(): Future[Seq[InfobaeNews]]

  def find(infobaeNewsId: String): Future[InfobaeNews]

  def findByUrl(infobaeNewsUrl: String): Future[InfobaeNews]

  def findByTitle(infobaeNewsTitle: String): Future[InfobaeNews]

  def findByDate(infobaeNewsDate: String): Future[InfobaeNews]

  def findByTuft(infobaeNewsTuft: String): Future[InfobaeNews]

  def findByAuthor(infobaeNewsAuthor: String): Future[InfobaeNews]

  def update(infobaeNews: InfobaeNews): Future[UpdateResult]

  def save(infobaeNews: InfobaeNews): Future[Completed]

  def drop(infobaeNews: InfobaeNews) : Future[InfobaeNews]
}

@Singleton
class MongoInfobaeNewsDao @Inject()(mongo: Mongo) extends InfobaeNewsDao {
  private val newsInfobae: MongoCollection[Document] = mongo.db.getCollection("infobaeNews")

  override def all(): Future[Seq[InfobaeNews]] = {
    newsInfobae.find().toFuture().map(doc => doc.map(documentToInfobaeNews))
  }

  override def find(infobaeNewsId: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("_id", infobaeNewsId)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByUrl(infobaeNewsUrl: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("url", infobaeNewsUrl)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByTitle(infobaeNewsTitle: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("title", infobaeNewsTitle)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByDate(infobaeNewsDate: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("date", infobaeNewsDate)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByTuft(infobaeNewsTuft: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("tuft", infobaeNewsTuft)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByAuthor(infobaeNewsAuthor: String): Future[InfobaeNews] = {
    newsInfobae.find(equal("author", infobaeNewsAuthor)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def update(infobaeNews: InfobaeNews): Future[UpdateResult] = {
    newsInfobae.updateOne(equal("_id", infobaeNews._id), Document(Json.toJson(infobaeNews).toString)).head()
  }

  override def save(infobaeNews: InfobaeNews): Future[Completed] = {
    val infobaeNewsSaver: String = Json.toJson(infobaeNews).toString
    val doc: Document = Document(infobaeNewsSaver)
    newsInfobae.insertOne(doc).head()
  }

  override def drop(infobaeNews: InfobaeNews) : Future[InfobaeNews] = {
    newsInfobae.findOneAndDelete(equal("_id", infobaeNews._id)).head().map[InfobaeNews]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  private def documentToInfobaeNews(doc: Document): InfobaeNews = {
    InfobaeNews(
      doc.get("_id").get.asString().getValue,
      doc.get("url").get.asString().getValue,
      doc.get("title").get.asString().getValue,
      doc.get("date").get.asString().getValue,
      doc.get("tuft").get.asString().getValue,
      doc.get("author").get.asString().getValue
    )
  }

}

