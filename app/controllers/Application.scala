package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index.render())
  }

  def tables = Action {
    Ok(views.html.tables.render())
  }

  def login = Action {
    Ok(views.html.login.render())
  }



}