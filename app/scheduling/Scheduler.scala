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
class Scheduler @Inject()(val system: ActorSystem,
                          @Named("scraper-actor") val schedulerActor: ActorRef,
                          config : Configuration)(implicit ec: ExecutionContext){

  val now : DateTime = new DateTime(DateTimeZone.UTC).toDateTimeISO

  val next: DateTime = now.plusMinutes(10).toDateTimeISO
  val interval : Period = new Interval(now,next).toPeriod()
  val initialDelay : Int = interval.getMinutes * 60 + interval.getSeconds

  val definedInterval : String = config.getString("scheduling.interval") match{
    case Some(x) => x
    case None => "day"
  }

  val hourToRun : Int = config.getInt("scheduling.time") match{
    case Some(x) => x
    case None => 2
  }

  definedInterval match{
    case "hour" =>
      system.scheduler.schedule(initialDelay.seconds, 1.hour, schedulerActor, ScrapAll)

    case "day" =>
      system.scheduler.schedule(initialDelay.seconds,1.day,schedulerActor, ScrapAll)

    case "week" =>
      system.scheduler.schedule(initialDelay.seconds,7.day,schedulerActor, ScrapAll)

    case "month" =>
      system.scheduler.schedule(initialDelay.seconds,30.day,schedulerActor, ScrapAll)

  }
}


