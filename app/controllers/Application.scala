package controllers


import java.text.SimpleDateFormat
import java.util.Date

import actions.SecureAction
import com.google.inject.Inject
import models.Prospect
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import services.ProspectService

import scala.concurrent.Future

class Application @Inject()(secureAction: SecureAction, prospectService: ProspectService,config : Configuration) extends Controller {

  def index = {
    homeFeed
  }

  def tables = secureAction {
    Ok(views.html.tables.render())
  }

  def login = Action {
    Ok(views.html.login.render(null,"",null))
  }

  /**
    * Private method which parses a log entry line into a useful tuple for the Apps index.
    */
  private def parseLogEntry(entry : String) : (DateTime,String,String,String) ={
    val dtf : DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")

    val entryData = entry.split(":-:")

    val data = entryData(0).split("-")

    val date : DateTime = dtf.parseDateTime(data.head.trim)
                          .withHourOfDay(0)
                          .withMinuteOfHour(0)
                          .withSecondOfMinute(0)
                          .withMillisOfSecond(0)
    val scraper : String = data.last
    val message : String = entryData(1)
    val url : String = if(entryData.size > 2) entryData(2) else ""

    (date,scraper,message,url)
  }

  /**
    * Home Feed showing last scraping log result data for user.
    * Shows the last Log entries based on last date of scrap activity for both success and error cases.
    * */
  def homeFeed = secureAction.async {

    val prospects : Future[Seq[Prospect]] = prospectService.all()

    prospects.map{x=>
      val format : SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
      val partition : (Seq[Prospect],Seq[Prospect]) = x.partition(p => p.errorDate.nonEmpty)

      val minDate = new Date(0)
      val errorDate : Date = partition._1 match{
        case seq : Seq[Prospect] =>
          val dates: Seq[Date] = seq.filter(_.errorDate.nonEmpty).map(x => format.parse(x.errorDate))
          if (dates.nonEmpty) dates.maxBy(_.getTime)
          else minDate
        case Nil => minDate
      }

      val updateDate : Date = x match{
        case seq : Seq[Prospect] =>
          val dates: Seq[Date] = seq.filter(_.updatedAt.nonEmpty).map(x => format.parse(x.updatedAt))
          if (dates.nonEmpty) dates.maxBy(_.getTime)
          else minDate
        case Nil => minDate
      }

      val errors : Seq[Prospect] = partition._1
      val updates: Seq[Prospect] = x.filter(e => e.updatedAt.nonEmpty && e.errorDate.isEmpty)

      Ok(com.home.views.html.index.render(errors,updates,format.format(errorDate),format.format(updateDate)))

    }
  }

  def showConfig = secureAction{
    val schedulingInterval : String = config.getString("scheduling.interval").get
    val schedulingHour : String = s"${config.getInt("scheduling.time").get}:00"
    Ok(com.home.views.html.config.render(schedulingInterval,schedulingHour))
  }
}