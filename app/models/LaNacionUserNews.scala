package models

import java.util.Date

/**
  * ismet-scalongo-seed
  * Created by jeronimocarlos on 9/12/16.
  */
case class LaNacionUserNews(
                             news: List[LaNacionNews],
                             timestamp: Date
                      )

object LaNacionUserNews {

  import play.api.libs.json.Json

  implicit val laNacionUserNewsFormat = Json.format[LaNacionUserNews]

}
