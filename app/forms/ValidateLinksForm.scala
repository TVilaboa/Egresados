package forms

import play.api.libs.json.Json

/**
  * Created by Leandro on 17/10/16.
  */
object ValidateLinksForm {

  case class ValidateLinksData(id: String, infobaeLinks: Array[String], laNacionLinks: Array[String], linkedInLinks: Array[String])

  implicit val linksFormat = Json.format[ValidateLinksData]

}
