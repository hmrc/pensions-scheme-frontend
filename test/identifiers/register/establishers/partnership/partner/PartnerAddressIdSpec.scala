package identifiers.register.establishers.partnership.partner

import base.SpecBase
import models.address.Address
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, InputOption, UserAnswers}
import viewmodels.{AnswerRow, Message}

class PartnerAddressIdSpec extends SpecBase {

  private val partnerName = "test partner"

  "cya" when {
    implicit val countryOptions: CountryOptions = new CountryOptions(Seq.empty[InputOption])

    val address = Address(
      "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
    )

    def addressAnswer(address: Address): Seq[String] = {
      val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
      Seq(
        Some(address.addressLine1),
        Some(address.addressLine2),
        address.addressLine3,
        address.addressLine4,
        address.postcode,
        Some(country)
      ).flatten
    }

    val onwardUrl = "onwardUrl"
    Seq(NormalMode, UpdateMode).foreach{ mode =>

      s"in ${mode.toString} mode" must {
        "return answers rows with change links" in {
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers().set(PartnerAddressId(0, 0))(address).flatMap(
              _.set(PartnerNameId(0, 0))(PersonName("test", "partner"))).asOpt.value, PsaId("A0000000"))

          PartnerAddressId(0, 0).row(onwardUrl, mode) must equal(Seq(
            AnswerRow(
              Message("messages__address__cya", "test company"),
              addressAnswer(address),
              false,
              Some(Link("site.change", onwardUrl, Some(messages("messages__visuallyhidden__dynamic_address", partnerName))))
            )))
        }
      }
    }
  }
}