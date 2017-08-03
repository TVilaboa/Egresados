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
                      secondaryEmail: String
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
                                      "secondaryEmail" -> secondaryEmail)

  def getFullName: String = s"$firstName $lastName"

  def getIdentification: String = s"${documentType.toUpperCase()} $documentId"

  def toJson : JsValue = Json.toJson(Map("_id"->Json.toJson(_id),
                                         "fullname"->Json.toJson(getFullName),
                                         "identification"->Json.toJson(getIdentification),
                                         "birthDate"->Json.toJson(birthDate),
                                         "country"->Json.toJson(country),
                                         "primaryEmail"->Json.toJson(primaryEmail),
                                         "secondaryEmail"->Json.toJson(secondaryEmail),
                                         "entry"->Json.toJson(entryDate),
                                         "exit"->Json.toJson(exitDate),
                                         "code"->Json.toJson(institutionCode),
                                         "title"->Json.toJson(title),
                                         "institution"->institution.toJson))
}

object Prospect {

  import play.api.libs.json.Json

  implicit val prospectFormat = Json.format[Prospect]

}
