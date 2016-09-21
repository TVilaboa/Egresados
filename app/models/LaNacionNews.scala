package models

import java.util.Date

/**
  * Default (Template) Project
  * Created by jeronimocarlos on 9/12/16.
  */
case class LaNacionNews(
                _id: String,
                url: String,
                title: String,
                date: String,
                tuft:String,
                author: String
               )

  object LaNacionNews {

    import play.api.libs.json.Json

    implicit val newsFormat = Json.format[LaNacionNews]

  }
