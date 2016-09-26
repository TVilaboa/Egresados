package services

import com.google.inject.Inject
import daos.LaNacionNewsDao
import models.LaNacionNews
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult
import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class LaNacionNewsService @Inject()(laNacionNewsDao: LaNacionNewsDao) {

  def all(): Future[Seq[LaNacionNews]] = laNacionNewsDao.all()

  def find(laNacionNewsId: String): Future[LaNacionNews] = laNacionNewsDao.find(laNacionNewsId)

  def findByUrl(laNacionNewsUrl: String): Future[LaNacionNews] = laNacionNewsDao.findByUrl(laNacionNewsUrl)

  def findByTitle(laNacionNewsTitle: String): Future[LaNacionNews] = laNacionNewsDao.findByTitle(laNacionNewsTitle)

  def findByDate(laNacionNewsDate: String): Future[LaNacionNews] = laNacionNewsDao.findByDate(laNacionNewsDate)

  def findByTuft(laNacionNewsTuft: String): Future[LaNacionNews] = laNacionNewsDao.findByTuft(laNacionNewsTuft)

  def findByAuthor(laNacionNewsAuthor: String): Future[LaNacionNews] = laNacionNewsDao.findByAuthor(laNacionNewsAuthor)

  def update(laNacionNews: LaNacionNews): Future[UpdateResult] = laNacionNewsDao.update(laNacionNews)

  def save(laNacionNews: LaNacionNews): Future[Completed] = laNacionNewsDao.save(laNacionNews)

  def drop(laNacionNews: LaNacionNews) : Future[LaNacionNews] = laNacionNewsDao.drop(laNacionNews)

}