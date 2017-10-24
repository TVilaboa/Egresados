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
  * Created by Brian Re & Michele Re on 26/09/2016.
  */

@ImplementedBy(classOf[MongoInfobaeNewsDao])
trait InfobaeNewsDao {

  def all(): Future[Seq[News]]

  def find(infobaeNewsId: String): Future[News]

  def findByUrl(infobaeNewsUrl: String): Future[News]

  def findByTitle(infobaeNewsTitle: String): Future[News]

  def findByDate(infobaeNewsDate: String): Future[News]

  def findByTuft(infobaeNewsTuft: String): Future[News]

  def findByAuthor(infobaeNewsAuthor: String): Future[News]

  def update(infobaeNews: News): Future[UpdateResult]

  def save(infobaeNews: News): Future[Completed]

  def drop(infobaeNews: News) : Future[News]
}

@Singleton
class MongoInfobaeNewsDao @Inject()(mongo: Mongo) extends InfobaeNewsDao {
  private val newsInfobae: MongoCollection[Document] = mongo.db.getCollection("infobaeNews")

  override def all(): Future[Seq[News]] = {
    newsInfobae.find().toFuture().map(doc => doc.map(documentToInfobaeNews))
  }

  override def find(infobaeNewsId: String): Future[News] = {
    newsInfobae.find(equal("_id", infobaeNewsId)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByUrl(infobaeNewsUrl: String): Future[News] = {
    newsInfobae.find(equal("url", infobaeNewsUrl)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByTitle(infobaeNewsTitle: String): Future[News] = {
    newsInfobae.find(equal("title", infobaeNewsTitle)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByDate(infobaeNewsDate: String): Future[News] = {
    newsInfobae.find(equal("date", infobaeNewsDate)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByTuft(infobaeNewsTuft: String): Future[News] = {
    newsInfobae.find(equal("tuft", infobaeNewsTuft)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def findByAuthor(infobaeNewsAuthor: String): Future[News] = {
    newsInfobae.find(equal("author", infobaeNewsAuthor)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  override def update(infobaeNews: News): Future[UpdateResult] = {
    newsInfobae.updateOne(equal("_id", infobaeNews._id), Document(Json .toJson(infobaeNews).toString)).head()
  }

  override def save(infobaeNews: News): Future[Completed] = {
    val infobaeNewsSaver: String = Json.toJson(infobaeNews).toString
    val doc: Document = Document(infobaeNewsSaver)
    newsInfobae.insertOne(doc).head()
  }

  override def drop(infobaeNews: News) : Future[News] = {
    newsInfobae.findOneAndDelete(equal("_id", infobaeNews._id)).head().map[News]((doc: Document) => {
      documentToInfobaeNews(doc)
    })
  }

  private def documentToInfobaeNews(doc: Document): News = {
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

