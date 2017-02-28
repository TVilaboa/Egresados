package services

import com.google.inject.Inject
import daos.ClarinNewsDao
import models.ClarinNews
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
  * Created by Ignacio Vazquez on 19/09/2016.
  */
class ClarinNewsService @Inject()(ClarinNewsDao: ClarinNewsDao) {

  def all(): Future[Seq[ClarinNews]] = ClarinNewsDao.all()

  def find(ClarinNewsId: String): Future[ClarinNews] = ClarinNewsDao.find(ClarinNewsId)

  def findByUrl(ClarinNewsUrl: String): Future[ClarinNews] = ClarinNewsDao.findByUrl(ClarinNewsUrl)

  def findByTitle(ClarinNewsTitle: String): Future[ClarinNews] = ClarinNewsDao.findByTitle(ClarinNewsTitle)

  def findByDate(ClarinNewsDate: String): Future[ClarinNews] = ClarinNewsDao.findByDate(ClarinNewsDate)

  def findByTuft(ClarinNewsTuft: String): Future[ClarinNews] = ClarinNewsDao.findByTuft(ClarinNewsTuft)

  def findByAuthor(ClarinNewsAuthor: String): Future[ClarinNews] = ClarinNewsDao.findByAuthor(ClarinNewsAuthor)

  def update(ClarinNews: ClarinNews): Future[UpdateResult] = ClarinNewsDao.update(ClarinNews)

  def save(ClarinNews: ClarinNews): Future[Completed] = ClarinNewsDao.save(ClarinNews)

  def drop(ClarinNews: ClarinNews) : Future[ClarinNews] = ClarinNewsDao.drop(ClarinNews)

}