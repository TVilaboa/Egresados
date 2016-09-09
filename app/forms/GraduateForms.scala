package forms

import play.api.libs.json.Json

/**
  * Created by Leandro on 05/09/2016.
  */
object GraduateForms {

  case class GraduateData(firstName: String, lastName: String, dni: String, birthday: String, birthmonth: String, birthyear: String, entryday: String, entrymonth: String, entryyear: String, graduationday: String, graduationmonth: String, graduationyear:String, carreer: String)
  implicit val signupFormat = Json.format[GraduateData]

}
