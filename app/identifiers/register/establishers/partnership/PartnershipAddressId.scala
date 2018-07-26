package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import models.address.Address
import play.api.libs.json.JsPath
import views.html.index

case class PartnershipAddressId(index: Int) extends TypedIdentifier[Address] {
  override def path: JsPath = PartnershipDetailsId(index).path \ PartnershipAddressId.toString
}

object PartnershipAddressId {
  override def toString: String = "partnershipAddress"
}