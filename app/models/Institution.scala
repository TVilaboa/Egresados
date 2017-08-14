package models

import enums._
import play.api.libs.json.{JsValue, Json}

/**
  * Created by franco on 27/07/17.
  */
case class Institution(_id: String, name: String, address: String, active: Boolean, institutionType: InstitutionType.Value, sector: InstitutionSector.Value) {

  def toMap: Map[String, String] = Map("_id" -> _id, "name" -> name, "address" -> address, "active" -> active.toString, "institutionType" -> institutionType.toString, "sector" -> sector.toString)

  def toJson: JsValue = Json.toJson(Map("_id" -> _id, "name" -> name, "address" -> address, "active" -> active.toString, "institutionType" -> institutionType.toString, "sector" -> sector.toString))
}

object Institution {

  import play.api.libs.json.Json

  implicit val institutionFormat = Json.format[Institution]

}