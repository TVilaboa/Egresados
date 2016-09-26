package models

import java.util.Date

/**
  * Created by Fede on 8/28/2016.
  */

case class Graduate(  _id: String,
                      firstName: String,
                      lastName: String,
                      documentId: String,
                      birthDate: String,
                      entryDate: String,
                      graduationDate: String,
                      career: String,
                      studentCode: String,
                      laNacionNews: List[LaNacionNews],
                      linkedinUserProfile: LinkedinUserProfile
                    )

object Graduate {

  import play.api.libs.json.Json

  implicit val graduateFormat = Json.format[Graduate]

}
