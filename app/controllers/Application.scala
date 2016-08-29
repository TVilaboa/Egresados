package controllers

import generator.InfobaeUrlGenerator
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    val gen : InfobaeUrlGenerator = new InfobaeUrlGenerator()
    val url = gen.searchInfobaeUrl("cassol",null,"austral")
    print(url)
    Ok(views.html.index.render())
  }

  def tables = Action {
    Ok(views.html.tables.render())
  }

  def login = Action {
    Ok(views.html.login.render())
  }



}