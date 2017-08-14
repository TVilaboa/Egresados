package enums

import play.api.libs.json.{Reads, Writes}


//Deberia pasar a la base para editarlos desde ahi si es necesario

object InstitutionType extends Enumeration {
  type InstitutionType = Value

  val Academical, Company = Value

  implicit val enumReads: Reads[InstitutionType] = EnumPlayUtils.enumReads(InstitutionType)

  implicit def enumWrites: Writes[InstitutionType] = EnumPlayUtils.enumWrites
}


