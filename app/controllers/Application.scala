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


  def index = {
    homeFeed
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
    var errorLogs: List[String] = List()
    var successDate : String = ""
    var successLogs: List[String] = List()

    if(errorLines.nonEmpty){
      errorDate = errorLines.last.substring(0,10)
      if(errorDate.nonEmpty)
        errorLogs = errorLines.filter(_.contains(errorDate)).map(_.split(" ").last)
    }

    if(successLines.nonEmpty){
      successDate = successLines.last.substring(0,10)
      if(successDate.nonEmpty)
        successLogs = successLines.filter(_.contains(successDate)).map(_.split(" ").last)
    }

    Ok(views.html.index.render("", errorDate, errorLogs, successDate, successLogs))
  }
}