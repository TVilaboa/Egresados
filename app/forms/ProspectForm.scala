package forms

import play.api.libs.json.Json

/**
  * Created by franco on 27/07/17.
  */
object ProspectForm {

  case class ProspectData( firstName : String, lastName : String, documentType : String, documentId : String, birthDate : String,
                           entryDate : String, exitDate : String, title : String)

  implicit val dataFormat = Json.format[ProspectData]

}
