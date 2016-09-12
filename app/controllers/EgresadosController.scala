package controllers

import akka.actor.FSM.Failure
import akka.actor.Status.Success
import com.google.inject.Inject
import models.Graduate
import play.api.mvc._
import services.{UserService, GraduateService}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController @Inject()(graduateService: GraduateService) extends Controller {

  /*def search = Action { implicit request => {
    var graduates = Seq[Graduate]()
    val all: Future[Seq[Graduate]] = graduateService.all()
    all onSuccess  {
      case results: Seq[Graduate] => {
        println("Success")
        results.foreach { result => {

          graduates = graduates :+ result

        } }

      }

    }
    all onFailure {
      case _ => {
        println("Error")

      }
    }
    Await.ready(all,Duration.Inf)

    Ok(views.html.search.render(graduates, false))
  }
  }

  def doSearch = Action { implicit request => {
    val graduates = Seq[Graduate]()
    graduateService.all().foreach(g => graduates ++ g)
    Ok(views.html.search.render(graduates, true))
  }
  }*/

}
