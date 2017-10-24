package services

import com.google.inject.Inject
import daos.ElCronistaNewsDao
import models.News
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class ElCronistaNewsService @Inject()(ElCronistaNewsDao: ElCronistaNewsDao) {

  def all(): Future[Seq[News]] = ElCronistaNewsDao.all()

  def find(ElCronistaNewsId: String): Future[News] = ElCronistaNewsDao.find(ElCronistaNewsId)

  def findByUrl(ElCronistaNewsUrl: String): Future[News] = ElCronistaNewsDao.findByUrl(ElCronistaNewsUrl)

  def findByTitle(ElCronistaNewsTitle: String): Future[News] = ElCronistaNewsDao.findByTitle(ElCronistaNewsTitle)

  def findByDate(ElCronistaNewsDate: String): Future[News] = ElCronistaNewsDao.findByDate(ElCronistaNewsDate)

  def findByTuft(ElCronistaNewsTuft: String): Future[News] = ElCronistaNewsDao.findByTuft(ElCronistaNewsTuft)

  def findByAuthor(ElCronistaNewsAuthor: String): Future[News] = ElCronistaNewsDao.findByAuthor(ElCronistaNewsAuthor)

  def update(ElCronistaNews: News): Future[UpdateResult] = ElCronistaNewsDao.update(ElCronistaNews)

  def save(ElCronistaNews: News): Future[Completed] = ElCronistaNewsDao.save(ElCronistaNews)

  def drop(ElCronistaNews: News) : Future[News] = ElCronistaNewsDao.drop(ElCronistaNews)

}