package services


import com.google.inject.Inject
import daos.LinkedinUserProfileDao
import models.{LinkedinEducation, LinkedinJob, LinkedinUserProfile}
import org.mongodb.scala.Completed
import org.mongodb.scala.result.UpdateResult
import play.data.format.Formats.DateTime

import scala.concurrent.Future

class LinkedinUserProfileService @Inject()(linkedinUserProfileDao: LinkedinUserProfileDao) {

  def all(): Future[Seq[LinkedinUserProfile]] = linkedinUserProfileDao.all()

  def find(linkedinUserProfileId: String): Future[LinkedinUserProfile] = linkedinUserProfileDao.find(linkedinUserProfileId)

  def findByActualPosition(linkedinUserProfilePosition: String): Future[LinkedinUserProfile] = linkedinUserProfileDao.findByActualPosition(linkedinUserProfilePosition)

  def findByJobList(linkedinUserJobList: List[LinkedinJob]): Future[LinkedinUserProfile] = linkedinUserProfileDao.findByJobList(linkedinUserJobList)

  def findByEducationList(linkedinUserEducationList:List[LinkedinEducation]): Future[LinkedinUserProfile] = linkedinUserProfileDao.findByEducationList(linkedinUserEducationList)

  def findByProfileUrl(linkedinUserProfileUrl: String): Future[LinkedinUserProfile] = linkedinUserProfileDao.findByProfileUrl(linkedinUserProfileUrl)

  def update(linkedinUserProfile : LinkedinUserProfile): Future[UpdateResult] = linkedinUserProfileDao.update(linkedinUserProfile)

  def save(linkedinUserProfile : LinkedinUserProfile): Future[Completed] = linkedinUserProfileDao.save(linkedinUserProfile)

  def drop(linkedinUserProfile : LinkedinUserProfile) : Future[LinkedinUserProfile] = linkedinUserProfileDao.drop(linkedinUserProfile)
}
