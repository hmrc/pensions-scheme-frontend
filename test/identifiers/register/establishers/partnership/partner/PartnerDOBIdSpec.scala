package identifiers.register.establishers.partnership.partner

import base.SpecBase
import models.person.PersonName
import models.requests.DataRequest
import models.{Link, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.{CountryOptions, DateHelper, UserAnswers}
import viewmodels.AnswerRow

class PartnerDOBIdSpec extends SpecBase {

  implicit val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)
  private val onwardUrl = "onwardUrl"
  val date = new LocalDate()
  private val answerRowsWithChangeLinks = Seq(
    AnswerRow(messages("messages__DOB__heading", "Test Name"), List(DateHelper.formatDate(date)), false, Some(Link("site.change", onwardUrl,
      Some(messages("messages__visuallyhidden__dynamic_dob", "Test Name")))))
  )

  "cya" when {

    def answers: UserAnswers = UserAnswers().set(PartnerDOBId(0))(date).flatMap(_.set(PartnerNameId(0))(PersonName("Test", "Name"))).asOpt.get

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnerDOBId(0).row(onwardUrl, NormalMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for new trustee" must {

      def answersNew: UserAnswers = answers.set(IsPartnerNewId(0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers
        PartnerDOBId(0).row(onwardUrl, UpdateMode) must equal(answerRowsWithChangeLinks)
      }
    }

    "in update mode for existing trustee" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers: UserAnswers = request.userAnswers

        PartnerDOBId(0).row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(messages("messages__DOB__heading", "Test Name"), List(DateHelper.formatDate(date)), false, None)
        ))
      }
    }
  }
}
