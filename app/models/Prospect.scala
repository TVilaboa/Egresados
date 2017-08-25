package models

import play.api.libs.json.{JsValue, Json}

/**
  * Created by franco on 27/07/17.
  */
case class Prospect(  _id : String,
                      firstName : String,
                      lastName : String,
                      documentType : String,
                      documentId : String,
                      birthDate : String,
                      entryDate : String,
                      exitDate : String,
                      institution : Institution,
                      institutionCode : String,
                      title : String,
                      nacionNews : List[News],
                      infobaeNews : List[News],
                      clarinNews : List[News],
                      cronistaNews : List[News],
                      linkedInProfile : LinkedinUserProfile,
                      country: String,
                      primaryEmail: String,
                      secondaryEmail: String,
                      createdAt : String,
                      updatedAt : String,
                      errorDate : String
                   ) {

  def toMap: Map[String,String] = Map("_id"->_id,
                                      "firstName"->firstName,
                                      "lastName"->lastName,
                                      "documentType"->documentType,
                                      "documentId"->documentId,
                                      "identification"->getIdentification,
                                      "birthDate"->birthDate,
                                      "entryDate"->entryDate,
                                      "exitDate"->exitDate,
                                      "institution"->institution._id,
                                      "institutionName"->institution.name,
                                      "institutionCode"->institutionCode,
                                      "title"->title,
                                      "country"->country,
                                      "primaryEmail" -> primaryEmail,
    "secondaryEmail" -> secondaryEmail,
    "createdAt" -> createdAt,
    "updatedAt" -> updatedAt,
    "errorDate" -> errorDate)

  def getFullName: String = s"$firstName $lastName"

  def getIdentification: String = s"${documentType.toUpperCase()} $documentId"

  def toJson : JsValue = Json.toJson(Map("_id"->Json.toJson(_id),
    "firstName" -> Json.toJson(firstName),
    "lastName" -> Json.toJson(lastName),
                                         "fullname"->Json.toJson(getFullName),
                                         "identification"->Json.toJson(getIdentification),
    "documentType" -> Json.toJson(documentType),
    "documentId" -> Json.toJson(documentId),
                                         "birthDate"->Json.toJson(birthDate),
                                         "country"->Json.toJson(country),
                                         "primaryEmail"->Json.toJson(primaryEmail),
                                         "secondaryEmail"->Json.toJson(secondaryEmail),
    "entryDate" -> Json.toJson(entryDate),
    "exitDate" -> Json.toJson(exitDate),
                                         "code"->Json.toJson(institutionCode),
                                         "title"->Json.toJson(title),
    "institution" -> institution.toJson,
    "createdAt" -> Json.toJson(createdAt),
    "updatedAt" -> Json.toJson(updatedAt),
    "errorDate" -> Json.toJson(errorDate)))
}

object Prospect {

  import play.api.libs.json.Json

  implicit val prospectFormat = Json.format[Prospect]

}
