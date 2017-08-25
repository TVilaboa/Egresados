package controllers

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import actions.SecureAction
import com.google.inject.Inject
import generators.InfobaeUrlGeneratorObject
import models._
import play.api.mvc.Controller
import scrapers.InfobaeScraper
import services.{InfobaeNewsService, ProspectService}

import scala.concurrent.Future

/**
  * Created by Brian Re & Michele Re on 26/09/2016.
  */
class InfobaeNewsController @Inject() (newsInfobaeService: InfobaeNewsService,
                                       prospectService: ProspectService,
                                       scraper: InfobaeScraper,secureAction: SecureAction) extends Controller{

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
    val links : Seq[String] = InfobaeUrlGeneratorObject.search(name, Option(prospect.institution.name))

    val news : Seq[News] = links.map{x=> scraper.getArticleData(x,name,0)}.filter(_.isDefined).map(_.get)

    if(news.nonEmpty){
      val activeNews: List[String] = prospect.infobaeNews.map(_.url)
      val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

      if(difference.nonEmpty) {
        difference.map(newsInfobaeService.save)

        val format : SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val now : Date = Calendar.getInstance().getTime

        val all : List[News] = prospect.infobaeNews ::: difference
        prospectService.update(prospect.copy(infobaeNews = all, updatedAt = format.format(now)))
      }
    }
  }
}
