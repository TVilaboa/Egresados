package services

import com.google.inject.Inject
import daos.ElCronistaNewsDao
import models.ElCronistaNews
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class ElCronistaNewsService @Inject()(ElCronistaNewsDao: ElCronistaNewsDao) {

  def all(): Future[Seq[ElCronistaNews]] = ElCronistaNewsDao.all()

  def find(ElCronistaNewsId: String): Future[ElCronistaNews] = ElCronistaNewsDao.find(ElCronistaNewsId)

  def findByUrl(ElCronistaNewsUrl: String): Future[ElCronistaNews] = ElCronistaNewsDao.findByUrl(ElCronistaNewsUrl)

  def findByTitle(ElCronistaNewsTitle: String): Future[ElCronistaNews] = ElCronistaNewsDao.findByTitle(ElCronistaNewsTitle)

  def findByDate(ElCronistaNewsDate: String): Future[ElCronistaNews] = ElCronistaNewsDao.findByDate(ElCronistaNewsDate)

  def findByTuft(ElCronistaNewsTuft: String): Future[ElCronistaNews] = ElCronistaNewsDao.findByTuft(ElCronistaNewsTuft)

  def findByAuthor(ElCronistaNewsAuthor: String): Future[ElCronistaNews] = ElCronistaNewsDao.findByAuthor(ElCronistaNewsAuthor)

  def update(ElCronistaNews: ElCronistaNews): Future[UpdateResult] = ElCronistaNewsDao.update(ElCronistaNews)

  def save(ElCronistaNews: ElCronistaNews): Future[Completed] = ElCronistaNewsDao.save(ElCronistaNews)

  def drop(ElCronistaNews: ElCronistaNews) : Future[ElCronistaNews] = ElCronistaNewsDao.drop(ElCronistaNews)

}