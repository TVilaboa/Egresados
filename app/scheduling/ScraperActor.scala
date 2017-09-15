package scheduling

import java.util.Date
import javax.inject._

import akka.actor.Actor
import com.google.inject.Inject
import models.Prospect
import scrapers._
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionException, Future}
import scala.util.Try

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
                             laNacionScraper: LaNacionScraper)(implicit ee: ExecutionException, executionContext: ExecutionContext) extends Actor {





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

  private def scrapLinkedIn(prospects: Future[Seq[Prospect]]): Int = {
    var linksSize = 0
    val result = prospects.map { x =>
      ScrapingService.addTotal(x.size)
      x.foreach { p =>
        linksSize += ScrapingService.runLinkedinSearch(prospectService, linkedinScraper, linkedinUserProfileService, p)
        ScrapingService.addCounter()
        println(ScrapingService.getCounter + "/" + ScrapingService.getTotal + " generated urls...")
      }
    }
    Await.result(result, Duration.Inf)
    return linksSize
  }

  private def scrapInfobae(prospects: Future[Seq[Prospect]]): Int = {
    var linksSize = 0
    val result = prospects.map { x =>
      ScrapingService.addTotal(x.size)
      x.foreach{p =>
        linksSize += ScrapingService.runInfobaeSearch(infobaeScraper, infobaeNewsService, prospectService, p)
        ScrapingService.addCounter()
        println(ScrapingService.getCounter + "/" + ScrapingService.getTotal + " generated urls...")
      }
    }
    Await.result(result, Duration.Inf)
    return linksSize
  }

  private def scrapLaNacion(prospects: Future[Seq[Prospect]]): Int = {
    var linksSize = 0
    val result = prospects.map { x =>
      ScrapingService.addTotal(x.size)
      x.foreach{p =>
        linksSize += ScrapingService.runLaNacionSearch(laNacionScraper, laNacionNewsService, prospectService, p)
        ScrapingService.addCounter()
        println(ScrapingService.getCounter + "/" + ScrapingService.getTotal + " generated urls...")
      }
    }
    Await.result(result, Duration.Inf)
    return linksSize
  }

  private def scrapElCronista(prospects: Future[Seq[Prospect]]): Int = {
    var linksSize = 0
    val result = prospects.map { x =>
      ScrapingService.addTotal(x.size)
      x.foreach{p =>
        linksSize += ScrapingService.runElCronistaSearch(elCronistaScraper, elCronistaNewsService, prospectService, p)
        ScrapingService.addCounter()
        println(ScrapingService.getCounter + "/" + ScrapingService.getTotal + " generated urls...")
      }
    }
    Await.result(result, Duration.Inf)
    return linksSize
  }

  private def scrapClarin(prospects: Future[Seq[Prospect]]): Int = {
    var linksSize = 0
    val result = prospects.map { x =>
      ScrapingService.addTotal(x.size)
      x.foreach{p =>
        linksSize += ScrapingService.runClarinSearch(clarinScraper, clarinNewsService, prospectService, p)
        ScrapingService.addCounter()
        println(ScrapingService.getCounter + "/" + ScrapingService.getTotal + " generated urls...")
      }
    }

    return linksSize
  }

  private def scrapAll() : Unit = {
    ScrapingService.setCounter(0)
    ScrapingService.setTotal(0)
    ScrapingService.isAutoScrappingRunning = true
    ScrapingService.lastStartDate = new Date()
    ScrapingService.linksGenerated = 0
    val prospects : Future[Seq[Prospect]]= prospectService.all()

    val linkedinFuture = Future[Int] {
      scrapLinkedIn(prospects)
    }
    val infobaeFuture = Future[Int] {
      scrapInfobae(prospects)
    }
    val laNacionFuture = Future[Int] {
      scrapLaNacion(prospects)
    }
    val elCronistaFuture = Future[Int] {
      scrapElCronista(prospects)
    }
    val clarinFuture = Future[Int] {
      scrapClarin(prospects)
    }

    val aggregatedFuture = for {
      linkedin <- linkedinFuture
      infobae <- infobaeFuture
      laNacion <- laNacionFuture
      elCronista <- elCronistaFuture
      clarin <- clarinFuture
    } yield linkedin + infobae + laNacion + elCronista + clarin

    aggregatedFuture.onComplete((linksObtained: Try[Int]) => {
      ScrapingService.isAutoScrappingRunning = false
      ScrapingService.lastFinishDate = new Date()
      ScrapingService.linksGenerated = linksObtained.getOrElse(-1)
    })


  }


}
