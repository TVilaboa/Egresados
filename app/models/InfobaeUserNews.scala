package models

import java.util.Date

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
case class InfobaeUserNews(
                             news: List[News],
                             timestamp: Date
                           )

object InfobaeUserNews {

  import play.api.libs.json.Json

  implicit val infobaeUserNewsFormat = Json.format[InfobaeUserNews]

}
