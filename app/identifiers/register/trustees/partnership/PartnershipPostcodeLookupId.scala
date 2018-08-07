package identifiers.register.trustees.partnership

import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.address.TolerantAddress
import play.api.libs.json.{JsPath, __}

class PartnershipPostcodeLookupId(index: Int) extends TypedIdentifier[Seq[TolerantAddress]] {
  override def path: JsPath = __ \ TrusteesId.toString \ index \ PartnershipPostcodeLookupId.toString
}

object PartnershipPostcodeLookupId {
  override def toString: String = "partnershipPostcodeLookup"
}