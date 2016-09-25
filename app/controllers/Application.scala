package controllers

import generators._
import play.api.mvc._

import scala.collection.mutable.ListBuffer

class Application extends Controller {

  def index = Action {

    Ok(views.html.index.render())
  }

  def tables = Action {
    Ok(views.html.tables.render())
  }

  def login = Action {
    Ok(views.html.login.render(null,"",null))
  }
}