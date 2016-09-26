package models

/**
  * Created by Nacho on 23/9/16.
  */
case class LinkedinJob(
                      _id: String,
                      position: String,
                      workplace: String,
                      workUrl: String,
                      activityPeriod: String,
                      jobDescription: String
                      )

object LinkedinJob {

  import play.api.libs.json.Json

  implicit val linkedinJobFormat = Json.format[LinkedinJob]
}