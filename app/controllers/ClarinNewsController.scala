package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actions.SecureAction
import com.google.inject.Inject
import generators.ClarinUrlGeneratorObject
import models.{News, Prospect}
import play.api.mvc.Controller
import scrapers.ClarinScraper
import services.{ClarinNewsService, ProspectService}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by matias on 21/09/2016.
  */
class ClarinNewsController @Inject()(newsClarinService: ClarinNewsService,
                                     prospectService: ProspectService,
                                     scraper: ClarinScraper,
                                     secureAction: SecureAction) extends Controller{

  def search(id: String) =secureAction{
    val prospect : Future[Prospect] = prospectService.find(id)

    prospect.map(runSearch)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll = secureAction{
    val prospects : Future[Seq[Prospect]] = prospectService.all()

    prospects.map(x => x.foreach(runSearch))

    Redirect(routes.ProspectController.index(""))
  }

  def runSearch(prospect: Prospect) : Unit = {
    val name : Option[String] = Option(prospect.getFullName)
    val links : Seq[String] = ClarinUrlGeneratorObject.search(name, Option(prospect.institution.name))

    val news : Seq[News] = links.map{x=> scraper.getArticleData(x,name,0)}.filter(_.isDefined).map(_.get)

    if(news.nonEmpty){
      val activeNews: List[String] = prospect.clarinNews.map(_.url)
      val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList
      difference.map(newsClarinService.save)

      val format : SimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd")
      val now : Date = Calendar.getInstance().getTime

      val all : List[News] = prospect.clarinNews ::: difference
      prospectService.update(prospect.copy(clarinNews = all, updatedAt = format.format(now)))
    }
  }

  def deleteNews(id:String) = secureAction {
    //Get graduate from DB.
    val news : News = Await.result(newsClarinService.find(id),Duration.Inf)
    Await.result(newsClarinService.drop(news), Duration.Inf)
    Redirect("/")
  }
}
