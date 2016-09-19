package controllers

import java.util.UUID

import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.AuthForms.{LoginData, SignupData}
import models.{Session, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, JsValue}
import play.api.mvc._
import views.html
import services.{SessionService, UserService}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class UserAuthController @Inject()(userService: UserService,
                                   sessionService: SessionService) extends Controller {

  val loginForm = Form(
    mapping = tuple(
      "user" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (username, password) => check(username, password)
    })
  )

  def checkPassword(username: String, password: String): Future[Boolean] = {
    userService.findByUsername(username).map(User => {
      if (User.password.equals(password)) true
      else false
    })
  }

  def check(username: String, password: String): Boolean = {
    if (userService.findByUsername(username) == null) return false
    val b: Boolean = Await.result(checkPassword(username, password), scala.concurrent.duration.Duration(5, "seconds"))
    b
  }

  def login = Action.async { implicit request =>
      userService.findByUsername(request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("user").get(0)).map((user: User) => {
          val sessionId: String = UUID.randomUUID().toString
          val currentTimeMillis: Long = System.currentTimeMillis()
          val session: Session = models.Session(
            sessionId,
            user._id,
            request.remoteAddress,
            request.headers.get("User-Agent").get,
            currentTimeMillis,
            currentTimeMillis
          )
          sessionService.save(session)
          val response = Map("sessionId" -> sessionId)
          Ok(Json.toJson(response)).withCookies(Cookie("sessionId", sessionId))
      }).recoverWith {
        case e: IllegalStateException => Future {
          Forbidden
        }
      }
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

  val userForm = Form(
    mapping = tuple(
      "name" -> text,
      "email" -> text,
      "username" -> text,
      "password" -> text
    )
  )

  def signupView = Action {
//    userService.create(userForm.get._1, userForm.get._2, userForm.get._3, userForm.get._4, 0)
    Ok(html.sign_up(userForm))
  }
  def signup = Action.async { implicit request =>
    try {
      val user = User(
        UUID.randomUUID().toString,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("name").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("email").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("username").get(0),
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data.get("password").get(0),
        System.currentTimeMillis())

      userService.save(user).map((_) => {
        Ok
      }).recoverWith {
        case e: MongoWriteException => Future {

          Forbidden
        }
        case e => Future {
          Forbidden
        }
      }
    } catch {
      case e: Exception => Future {
        //Ok(e.toString)
        BadRequest
      }
    }}
}