package models

import java.util.UUID

import play.api.libs.json.{JsValue, Json}

case class InstitutionalData(_id: String = UUID.randomUUID().toString,
                             entryDate: String,
                             exitDate: String,
                             title: String,
                             institutionCode: String,
                             institution: Institution) {

  def toMap: Map[String, String] = Map[String, String](
    "entryDate" -> entryDate,
    "exitDate" -> exitDate,
    "title" -> title,
    "institutionCode" -> institutionCode,
    "institution" -> institution._id,
    "institutionName" -> institution.name
  )

  def toJson: JsValue = Json.toJson(Map("_id"-> Json.toJson(_id),
                                        "entryDate"-> Json.toJson(entryDate),
                                        "exitDate"-> Json.toJson(exitDate),
                                        "title"-> Json.toJson(title),
                                        "InstitutionCode"-> Json.toJson(institutionCode),
                                        "institution"-> institution.toJson))

  def isEmpty: Boolean = entryDate.isEmpty || exitDate.isEmpty || title.isEmpty
}

object InstitutionalData {

  import play.api.libs.json.Json

  implicit val institutionalDataFormat = Json.format[InstitutionalData]

  def DEFAULT_EMPTY: InstitutionalData = InstitutionalData(UUID.randomUUID().toString, "", "", "", "", Institution.DEFAULT_EMPTY)
}
