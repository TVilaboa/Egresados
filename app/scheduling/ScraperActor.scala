package scheduling

import javax.inject._

import akka.actor.Actor
import com.google.inject.Inject
import generators._
import models.Graduate
import scrapers._
import services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionException}

/**
  * Created by franco on 01/03/17.
  */

trait Message
object Scrap extends Message

@Singleton
class ScraperActor @Inject() (graduateService: GraduateService,
                              clarinNewsService: ClarinNewsService,
                              infobaeNewsService: InfobaeNewsService,
                              elCronistaNewsService: ElCronistaNewsService,
                              laNacionNewsService: LaNacionNewsService
                             ) (implicit ec : ExecutionException) extends Actor{

  override def receive : Receive = {
    case Scrap =>
      scrapAll()
  }

  private def scrapAll() : Unit = {
    val all : Seq[Graduate] = Await.result(graduateService.all(),Duration.Inf)

    all.foreach{graduate =>
      val fullName : Option[String]= Option(graduate.firstName + " " + graduate.lastName)
      val query : Option[String] = Option("Universidad Austral")

      val laNacionLinks = LaNacionUrlGeneratorObject.search(fullName, query)
      val laNacionNews = laNacionLinks.map{x : String =>
        val scraper = new LaNacionScraper()
        scraper.getArticleData(x,fullName,0)
      }.filter(_.isDefined)
      laNacionNews.foreach(x=> laNacionNewsService.save(x.get))

      val clarinLinks = ClarinUrlGeneratorObject.search(fullName, query)
      val clarinNews = clarinLinks.map{x : String =>
        val scraper = new ClarinScraper()
        scraper.getArticleData(x,fullName,0)
      }.filter(_.isDefined)
      clarinNews.foreach(x=> clarinNewsService.save(x.get))

      val infobaeLinks = InfobaeUrlGeneratorObject.search(fullName, query)
      val infobaeNews = infobaeLinks.map{x : String =>
        val scraper = new InfobaeScraper()
        scraper.getArticleData(x,fullName,0)
      }.filter(_.isDefined)
      infobaeNews.foreach(x=> infobaeNewsService.save(x.get))

      val elCronistaLinks = ElCronistaUrlGeneratorObject.search(fullName, query)
      val elCronistaNews = elCronistaLinks.map{x : String =>
        val scraper = new ElCronistaScraper()
        scraper.getArticleData(x,fullName,0)
      }.filter(_.isDefined)
      elCronistaNews.foreach(x=> elCronistaNewsService.save(x.get))

      val linkedInLinks = LinkedInUrlGeneratorObject.search(fullName,query)
      val linkedInUserProfile = linkedInLinks.map{x : String=>
      val scraper = new LinkedinUserProfileScraper()
        scraper.getLinkedinProfile(x,0)
      }.filter(_.isDefined) match{
        case Nil => Option(graduate.linkedinUserProfile)
        case x :: xs => x
      }

      val update = graduate.copy(linkedinUserProfile = linkedInUserProfile.get,
        laNacionNews = laNacionNews.map(_.get),
        clarinNews = clarinNews.map(_.get),
        infobaeNews = infobaeNews.map(_.get),
        elCronistaNews = elCronistaNews.map(_.get)
      )

      Await.result(graduateService.update(update),Duration.Inf)

    }

  }


}
