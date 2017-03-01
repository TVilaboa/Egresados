package scrapers

import models.News

/**
  * Created by franco on 01/03/17.
  */
trait Scraper {

  def getArticleData(url : String, name : Option[String], cycle : Int): Option[News]

}
