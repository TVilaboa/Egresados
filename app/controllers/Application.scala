package controllers

import generators.LaNacionUrlGenerator
import play.api.mvc._

import scala.collection.mutable.ListBuffer

class Application extends Controller {

  //Test La Nacion URL Generator, se los dejo por si querian ver.
  val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
  val links: ListBuffer[String] = generator.searchLaNacionUrl("lopez gabeiras")
  println("*********************************")
  for(link <- links) {
    println(link)
  }
  println("*********************************")


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