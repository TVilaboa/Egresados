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

}

object Prospect {

  import play.api.libs.json.Json

  implicit val prospectFormat = Json.format[Prospect]

}
