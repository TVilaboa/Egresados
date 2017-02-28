package models

import java.util.Date

/**
  * Default (Template) Project
  * Created by Tomás on 28/02/2017.
  */
case class ClarinNews(
                         _id: String,
                         url: String,
                         title: String,
                         date: String,
                         tuft:String,
                         author: String
                       )

object ClarinNews {

  import play.api.libs.json.Json

  implicit val newsFormat = Json.format[ClarinNews]

}
