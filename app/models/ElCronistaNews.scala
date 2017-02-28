package models

/**
  * Created by Tomás on 28/02/2017.
  */
case class ElCronistaNews(
                       _id: String,
                       url: String,
                       title: String,
                       date: String,
                       tuft:String,
                       author: String
                     )

object ElCronistaNews {

  import play.api.libs.json.Json

  implicit val newsFormat = Json.format[ElCronistaNews]

}
