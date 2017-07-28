package controllers

import com.google.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import services.InstitutionService

/**
  * Created by franco on 28/07/17.
  */
class InstitutionController  @Inject()(institutionService: InstitutionService,
                                       val messagesApi: MessagesApi) extends Controller with I18nSupport{

  def index = Action{
    Ok("")
  }

  def create = Action{
    Ok("")
  }

  def store = Action{
    implicit request => {
      Ok("")
    }
  }

  def show(id: String) = Action{
    Ok("")
  }

  def edit(id: String) = Action{
    Ok("")
  }

  def update(id: String) = Action{
    implicit request => {
      Ok("")
    }
  }

  def delete(id: String) = Action{
    implicit request => {
      Ok("")
    }
  }

}
