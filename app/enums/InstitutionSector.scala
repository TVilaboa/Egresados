package enums


import play.api.libs.json._

object InstitutionSector extends Enumeration {
  type InstitutionSector = Value

  val Unspecified, Bank, BuildingTrade, Engineering, FinancialInstitutions, Food, Fuel, Power,
  Insurance, ITSector, Miscellaneous, PaperPrinting, Pharmaceuticals, Chemicals,
  Services, RealEstate, Tannery, Telecommunication, Textile, Travel, Leisure, Other = Value


  implicit val enumReads: Reads[InstitutionSector] = EnumPlayUtils.enumReads(InstitutionSector)

  implicit def enumWrites: Writes[InstitutionSector] = EnumPlayUtils.enumWrites
}
