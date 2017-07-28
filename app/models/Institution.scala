package models

/**
  * Created by franco on 27/07/17.
  */
case class Institution(_id : String, name : String, address :  String) {

}

object Institution {

  import play.api.libs.json.Json

  implicit val institutionFormat = Json.format[Institution]

}