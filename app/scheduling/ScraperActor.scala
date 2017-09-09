package scheduling

import javax.inject._

import akka.actor.Actor
import com.google.inject.Inject
import models.Prospect
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
class ScraperActor @Inject()(prospectService: ProspectService,
                             linkedinScraper : LinkedinUserProfileScraper,
                             linkedinUserProfileService: LinkedinUserProfileService,
                             infobaeNewsService: InfobaeNewsService,
                             infobaeScraper: InfobaeScraper,
                             clarinNewsService: ClarinNewsService,
                             clarinScraper: ClarinScraper,
                             elCronistaNewsService: ElCronistaNewsService,
                             elCronistaScraper: ElCronistaScraper,
                             laNacionNewsService: LaNacionNewsService,
                             laNacionScraper: LaNacionScraper,
                             scrappingService: ScrapingService)(implicit ee: ExecutionException, executionContext: ExecutionContext) extends Actor {



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
        scrappingService.runLinkedinSearch(prospectService, linkedinScraper, linkedinUserProfileService, p)
      }
    }
  }

  private def scrapInfobae(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        scrappingService.runInfobaeSearch(infobaeScraper, infobaeNewsService, prospectService, p)
      }
    }
  }

  private def scrapLaNacion(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        scrappingService.runLaNacionSearch(laNacionScraper, laNacionNewsService, prospectService, p)
      }
    }
  }

  private def scrapElCronista(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        scrappingService.runElCronistaSearch(elCronistaScraper, elCronistaNewsService, prospectService, p)
      }
    }
  }

  private def scrapClarin(prospects : Future[Seq[Prospect]]) : Unit = {
    prospects.map{x=>
      x.foreach{p =>
        scrappingService.runClarinSearch(clarinScraper, clarinNewsService, prospectService, p)
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
