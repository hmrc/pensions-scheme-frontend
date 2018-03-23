package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.AddressYears
import play.api.libs.json.JsPath
import utils.Cleanup

case class AddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = TrusteesId.path \ index \ AddressYearsId.toString
}

object AddressYearsId {

  override lazy val toString: String =
    "addressYears"
}

