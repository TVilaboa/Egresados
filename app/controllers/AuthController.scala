package controllers

import java.io.IOException
import java.util.UUID

import actions.SecureAction
import com.github.t3hnar.bcrypt._
import com.google.inject.Inject
import com.mongodb.MongoWriteException
import forms.AuthForms.{LoginData, SignupData}
import models.{Session, User}
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import services.{SessionService, UserService}

import scala.concurrent.Future

/**
  * Created by ismet on 13/12/15.
  */
class AuthController @Inject()(userService: UserService,
                               sessionService: SessionService,
                               secureAction: SecureAction,
                               val messagesApi: MessagesApi) extends Controller {

  //TODO refactor del signup y de su View
  def signup = Action.async { implicit request =>
    val body : JsValue = request.body.asJson.get
    val rawBody: JsValue = request.body.asJson.get
    try {
      val signUpData: SignupData = body.validate[SignupData].get
      val user = User(UUID.randomUUID().toString,
                      signUpData.name,
                      signUpData.email,
                      signUpData.username,
                      signUpData.password.bcrypt,
                      System.currentTimeMillis())

      userService.save(user).map((_) => {Ok}).recoverWith {
        case e: MongoWriteException => Future {Forbidden}
        case e => Future {Forbidden}
      }
    }
    catch {
      case e: IOException => Future {BadRequest}
    }
  }

  def login = Action.async { implicit request =>
    val body= request.body.asJson.get
    try {
      val credentials = body.validate[LoginData].get

      userService.findByUsername(credentials.username).map((user: User) => {
        if (credentials.password.isBcrypted(user.password)) {
          val sessionId: String = UUID.randomUUID().toString
          val currentTimeMillis: Long = System.currentTimeMillis()
          val session: Session = models.Session(sessionId,
                                                user._id,
                                                request.remoteAddress,
                                                request.headers.get("User-Agent").get,
                                                currentTimeMillis,
                                                currentTimeMillis)
          sessionService.save(session)
          val response = Map("sessionId" -> sessionId)
          Ok(Json.toJson(response)).withCookies(Cookie("sessionId", sessionId))
        }
        else Unauthorized
      }).recoverWith {
        case e: IllegalStateException => Future {Forbidden}
      }
    }
    catch {
      case e: IOException => Future {BadRequest}
    }
  }

  def logout = secureAction { implicit request =>
    sessionService.delete(request.sessionId)
    Ok.discardingCookies(DiscardingCookie("sessionId"))
  }
}
