package services

import com.google.inject.Inject
import daos.{InfobaeNewsDao, LaNacionNewsDao}
import models.{InfobaeNews, LaNacionNews}
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult
import scala.concurrent.Future

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsService @Inject()(infobaeNewsDao: InfobaeNewsDao) {

  def all(): Future[Seq[InfobaeNews]] = infobaeNewsDao.all()

  def find(infobaeNewsId: String): Future[InfobaeNews] = infobaeNewsDao.find(infobaeNewsId)

  def findByUrl(infobaeNewsUrl: String): Future[InfobaeNews] = infobaeNewsDao.findByUrl(infobaeNewsUrl)

  def findByTitle(infobaeNewsTitle: String): Future[InfobaeNews] = infobaeNewsDao.findByTitle(infobaeNewsTitle)

  def findByDate(infobaeNewsDate: String): Future[InfobaeNews] = infobaeNewsDao.findByDate(infobaeNewsDate)

  def findByTuft(infobaeNewsTuft: String): Future[InfobaeNews] = infobaeNewsDao.findByTuft(infobaeNewsTuft)

  def findByAuthor(infobaeNewsAuthor: String): Future[InfobaeNews] = infobaeNewsDao.findByAuthor(infobaeNewsAuthor)

  def update(infobaeNews: InfobaeNews): Future[UpdateResult] = infobaeNewsDao.update(infobaeNews)

  def save(infobaeNews: InfobaeNews): Future[Completed] = infobaeNewsDao.save(infobaeNews)

  def drop(infobaeNews: InfobaeNews) : Future[InfobaeNews] = infobaeNewsDao.drop(infobaeNews)

}