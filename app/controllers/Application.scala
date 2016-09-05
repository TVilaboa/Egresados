package controllers

import generators.{LaNacionUrlGenerator, LinkedInUrlGenerator}
import play.api.mvc._
import scrapers.LaNacionScraper

import scala.collection.mutable.ListBuffer

class Application extends Controller {

  //Test La Nacion URL Generator, se los dejo por si querian ver.
  val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
  val links: ListBuffer[String] = generator.searchLaNacionUrl("lopez gabeiras")
  val scraper: LaNacionScraper = new LaNacionScraper()
  println("*********************************")
  for(link <- links) {
    println(link)
    val list : List[String] = scraper.getArticleData(link)
    println(list.head)
    println(list(1))
    println(list(2))
    println(list(3))
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