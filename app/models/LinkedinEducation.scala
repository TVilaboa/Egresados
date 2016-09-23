package models

/**
  * Created by Nacho on 23/9/16.
  */
case class LinkedinEducation(
                            _id: String,
                            institute: String,
                            instituteUrl: String,
                            title: String,
                            educationPeriod: String,
                            educationDescription: String
                            )
object LinkedinEducation{

  import play.api.libs.json.Json

  implicit val linkedinEducationFormat = Json.format[LinkedinEducation]
}
