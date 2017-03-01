package services

import com.google.inject.Inject
import daos.{InfobaeNewsDao, LaNacionNewsDao}
import models.News
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult
import scala.concurrent.Future

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsService @Inject()(infobaeNewsDao: InfobaeNewsDao) {

  def all(): Future[Seq[News]] = infobaeNewsDao.all()

  def find(infobaeNewsId: String): Future[News] = infobaeNewsDao.find(infobaeNewsId)

  def findByUrl(infobaeNewsUrl: String): Future[News] = infobaeNewsDao.findByUrl(infobaeNewsUrl)

  def findByTitle(infobaeNewsTitle: String): Future[News] = infobaeNewsDao.findByTitle(infobaeNewsTitle)

  def findByDate(infobaeNewsDate: String): Future[News] = infobaeNewsDao.findByDate(infobaeNewsDate)

  def findByTuft(infobaeNewsTuft: String): Future[News] = infobaeNewsDao.findByTuft(infobaeNewsTuft)

  def findByAuthor(infobaeNewsAuthor: String): Future[News] = infobaeNewsDao.findByAuthor(infobaeNewsAuthor)

  def update(infobaeNews: News): Future[UpdateResult] = infobaeNewsDao.update(infobaeNews)

  def save(infobaeNews: News): Future[Completed] = infobaeNewsDao.save(infobaeNews)

  def drop(infobaeNews: News) : Future[News] = infobaeNewsDao.drop(infobaeNews)

}