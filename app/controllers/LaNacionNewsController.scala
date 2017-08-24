package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actions.SecureAction
import com.google.inject.Inject
import generators.LaNacionUrlGeneratorObject
import models.{News, Prospect}
import play.api.mvc.Controller
import scrapers.LaNacionScraper
import services.{LaNacionNewsService, ProspectService}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Created by matias on 21/09/2016.
  */
class LaNacionNewsController @Inject() (newsLaNacionService: LaNacionNewsService,
                                        prospectService: ProspectService,
                                        scraper: LaNacionScraper,
                                        secureAction: SecureAction) extends Controller{

  def search(id: String) =secureAction{
    val prospect : Future[Prospect] = prospectService.find(id)

    prospect.map(runSearch)

    Redirect(routes.ProspectController.show(id))
  }

  def searchAll =secureAction{
    val prospects : Future[Seq[Prospect]] = prospectService.all()

    prospects.map(x => x.foreach(runSearch))

    Redirect(routes.ProspectController.index(""))
  }

  def runSearch(prospect: Prospect) : Unit = {
    val name : Option[String] = Option(prospect.getFullName)
    val links : Seq[String] = LaNacionUrlGeneratorObject.search(name, Option(prospect.institution.name))

    val news : Seq[News] = links.map{x=> scraper.getArticleData(x,name,0)}.filter(_.isDefined).map(_.get)

    if(news.nonEmpty){
      val activeNews: List[String] = prospect.nacionNews.map(_.url)
      val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

      if(difference.nonEmpty) {
        difference.map(newsLaNacionService.save)

        val format : SimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd")
        val now : Date = Calendar.getInstance().getTime

        val all : List[News] = prospect.nacionNews ::: difference
        prospectService.update(prospect.copy(nacionNews = all, updatedAt = format.format(now)))
      }
    }
  }

  def deleteNews(id:String) =secureAction {
    //Get graduate from DB.
    val news : News = Await.result(newsLaNacionService.find(id),Duration.Inf)
    Await.result(newsLaNacionService.drop(news), Duration.Inf)
    Redirect("/")
  }

}
