package scheduling

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import javax.inject._

import akka.actor.Actor
import com.google.inject.Inject
import generators._
import models.{LinkedinUserProfile, News, Prospect}
import scrapers._
import services._

import scala.concurrent.{ExecutionContext, ExecutionException, Future}

/**
  * Created by franco on 01/03/17.
  */

trait Message

object ScrapAll extends Message
object ScrapLinkedIn extends Message
object ScrapInfobae extends Message
object ScrapLaNacion extends Message
object ScrapElCronista extends Message
object ScrapClarin extends Message

@Singleton
class ScraperActor @Inject() (prospectService: ProspectService,
                              linkedinScraper : LinkedinUserProfileScraper,
                              linkedinUserProfileService: LinkedinUserProfileService,
                              infobaeNewsService: InfobaeNewsService,
                              infobaeScraper: InfobaeScraper,
                              clarinNewsService: ClarinNewsService,
                              clarinScraper: ClarinScraper,
                              elCronistaNewsService: ElCronistaNewsService,
                              elCronistaScraper: ElCronistaScraper,
                              laNacionNewsService: LaNacionNewsService,
                              laNacionScraper: LaNacionScraper) (implicit ee : ExecutionException, executionContext: ExecutionContext)  extends Actor{

  final val format : SimpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  override def receive : Receive = {
    case ScrapAll =>
      scrapAll()
    case ScrapLinkedIn =>
      scrapLinkedIn(prospectService.all())
    case ScrapInfobae =>
      scrapInfobae(prospectService.all())
    case ScrapLaNacion =>
      scrapLaNacion(prospectService.all())
    case ScrapElCronista =>
      scrapElCronista(prospectService.all())
    case ScrapClarin =>
      scrapClarin(prospectService.all())
  }

  private def scrapLinkedIn(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        val links : List[String] = LinkedInUrlGeneratorObject.search(Option(p.getFullName),Option(p.institution.name))
        val profiles : List[LinkedinUserProfile] =  links.map(linkedinScraper.getLinkedinProfile(_,0)).filter(_.isDefined).map(_.get)

        if(profiles.nonEmpty){
          val profile: LinkedinUserProfile = profiles.head

          val now : Date = Calendar.getInstance().getTime

          if(profile.actualPosition.nonEmpty)
            linkedinUserProfileService.save(profile).map(x=> prospectService.update(p.copy(linkedInProfile = profile, updatedAt = format.format(now))))
          else
            linkedinUserProfileService.save(profile).map(x=> prospectService.update(p.copy(linkedInProfile = profile, errorDate = format.format(now))))
        }
      }
    }
  }

  private def scrapInfobae(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        val links : List[String] = InfobaeUrlGeneratorObject.search(Option(p.getFullName),Option(p.institution.name))

        val news : Seq[News] = links.map{x=> infobaeScraper.getArticleData(x,Option(p.getFullName),0)}.filter(_.isDefined).map(_.get)

        if(news.nonEmpty){
          val activeNews: List[String] = p.infobaeNews.map(_.url)
          val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

          if(difference.nonEmpty) {
            difference.map(infobaeNewsService.save)

            val now : Date = Calendar.getInstance().getTime

            val all : List[News] = p.infobaeNews ::: difference
            prospectService.update(p.copy(infobaeNews = all, updatedAt = format.format(now)))
          }
        }
      }
    }
  }

  private def scrapLaNacion(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        val links : List[String] = LaNacionUrlGeneratorObject.search(Option(p.getFullName),Option(p.institution.name))

        val news : Seq[News] = links.map{x=> laNacionScraper.getArticleData(x,Option(p.getFullName),0)}.filter(_.isDefined).map(_.get)

        if(news.nonEmpty){
          val activeNews: List[String] = p.nacionNews.map(_.url)
          val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

          if(difference.nonEmpty) {
            difference.map(laNacionNewsService.save)

            val now : Date = Calendar.getInstance().getTime

            val all : List[News] = p.nacionNews ::: difference
            prospectService.update(p.copy(nacionNews = all, updatedAt = format.format(now)))
          }
        }
      }
    }
  }

  private def scrapElCronista(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        val links : List[String] = ElCronistaUrlGeneratorObject.search(Option(p.getFullName),Option(p.institution.name))

        val news : Seq[News] = links.map{x=> elCronistaScraper.getArticleData(x,Option(p.getFullName),0)}.filter(_.isDefined).map(_.get)

        if(news.nonEmpty){
          val activeNews: List[String] = p.cronistaNews.map(_.url)
          val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

          if(difference.nonEmpty) {
            difference.map(elCronistaNewsService.save)

            val now : Date = Calendar.getInstance().getTime

            val all : List[News] = p.cronistaNews ::: difference
            prospectService.update(p.copy(cronistaNews = all, updatedAt = format.format(now)))
          }
        }
      }
    }
  }

  private def scrapClarin(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        val links : List[String] = ClarinUrlGeneratorObject.search(Option(p.getFullName),Option(p.institution.name))

        val news : Seq[News] = links.map{x=> clarinScraper.getArticleData(x,Option(p.getFullName),0)}.filter(_.isDefined).map(_.get)

        if(news.nonEmpty){
          val activeNews: List[String] = p.clarinNews.map(_.url)
          val difference : List[News] = news.filter(n=> !activeNews.contains(n.url)).toList

          if(difference.nonEmpty) {
            difference.map(clarinNewsService.save)

            val now : Date = Calendar.getInstance().getTime

            val all : List[News] = p.clarinNews ::: difference
            prospectService.update(p.copy(clarinNews = all, updatedAt = format.format(now)))
          }
        }
      }
    }
  }

  private def scrapAll() : Unit = {
    val prospects : Future[Seq[Prospect]]= prospectService.all()

    scrapLinkedIn(prospects)
    scrapInfobae(prospects)
    scrapLaNacion(prospects)
    scrapElCronista(prospects)
    scrapClarin(prospects)
  }


}
