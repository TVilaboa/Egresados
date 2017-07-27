package services

import java.text.Normalizer

import com.google.inject.Inject
import daos.ProspectDao
import models.Prospect
import org.mongodb.scala.Completed
import org.mongodb.scala.result._

import scala.concurrent.Future

/**
  * Created by franco on 27/07/17.
  */
class ProspectService @Inject()(dao: ProspectDao) {

  def all(): Future[Seq[Prospect]] = dao.all()

  def find(graduateId: String): Future[Prospect] = dao.find(graduateId)

  def findByDocumentId(documentId: String): Future[Prospect] = dao.findByDocumentId(documentId)

  def findByFirstName(firstName: String): Future[Prospect] = dao.findByFirstName(firstName)

  def findByLastName(lastName: String): Future[Prospect] = dao.findByLastName(lastName)

  def findByInstitutionCode(institutionCode : String): Future[Prospect] = dao.findByInstitutionCode(institutionCode)

  def save(prospect: Prospect): Future[Completed] = dao.save(prospect)

  def update(prospect: Prospect): Future[UpdateResult] = dao.update(prospect.copy(firstName = Normalizer.normalize(prospect.firstName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "")))

  def drop(prospect: Prospect): Future[Prospect] = dao.drop(prospect)

}
