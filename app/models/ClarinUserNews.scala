package models

import java.util.Date

/**
  * Created by Tomás on 28/02/2017.
  */
case class ClarinUserNews(
                             news: List[ClarinNews],
                             timestamp: Date
                           )

object ClarinUserNews {

  import play.api.libs.json.Json

  implicit val ClarinUserNewsFormat = Json.format[ClarinUserNews]

}
