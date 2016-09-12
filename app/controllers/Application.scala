package controllers

import java.sql.Timestamp
import java.util.Date

import generators.{LaNacionUrlGenerator, LinkedInUrlGenerator}
import models.{LaNacionUserNews, News}
import play.api.mvc._
import scrapers.LaNacionScraper

import scala.collection.mutable.ListBuffer

class Application extends Controller {

  //Test La Nacion URL Generator, se los dejo por si querian ver.
  val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
  val links: ListBuffer[String] = generator.searchLaNacionUrl("lopez gabeiras")
  val scraper: LaNacionScraper = new LaNacionScraper()
  val news: ListBuffer[News] = new ListBuffer[News]

  for(link <- links) {
    news += scraper.getArticleData(link)
    }
    val userNews: LaNacionUserNews = new LaNacionUserNews(news, new Timestamp(new Date().getTime))
    for(news <- userNews.getNews) {
      println(news.getUrl)
      println(news.getTitle)
      println(news.getDate)
      println(news.getTuft)
      println(news.getAuthor)
      println("*********************************")
  }


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