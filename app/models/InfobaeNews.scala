package models

/**
  * Created by Brian Re & Michele Re
  */

case class InfobaeNews(
                         _id: String,
                         url: String,
                         title: String,
                         date: String,
                         tuft:String,
                         author: String
                       )

object InfobaeNews {

  import play.api.libs.json.Json

  implicit val newsFormat = Json.format[InfobaeNews]

}