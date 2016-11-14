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

  def homeFeed = secureAction {
    val errorLines = Source.fromFile("logs/error.log").getLines.toList
    val successLines = Source.fromFile("logs/success.log").getLines.toList

    val dateError = errorLines.last.substring(0, 10)
    val dateSuccess = successLines.last.substring(0, 10)

    val qtyError = errorLines.count(_.contains(dateError))
    val qtySuccess = successLines.count(_.contains(dateSuccess))

    Ok(views.html.index.render("", dateError, qtyError, dateSuccess, qtySuccess))
  }
}