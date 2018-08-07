package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.address.TolerantAddress
import play.api.libs.json.JsPath

case class PartnershipAddressListId(index: Int) extends TypedIdentifier[TolerantAddress] {
  override def path: JsPath = TrusteesId(index).path \ PartnershipAddressListId.toString
}

object PartnershipAddressListId {
  override def toString: String = "partnershipAddressList"
}
