package controllers

import models.Graduate
import play.api.mvc._

/**
  * Created by Ignacio Vazquez on 28/08/2016.
  */
class EgresadosController extends Controller {

  def search = Action {
    val gr : List[Graduate] = List()
    Ok(views.html.search.render(gr,false))
  }

  def doSearch = Action {
    val a = Graduate("1","Franco","Testori","12345678","01/01/94","2011","2016","Informatic Engineering")
    val b = Graduate("2","Florencia","Velarde","12345679","02/01/94","2011","2016","Informatic Engineering")

    val graduates = List[Graduate](a,b)
    Ok(views.html.search.render(graduates,true))
  }

}
