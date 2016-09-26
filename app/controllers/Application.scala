package controllers

import com.google.inject.Inject
import generators._
import play.api.i18n.MessagesApi
import play.api.mvc._
import actions.SecureAction
import services.{SessionService, UserService}

import scala.collection.mutable.ListBuffer

class Application @Inject()(secureAction: SecureAction) extends Controller {


  def index = secureAction {

    Ok(views.html.index.render())
  }

  def tables = secureAction {
    Ok(views.html.tables.render())
  }

  def login = secureAction {
    Ok(views.html.login.render(null,"",null))
  }
}