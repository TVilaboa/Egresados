package controllers

import play.api.mvc._

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController extends Controller {

  def search = Action {
    Ok(views.html.search.render())
  }

  def doSearch() = {}

}
