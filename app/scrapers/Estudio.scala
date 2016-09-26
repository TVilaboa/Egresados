package scrapers

/*
  * Elaborado por Brian Re y Michele Re
 */

case class Estudio(instituto: String, urlInstituto: String, titulo: String, periodoEstudio: String, descripcion: String){
  override def equals(obj: scala.Any): Boolean = {
    val estudio = obj.asInstanceOf[Estudio]
    estudio.instituto == this.instituto && estudio.urlInstituto == this.urlInstituto &&
      estudio.titulo == this.titulo &&
      estudio.periodoEstudio == this.periodoEstudio &&
      estudio.descripcion == this.descripcion
  }
}
