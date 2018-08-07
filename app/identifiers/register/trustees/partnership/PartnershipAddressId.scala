package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.address.Address
import play.api.libs.json.JsPath

case class PartnershipAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipAddressId.toString
}

object PartnershipAddressId {
  override def toString: String = "partnershipAddress"
}
