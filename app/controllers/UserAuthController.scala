package controllers

import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

import actions.SecureAction
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.AuthForms.{LoginData, SignupData}
import models.{Session, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import views.html
import services.{SessionService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import com.github.t3hnar.bcrypt._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class UserAuthController @Inject()(userService: UserService,
                                   sessionService: SessionService,
                                   secureAction: SecureAction) extends Controller {

  val loginForm = Form(mapping = tuple("user" -> text, "password" -> text)
    verifying("Invalid email or password",
              result => result match {case (username, password) => check(username, password)}))

  def checkPassword(username: String, password: String): Future[Boolean] = {
    userService.findByUsername(username).map(User => {
      if (User.password.equals(password)) true
      else false
    })
  }

  def check(username: String, password: String): Boolean = {
    if (userService.findByUsername(username) == null) return false
    val b: Boolean = Await.result(checkPassword(username, password), Duration.create(5, TimeUnit.SECONDS))
    b
  }

  def login =secureAction.async { implicit request =>
    try{
      userService.findByUsername(loginForm.bindFromRequest().data("user")).map((user: User) => {
        val insertedPassword =loginForm.bindFromRequest().data("password")
        if(insertedPassword.isBcrypted(user.password)){
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
//          Ok(Json.toJson(response)).withCookies(Cookie("sessionId", sessionId))
//          Redirect("/").withCookies(Cookie("sessionId", sessionId))

          Redirect("/").withCookies(Cookie("sessionId", sessionId))
        }else
          Unauthorized(views.html.login.render(null,"Invalid Password" ,null))

      }).recoverWith {
        case e: IllegalStateException => Future {
          Unauthorized(views.html.login.render(null,"Invalid Username" ,null))
        }
      }
    }
    catch {
      case e: Exception => Future {
        Unauthorized(views.html.login.render(null,"Invalid Username" ,null))
      }
    }
  }

  def logout = secureAction { implicit request =>
    sessionService.delete(request.sessionId)

    Unauthorized(views.html.login.render(null,"" ,null)).withNewSession.discardingCookies(DiscardingCookie("sessionId"))
  }

  val userForm = Form(
    mapping = tuple(
      "name" -> text,
      "email" -> text,
      "username" -> text,
      "password" -> text
    )
  )

  def signupView =secureAction {
//    userService.create(userForm.get._1, userForm.get._2, userForm.get._3, userForm.get._4, 0)
    Ok(html.sign_up(userForm))
  }
  def signup =secureAction.async { implicit request =>
    try {
      val user = User(
        UUID.randomUUID().toString,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("name").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("email").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("username").head,
        request.body.asInstanceOf[AnyContentAsFormUrlEncoded].data("password").head.bcrypt,
        System.currentTimeMillis())

        userService.save(user).map((_) => {
        Ok(views.html.login.render(null,"", null))
      }).recoverWith {
        case e: MongoWriteException => Future {

          Forbidden
        }
        case e => Future {
          Forbidden
        }
      }
    } catch {
      case e: IOException => Future {
        //Ok(e.toString)
        BadRequest
      }
    }}
}