package controllers

import java.sql.Timestamp
import java.util.Date

import actions.SecureAction
import com.google.inject.Inject
import generators.{LaNacionUrlGeneratorObject, LaNacionUrlGenerator}
import models.{Graduate, LaNacionUserNews, LaNacionNews}
import play.api.mvc.{Action, Controller}
import scrapers.LaNacionScraper
import services.{GraduateService, LaNacionNewsService}

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by matias on 21/09/2016.
  */
class LaNacionNewsController @Inject() (newsLaNacionService: LaNacionNewsService,graduateService: GraduateService, secureAction: SecureAction) extends Controller{

  def saveNews = secureAction {
    val generator: LaNacionUrlGenerator = new LaNacionUrlGenerator()
    val links = LaNacionUrlGeneratorObject.search(Option("lopez gabeiras"),Option("Universidad Austral"))
    val scraper: LaNacionScraper = new LaNacionScraper()
    var news: List[LaNacionNews] = List[LaNacionNews]()
    var element: LaNacionNews = null
    var graduate : Graduate = Await.result(graduateService.findByLastName("Testori"),Duration.Inf)
    for(link <- links) {
      element = scraper.getArticleData(link)
      newsLaNacionService.save(element)
      news = element :: news

      //news = scraper.getArticleData(link) :: news
    }
    graduate = graduate.copy(laNacionNews = news)
    var result = Await.result(graduateService.update(graduate),Duration.Inf)

    Ok
  }

}
