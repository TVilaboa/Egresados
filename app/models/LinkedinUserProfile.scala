package models

/**
  * Created by Nacho on 20/9/16.
  */

case class LinkedinUserProfile(
                                _id: String,
                                actualPosition: String,
                                jobList: List[LinkedinJob],
                                educationList: List[LinkedinEducation],
                                profileUrl: String,
                                validated: Boolean = false,
                                rejected: Boolean = false
                              ) {
  def selected: String = if (validated) "checked" else ""

}

object LinkedinUserProfile{

  import play.api.libs.json.Json

  implicit val linkedinUserProfileFormat = Json.format[LinkedinUserProfile]

}