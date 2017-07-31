package models

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
                      country: String
                   ) {

  def toMap: Map[String,String] = Map("_id"->_id,
                                      "firstName"->firstName,
                                      "lastName"->lastName,
                                      "documentType"->documentType,
                                      "documentId"->documentId,
                                      "birthDate"->birthDate,
                                      "entryDate"->entryDate,
                                      "exitDate"->exitDate,
                                      "institution"->institution._id,
                                      "institutionCode"->institutionCode,
                                      "title"->title,
                                      "country"->country)

  def getFullName: String = s"$firstName $lastName"

  def getIdentification: String = s"${documentType.toUpperCase()} $documentId"
}

object Prospect {

  import play.api.libs.json.Json

  implicit val prospectFormat = Json.format[Prospect]

}
