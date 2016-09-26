package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

case class Empleo(cargo: String, lugarTrabajo: String, urlTrabajo: String, periodoActividad: String, descripcionTrabajo: String) {
  override def equals(obj: Any): Boolean = {
    val empleo = obj.asInstanceOf[Empleo]
    empleo.cargo == this.cargo && empleo.lugarTrabajo == this.lugarTrabajo &&
      empleo.urlTrabajo == this.urlTrabajo &&
      empleo.periodoActividad == this.periodoActividad &&
      empleo.descripcionTrabajo == this.descripcionTrabajo
  }
}