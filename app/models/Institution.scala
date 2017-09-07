package models

import java.util.UUID

import enums._
import play.api.libs.json.{JsValue, Json}

/**
  * Created by franco on 27/07/17.
  */
case class Institution(_id: String = UUID.randomUUID().toString, name: String = "", address: String = "", active: Boolean = true, institutionType: InstitutionType.Value = InstitutionType.Unspecified, sector: InstitutionSector.Value = InstitutionSector.Unspecified) {

  def toMap: Map[String, String] = Map("_id" -> _id, "name" -> name, "address" -> address, "active" -> active.toString, "institutionType" -> institutionType.toString, "sector" -> sector.toString)

  def toJson: JsValue = Json.toJson(Map("_id" -> _id, "name" -> name, "address" -> address, "active" -> active.toString, "institutionType" -> institutionType.toString, "sector" -> sector.toString))
}

object Institution {

  import play.api.libs.json.Json

  implicit val institutionFormat = Json.format[Institution]

}