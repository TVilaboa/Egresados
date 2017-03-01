package services

import com.google.inject.Inject
import daos.LaNacionNewsDao
import models.News
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult
import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class LaNacionNewsService @Inject()(laNacionNewsDao: LaNacionNewsDao) {

  def all(): Future[Seq[News]] = laNacionNewsDao.all()

  def find(laNacionNewsId: String): Future[News] = laNacionNewsDao.find(laNacionNewsId)

  def findByUrl(laNacionNewsUrl: String): Future[News] = laNacionNewsDao.findByUrl(laNacionNewsUrl)

  def findByTitle(laNacionNewsTitle: String): Future[News] = laNacionNewsDao.findByTitle(laNacionNewsTitle)

  def findByDate(laNacionNewsDate: String): Future[News] = laNacionNewsDao.findByDate(laNacionNewsDate)

  def findByTuft(laNacionNewsTuft: String): Future[News] = laNacionNewsDao.findByTuft(laNacionNewsTuft)

  def findByAuthor(laNacionNewsAuthor: String): Future[News] = laNacionNewsDao.findByAuthor(laNacionNewsAuthor)

  def update(laNacionNews: News): Future[UpdateResult] = laNacionNewsDao.update(laNacionNews)

  def save(laNacionNews: News): Future[Completed] = laNacionNewsDao.save(laNacionNews)

  def drop(laNacionNews: News) : Future[News] = laNacionNewsDao.drop(laNacionNews)

}