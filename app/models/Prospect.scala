package models

import play.api.libs.json.{JsValue, Json}

/**
  * Created by franco on 27/07/17.
  */
case class Prospect(_id: String,
                    firstName: String,
                    lastName: String,
                    documentType: String,
                    documentId: String,
                    birthDate: String,
                    // Institution Information
                    workingData: InstitutionalData,
                    // Academy Information
                    academicData: InstitutionalData,
                    // News Data
                    nacionNews: List[News], infobaeNews: List[News], clarinNews: List[News], cronistaNews: List[News],
                    // LinkedIn Data
                    linkedInProfiles: List[LinkedinUserProfile],
                    country: String,
                    primaryEmail: String,
                    secondaryEmail: String,
                    createdAt: String, updatedAt: String, errorDate: String) {

  def toMap: Map[String,String] = Map("_id"->_id,
                                      "firstName"->firstName,
                                      "lastName"->lastName,
                                      "documentType"->documentType,
                                      "documentId"->documentId,
                                      "identification"->getIdentification,
                                      "birthDate"->birthDate,
                                      //Institution Data
                                      "entryDate"->workingData.entryDate,
                                      "exitDate"->workingData.exitDate,
                                      "institution"->workingData.institution._id,
                                      "institutionName"->workingData.institution.name,
                                      "institutionCode"->workingData.institutionCode,
                                      "title"->workingData.title,
                                      //Academy Data
                                      "academyEntryDate"->academicData.entryDate,
                                      "academyExitDate"->academicData.exitDate,
                                      "academy"->academicData.institution._id,
                                      "academyName"->academicData.institution.name,
                                      "academyCode"->academicData.institutionCode,
                                      "academyTitle"->academicData.title,
                                      "country"->country,
                                      "primaryEmail" -> primaryEmail,
                                      "secondaryEmail" -> secondaryEmail,
                                      "createdAt" -> createdAt, "updatedAt" -> updatedAt, "errorDate" -> errorDate)

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
                                         "entryDate" -> Json.toJson(workingData.entryDate),
                                         "exitDate" -> Json.toJson(workingData.exitDate),
                                         "code"->Json.toJson(workingData.institutionCode),
                                         "title"->Json.toJson(workingData.title),
                                         "institution" -> workingData.institution.toJson,
                                         "academyEntryDate" -> Json.toJson(academicData.entryDate),
                                         "academyExitDate" -> Json.toJson(academicData.exitDate),
                                         "academyCode"->Json.toJson(academicData.institutionCode),
                                         "academyTitle"->Json.toJson(academicData.title),
                                         "academy" -> academicData.institution.toJson,
                                         "createdAt" -> Json.toJson(createdAt),
                                         "updatedAt" -> Json.toJson(updatedAt),
                                         "errorDate" -> Json.toJson(errorDate)))
}

object Prospect {

  import play.api.libs.json.Json

  implicit val prospectFormat = Json.format[Prospect]

}
