package services

import com.google.inject.Inject
import daos.ClarinNewsDao
import models.News
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class ClarinNewsService @Inject()(ClarinNewsDao: ClarinNewsDao) {

  def all(): Future[Seq[News]] = ClarinNewsDao.all()

  def find(ClarinNewsId: String): Future[News] = ClarinNewsDao.find(ClarinNewsId)

  def findByUrl(ClarinNewsUrl: String): Future[News] = ClarinNewsDao.findByUrl(ClarinNewsUrl)

  def findByTitle(ClarinNewsTitle: String): Future[News] = ClarinNewsDao.findByTitle(ClarinNewsTitle)

  def findByDate(ClarinNewsDate: String): Future[News] = ClarinNewsDao.findByDate(ClarinNewsDate)

  def findByTuft(ClarinNewsTuft: String): Future[News] = ClarinNewsDao.findByTuft(ClarinNewsTuft)

  def findByAuthor(ClarinNewsAuthor: String): Future[News] = ClarinNewsDao.findByAuthor(ClarinNewsAuthor)

  def update(ClarinNews: News): Future[UpdateResult] = ClarinNewsDao.update(ClarinNews)

  def save(ClarinNews: News): Future[Completed] = ClarinNewsDao.save(ClarinNews)

  def drop(ClarinNews: News) : Future[News] = ClarinNewsDao.drop(ClarinNews)

}