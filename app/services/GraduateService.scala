package services

import java.text.Normalizer

import com.google.inject.Inject
import daos.{GraduateDao, UserDao}
import models.{Graduate, User}
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

/**
  * Created by Fede on 8/28/2016.
  */
class GraduateService @Inject()(graduateDao: GraduateDao) {
  def all(): Future[Seq[Graduate]] = graduateDao.all()
  def find(graduateId: String): Future[Graduate] = graduateDao.find(graduateId)

  def findByDocumentId(documentId: String): Future[Graduate] = graduateDao.findByDocumentId(documentId)

  def findByFirstName(firstName: String): Future[Graduate] = graduateDao.findByFirstName(firstName)

  def findByLastName(lastName: String): Future[Graduate] = graduateDao.findByLastName(lastName)

  def findByStudentCode(studentCode : String): Future[Graduate] = graduateDao.findByStudentCode(studentCode)

  def update(graduate: Graduate): Future[UpdateResult] = graduateDao.update(graduate.copy(firstName = Normalizer.normalize(graduate.firstName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")))

  def save(graduate: Graduate): Future[Completed] = graduateDao.save(graduate)

  def drop(graduate: Graduate): Future[Graduate] = graduateDao.drop(graduate)

  def getNumberWithLinks : Future[Seq[(String,String,String,String)]] = graduateDao.getNumberWithLinks()
}
