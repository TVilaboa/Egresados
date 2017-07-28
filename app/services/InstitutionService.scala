package services

import java.text.Normalizer

import com.google.inject.Inject
import daos.InstitutionDao
import models.Institution
import org.mongodb.scala.Completed
import org.mongodb.scala.result._

import scala.concurrent.Future

/**
  * Created by franco on 27/07/17.
  */
class InstitutionService @Inject()(dao: InstitutionDao) {

  def all(): Future[Seq[Institution]] = dao.all()

  def find(_id: String): Future[Institution] = dao.find(_id)

  def findByName(name: String): Future[Institution] = dao.findByName(name)

  def save(institution: Institution): Future[Completed] = dao.save(institution)

  def update(institution: Institution): Future[UpdateResult] = dao.update(institution.copy())

  def drop(institution: Institution): Future[Institution] = dao.drop(institution)

}
