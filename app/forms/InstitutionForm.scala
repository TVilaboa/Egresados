package forms

import play.api.libs.json.Json

/**
  * Created by franco on 27/07/17.
  */
object InstitutionForm {

  case class InstitutionData(name : String, address :  String)

  implicit val dataFormat = Json.format[InstitutionData]
}
