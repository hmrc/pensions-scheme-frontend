package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import play.api.libs.json.JsPath
import identifiers.register.establishers.EstablishersId
import models.UniqueTaxReference

case class PartnershipUniqueTaxReferenceID (index: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(index).path \ PartnershipUniqueTaxReferenceID.toString
}

object PartnershipUniqueTaxReferenceID {
  override def toString: String = "partnershipUniqueTaxReference"
}