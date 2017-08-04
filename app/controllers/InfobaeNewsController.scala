package controllers

import actions.SecureAction
import com.google.inject.Inject
import generators.InfobaeUrlGeneratorObject
import models._
import play.api.mvc.{Action, Controller}
import scrapers.InfobaeScraper
import services.{InfobaeNewsService, ProspectService}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsController @Inject() (newsInfobaeService: InfobaeNewsService,
                                       prospectService: ProspectService,
                                       scraper: InfobaeScraper,secureAction: SecureAction) extends Controller{

  def search(id: String) =secureAction{
    val prospect : Prospect = Await.result(prospectService.find(id), Duration.Inf)

    runSearch(prospect)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll =secureAction{
    val prospects : Seq[Prospect] = Await.result(prospectService.all(), Duration.Inf)

    prospects.foreach(x=>runSearch(x))

    Redirect(routes.ProspectController.index(""))
  }

  def runSearch(prospect: Prospect) : Unit = {
    val name : Option[String] = Option(prospect.getFullName)
    val links : Seq[String] = InfobaeUrlGeneratorObject.search(name, Option(prospect.institution.name))

    val news : Seq[News] = links.map{x=> scraper.getArticleData(x,name,0)}.filter(_.isDefined).map(_.get)

    if(news.nonEmpty){
      val activeNews: List[String] = prospect.infobaeNews.map(_.url)
      val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList
      difference.map(newsInfobaeService.save)

      val all : List[News] = prospect.infobaeNews ::: difference
      Await.result(prospectService.update(prospect.copy(infobaeNews = all)), Duration.Inf)
    }
  }
}
