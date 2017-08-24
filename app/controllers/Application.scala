package controllers


import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.google.inject.Inject
import play.api.mvc._
import actions.SecureAction
import models.Prospect
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import services.ProspectService

import scala.concurrent.Future

class Application @Inject()(secureAction: SecureAction, prospectService: ProspectService) extends Controller {

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

      val errorDate : Date = partition._1 match{
        case seq : Seq[Prospect] =>
          val dates : Seq[Date]= seq.filter(_.errorDate.nonEmpty).map(x=> format.parse(x.errorDate)).sortBy(_.getTime)
          if(dates.nonEmpty) dates.head
          else new Date()
        case Nil => new Date()
      }

      val updateDate : Date = x match{
        case seq : Seq[Prospect] =>
          val dates : Seq[Date]= seq.filter(_.updatedAt.nonEmpty).map(x=> format.parse(x.updatedAt)).sortBy(_.getTime)
          if(dates.nonEmpty) dates.head
          else new Date()
        case Nil => new Date()
      }

      val errors : Seq[Prospect] = partition._1
      val updates : Seq[Prospect] = x.filter(e => e.updatedAt.equals(format.format(updateDate)))

      Ok(com.home.views.html.index.render(errors,updates,format.format(errorDate),format.format(updateDate)))

    }
//
//    val successLogs : List[String] = Source.fromFile("logs/success.log").getLines().toList
//    val successTuples : List[(DateTime,String,String,String)] = successLogs.map(parseLogEntry)
//
//    val errorLogs : List[String] = Source.fromFile("logs/error.log").getLines().toList
//    val errorTuples : List[(DateTime,String,String,String)] = errorLogs.map(parseLogEntry)
//    var lastError : DateTime =  new DateTime( 0x0, 1, 1, 0, 0, 0, DateTimeZone.UTC )
//    if(errorTuples.nonEmpty){
//      lastError = errorTuples.minBy(_._1.toDate.getTime)._1
//    }
//
//    var lastSuccess : DateTime =  new DateTime( 0x0, 1, 1, 0, 0, 0, DateTimeZone.UTC )
//
//
//    if(errorTuples.nonEmpty){
//      lastSuccess = successTuples.minBy(_._1.toDate.getTime)._1
//    }
//
//
//
//    if(lastError.isAfter(lastSuccess))
//      Ok(views.html.index.render(lastError,
//              successTuples.filter(_._1.equals(lastError)),
//              errorTuples.filter(_._1.equals(lastError))))
//    else
//      Ok(views.html.index.render(lastSuccess,
//        successTuples.filter(_._1.equals(lastSuccess)),
//        errorTuples.filter(_._1.equals(lastSuccess))))
  }
}