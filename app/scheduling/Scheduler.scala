package scheduling

import javax.inject._

import akka.actor.{ActorRef, ActorSystem}
import org.joda.time.{DateTime, DateTimeZone, Interval, Period}
import play.api.Configuration

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

/**
  * Created by franco on 27/01/17.w
  */
class Scheduler @Inject()(val system: ActorSystem, @Named("scraper-actor") val schedulerActor: ActorRef, config : Configuration)(implicit ec: ExecutionContext){
  system.scheduler.scheduleOnce(0.microsecond,schedulerActor,Scrap)

  val now : DateTime = new DateTime(DateTimeZone.UTC).toDateTimeISO

  val definedInterval : String = config.getString("scheduling.interval") match{
    case Some(x) => x
    case None => "day"
  }

  val hourToRun : Int = config.getInt("scheduling.time") match{
    case Some(x) => x
    case None => 2
  }

  definedInterval match{
    case "day" =>
      val next : DateTime = now.plusDays(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDateTimeISO

      val interval : Period = new Interval(now,next).toPeriod()
      val timeToTask : Int = interval.getMinutes * 60 + interval.getSeconds

      system.scheduler.schedule(timeToTask.seconds,1.day,schedulerActor,Scrap)

    case "week" =>
      val next : DateTime = now.plusDays(7).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDateTimeISO

      val interval : Period = new Interval(now,next).toPeriod()
      val timeToTask : Int = interval.getMinutes * 60 + interval.getSeconds

      system.scheduler.schedule(timeToTask.seconds,7.day,schedulerActor,Scrap)

    case "month" =>
      val next : DateTime = now.plusDays(30).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDateTimeISO

      val interval : Period = new Interval(now,next).toPeriod()
      val timeToTask : Int = interval.getMinutes * 60 + interval.getSeconds

      system.scheduler.schedule(timeToTask.seconds,30.day,schedulerActor,Scrap)

  }




}


