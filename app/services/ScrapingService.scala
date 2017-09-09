package services

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.google.inject.Inject
import generators._
import models.{LinkedinUserProfile, News, Prospect}
import scrapers._

import scala.concurrent.{ExecutionContext, ExecutionException, Future}


class ScrapingService @Inject()(implicit ee: ExecutionException, executionContext: ExecutionContext) {
  //Clase para unificar logicas de scrapeado ya sea se llame desde controler o desde actor
  final val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")


  def runLinkedinSearch(prospectService: ProspectService, scraper: LinkedinUserProfileScraper, linkedinUserProfileService: LinkedinUserProfileService, prospect: Prospect): Unit = {
    try {
      val links: List[String] = LinkedInUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

      val profiles: List[LinkedinUserProfile] = links.map(x => scraper.getLinkedinProfile(x, 0)).filter(_.isDefined).map(_.get)

      val now: Date = Calendar.getInstance().getTime

      if (profiles.nonEmpty) {
        var updatedProfiles: List[LinkedinUserProfile] = List[LinkedinUserProfile]()
        var matchedValidOrAddNew = false
        for (profile <- profiles) {
          var matchedProfile = prospect.linkedInProfiles.find(p => p.profileUrl == profile.profileUrl)
          if (matchedProfile.isDefined) {
            val updatedProfile = profile.copy(_id = matchedProfile.get._id, rejected = matchedProfile.get.rejected, validated = matchedProfile.get.validated)
            linkedinUserProfileService.update(updatedProfile)
            updatedProfiles = updatedProfile :: updatedProfiles
            if (!matchedValidOrAddNew) {
              matchedValidOrAddNew = !matchedProfile.get.rejected
            }

          } else {
            linkedinUserProfileService.save(profile)
            updatedProfiles = profile :: updatedProfiles
            matchedValidOrAddNew = true
          }
        }


        if (matchedValidOrAddNew) {
          prospectService.update(prospect.copy(linkedInProfiles = updatedProfiles, updatedAt = format.format(now), errorDate = null))
        }
        else {
          prospectService.update(prospect.copy(errorDate = format.format(now)))
        }


      } else {
        prospectService.update(prospect.copy(errorDate = format.format(now)))
      }
    } catch {

      case e: Exception =>

        None
    }
  }

  def runClarinSearch(clarinScraper: ClarinScraper, clarinNewsService: ClarinNewsService, prospectService: ProspectService, prospect: Prospect): Unit = {
    val links: List[String] = ClarinUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val news: Seq[News] = links.map { x => clarinScraper.getArticleData(x, Option(prospect.getFullName), 0) }.filter(_.isDefined).map(_.get)

    if (news.nonEmpty) {
      val activeNews: List[String] = prospect.clarinNews.map(_.url)
      val difference: List[News] = news.filter(n => !activeNews.contains(n.url)).toList

      if (difference.nonEmpty) {
        Future.sequence(difference.map(clarinNewsService.save)).map { completion =>
          val now: Date = Calendar.getInstance().getTime

          val all: List[News] = prospect.clarinNews ::: difference
          prospectService.update(prospect.copy(clarinNews = all, updatedAt = format.format(now)))
        }
      }
    }
  }

  def runElCronistaSearch(elCronistaScraper: ElCronistaScraper, elCronistaNewsService: ElCronistaNewsService, prospectService: ProspectService, prospect: Prospect): Unit = {
    val links: List[String] = ElCronistaUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val news: Seq[News] = links.map { x => elCronistaScraper.getArticleData(x, Option(prospect.getFullName), 0) }.filter(_.isDefined).map(_.get)

    if (news.nonEmpty) {
      val activeNews: List[String] = prospect.cronistaNews.map(_.url)
      val difference: List[News] = news.filter(n => !activeNews.contains(n.url)).toList

      if (difference.nonEmpty) {
        Future.sequence(difference.map(elCronistaNewsService.save)).map { completion =>
          val now: Date = Calendar.getInstance().getTime

          val all: List[News] = prospect.cronistaNews ::: difference
          prospectService.update(prospect.copy(cronistaNews = all, updatedAt = format.format(now)))
        }
      }
    }
  }

  def runLaNacionSearch(laNacionScraper: LaNacionScraper, laNacionNewsService: LaNacionNewsService, prospectService: ProspectService, prospect: Prospect): Unit = {
    val links: List[String] = LaNacionUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val news: Seq[News] = links.map { x => laNacionScraper.getArticleData(x, Option(prospect.getFullName), 0) }.filter(_.isDefined).map(_.get)

    if (news.nonEmpty) {
      val activeNews: List[String] = prospect.nacionNews.map(_.url)
      val difference: List[News] = news.filter(n => !activeNews.contains(n.url)).toList

      if (difference.nonEmpty) {
        Future.sequence(difference.map(laNacionNewsService.save)).map { completion =>
          val now: Date = Calendar.getInstance().getTime

          val all: List[News] = prospect.nacionNews ::: difference
          prospectService.update(prospect.copy(nacionNews = all, updatedAt = format.format(now)))
        }
      }
    }
  }

  def runInfobaeSearch(infobaeScraper: InfobaeScraper, infobaeNewsService: InfobaeNewsService, prospectService: ProspectService, prospect: Prospect): Unit = {
    val links: List[String] = InfobaeUrlGeneratorObject.search(Option(prospect.getFullName), Option(prospect.institution.name))

    val news: Seq[News] = links.map { x => infobaeScraper.getArticleData(x, Option(prospect.getFullName), 0) }.filter(_.isDefined).map(_.get)

    if (news.nonEmpty) {
      val activeNews: List[String] = prospect.infobaeNews.map(_.url)
      val difference: List[News] = news.filter(n => !activeNews.contains(n.url)).toList

      if (difference.nonEmpty) {
        Future.sequence(difference.map(infobaeNewsService.save)).map { completion =>
          val now: Date = Calendar.getInstance().getTime

          val all: List[News] = prospect.infobaeNews ::: difference
          prospectService.update(prospect.copy(infobaeNews = all, updatedAt = format.format(now)))
        }
      }
    }
  }
}
