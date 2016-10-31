package models


import java.util.Date

import play.data.format.Formats.DateTime

/**
  * Created by Nacho on 20/9/16.
  */

case class LinkedinUserProfile(
                              _id: String,
                              actualPosition: String,
                              jobList: List[LinkedinJob],
                              educationList: List[LinkedinEducation],
                              profileUrl: String
                              )

object LinkedinUserProfile{

  import play.api.libs.json.Json

  implicit val linkedinUserProfileFormat = Json.format[LinkedinUserProfile]

}