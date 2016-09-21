package controllers

import java.sql.Timestamp
import java.util.Date

import com.google.inject.Inject
import generators.LaNacionUrlGenerator
import models.{LaNacionUserNews, LaNacionNews}
import play.api.mvc.{Action, Controller}
import scrapers.LaNacionScraper
import services.LaNacionNewsService

import scala.collection.mutable.ListBuffer

/**
  * Created by matias on 21/09/2016.
  */
class LaNacionNewsController @Inject() (newsLaNacionService: LaNacionNewsService) extends Controller{

  def saveNews = Action {
    val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
    val links: ListBuffer[String] = generator.searchLaNacionUrl("lopez gabeiras")
    val scraper: LaNacionScraper = new LaNacionScraper()
    var news: List[LaNacionNews] = List[LaNacionNews]()
    var element: LaNacionNews = null

    for(link <- links) {
      element = scraper.getArticleData(link)
      newsLaNacionService.save(element)
      news = element :: news
      news = scraper.getArticleData(link) :: news
    }
    val userNews: LaNacionUserNews = new LaNacionUserNews(news, new Timestamp(new Date().getTime))
    for(news <- userNews.news) {
      println(news.url)
      println(news.title)
      println(news.date)
      println(news.tuft)
      println(news.author)
      println("*********************************")
    }
    Ok
  }

}
