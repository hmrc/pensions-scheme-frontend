package identifiers.register.establishers.partnership

import identifiers.TypedIdentifier
import models.address.TolerantAddress
import play.api.libs.json.JsPath

case class PartnershipAddressListId(index: Int) extends  TypedIdentifier[TolerantAddress] {
  override def path: JsPath = PartnershipDetails(index).path \ PartnershipAddressListId.toString
}

object PartnershipAddressListId {
  override def toString: String = "partnershipAddressResults"
}
