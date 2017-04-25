package controllers

import com.google.inject.Inject
import generators._
import play.api.i18n.MessagesApi
import play.api.mvc._
import actions.SecureAction
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import scala.io.Source

class Application @Inject()(secureAction: SecureAction) extends Controller {


  def index = {
    homeFeed
  }

  def tables = secureAction {
    Ok(views.html.tables.render())
  }

  def login = secureAction {
    Ok(views.html.login.render(null,"",null))
  }

  /**
    * Home Feed showing last scraping log result data for user
    * */
  def homeFeed = secureAction {
    val errorLines = Source.fromFile("logs/error.log").getLines.toList
    val successLines = Source.fromFile("logs/success.log").getLines.toList

    val error : List[String] = Source.fromFile("logs/error.log").getLines().toList
    val success : List[String] = Source.fromFile("logs/success.log").getLines().toList

    val dtf : DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss")

    val errorTuple : List[(DateTime,String,String,String)] = error.map{x =>
      //Info is made up from either
      //News Scraps => Date - [ERROR] - Class :-: Message :-: Url
      //LinkedIn Scraps => Date - [ERROR] - Class :-: Url :-: Message
      val info : Array[String] = x.split(":-:")
      val basicData : Array[String]= info(0).split("-")

      val scraper : String = basicData.last
      val message : String = scraper match{
        case "LinkedIn Scraper" => info.last
        case _ => info(1)
      }
      val url : String = scraper match{
        case "LinkedIn Scraper" => info(1)
        case _ => info.last
      }
      val date : DateTime = dtf.parseDateTime(basicData.head.trim).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
      (date,scraper,message,url)
    }.groupBy(_._1).toList.sortBy(_._1.getMillis).last._2

    val successTuple : List[(DateTime,String,String,String)] = success.map{x =>
      //Info is made up from either
      //News Scraps => Date - [SUCCESS] - Class :-: Message :-: Url
      //LinkedIn Scraps => Date - [SUCCESS] - Class :-: Url
      val info : Array[String] = x.split(":-:")
      val basicData : Array[String]= info(0).split("-")

      val scraper : String = basicData.last
      val message : String = scraper match{
        case "LinkedIn Scraper" => ""
        case _ => info(1)
      }
      val url : String = info.last

      val date : DateTime = dtf.parseDateTime(basicData.head.trim).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
      (date,scraper,message,url)
    }.groupBy(_._1).toList.sortBy(_._1.getMillis).last._2

    val errorDate : DateTime = errorTuple.head._1
    val successDate : DateTime = successTuple.head._1

    if(errorDate.isEqual(successDate))
      Ok(views.html.index.render("", errorDate, successTuple, errorTuple))
    else if(errorDate.isAfter(successDate))
      Ok(views.html.index.render("", errorDate, successTuple.filter(_._1.equals(errorDate)), errorTuple))
    else
      Ok(views.html.index.render("", successDate, successTuple, errorTuple.filter(_._1.equals(successDate))))

  }
}