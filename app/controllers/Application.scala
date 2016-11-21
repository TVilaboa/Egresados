package controllers

import com.google.inject.Inject
import generators._
import play.api.i18n.MessagesApi
import play.api.mvc._
import actions.SecureAction
import services.{SessionService, UserService}

import scala.collection.mutable.ListBuffer
import scala.io.Source

class Application @Inject()(secureAction: SecureAction) extends Controller {


  def index = secureAction {
    Ok(views.html.index.render("", "", 0, "", 0))
  }

  def tables = secureAction {
    Ok(views.html.tables.render())
  }

  def login = secureAction {
    Ok(views.html.login.render(null,"",null))
  }

  /**
    * Home Feed showing last scraping log result data
    * */
  def homeFeed = secureAction {
    val errorLines = Source.fromFile("logs/error.log").getLines.toList
    val successLines = Source.fromFile("logs/success.log").getLines.toList

    var errorDate : String = ""
    var errorLogCount : Int = 0
    var successDate : String = ""
    var successLogCount : Int = 0

    if(errorLines.nonEmpty){
      errorDate = errorLines.last.substring(0,10)
      if(errorDate.nonEmpty)
        errorLogCount = errorLines.count(_.contains(errorDate))
    }

    if(successLines.nonEmpty){
      successDate = successLines.last.substring(0,10)
      if(successDate.nonEmpty)
        successLogCount = successLines.count(_.contains(successDate))
    }

    Ok(views.html.index.render("", errorDate, errorLogCount, successDate, successLogCount))
  }
}