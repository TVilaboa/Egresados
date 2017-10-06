package services

import com.google.inject.Inject
import com.mongodb.client.result.UpdateResult
import daos.InstitutionalDataDao
import models.InstitutionalData
import org.mongodb.scala.Completed

import scala.concurrent.Future

class InstitutionalDataService @Inject()(dao: InstitutionalDataDao){

  def all(): Future[Seq[InstitutionalData]] = dao.all()

  def find(dataId : String): Future[InstitutionalData] = dao.find(dataId)

  def save(institutionalData: InstitutionalData): Future[Completed] = dao.save(institutionalData)

  def update(institutionalData: InstitutionalData): Future[UpdateResult] = dao.update(institutionalData)

  def drop(institutionalData: InstitutionalData) : Future[InstitutionalData] = dao.drop(institutionalData)

}
