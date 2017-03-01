package models

/**
  * Created by franco on 01/03/17.
  */
case class News(
                 _id: String,
                 url: String,
                 title: String,
                 date: String,
                 tuft:String,
                 author: String
               )

object News {

  import play.api.libs.json.Json

  implicit val newsFormat = Json.format[News]

}
