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
                 author: String,
                 validated: Boolean,
                 rejected: Boolean = false
               ){
  def selected : String = if(validated) "checked" else ""
}

object News {

  import play.api.libs.json.Json

  implicit val newsFormat = Json.format[News]

}
