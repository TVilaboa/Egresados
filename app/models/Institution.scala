package models

import play.api.libs.json.{JsValue, Json}

/**
  * Created by franco on 27/07/17.
  */
case class Institution(_id : String, name : String, address :  String, active: Boolean) {

  def toMap: Map[String, String] = Map("_id"->_id,"name"->name, "address"->address)

  def toJson : JsValue = Json.toJson(Map("_id"->_id,"name"->name, "address"->address))
}

object Institution {

  import play.api.libs.json.Json

  implicit val institutionFormat = Json.format[Institution]

}