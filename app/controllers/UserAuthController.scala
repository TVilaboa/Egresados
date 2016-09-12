package controllers

import com.google.inject.Inject
import models.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller, Security}
import views.html
import services.{SessionService, UserService}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

class UserAuthController @Inject()(userService: UserService) extends Controller {

  val loginForm = Form(
    mapping = tuple(
      "user" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match{
      case (username, password) => check(username, password)
    })
  )

  def checkPassword(username: String, password: String): Future[Boolean]= {
    userService.findByUsername(username).map(User => {
      if (User.password.equals(password)) true
      else false
    })
  }

  def check(username: String, password: String): Boolean = {
    if (userService.findByUsername(username)==null) return false
    val b: Boolean = Await.result(checkPassword(username,password), scala.concurrent.duration.Duration(5, "seconds"))
    b
  }

  def login = Action { implicit request =>
    Ok(html.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      user => Redirect(routes.Application.index()).withSession(Security.username -> user._1)
    )
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You are now logged out."
    )
  }
}
