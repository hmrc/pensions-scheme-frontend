/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.IsTrusteeNewId
import identifiers.register.trustees.partnership._
import models.AddressYears.UnderAYear
import models._
import models.address.Address
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.CheckYourAnswers.{PayeCYA, VatCYA}
import utils.checkyouranswers.Ops._
import utils.checkyouranswers.UniqueTaxReferenceCYA
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswersController" when {

    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(partnershipAnswers)
        val result = controller(partnershipAnswers.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(answerSections(request))
      }

      behave like changeableController(
        controller(partnershipAnswers.dataRetrievalAction, _: AllowChangeHelper).onPageLoad(NormalMode, firstIndex, None)(FakeDataRequest(partnershipAnswers))
      )
    }

    "on Page load if toggle on in UpdateMode" must {
      "return OK and the correct view for vat if not new trustee" in {
        val answers = UserAnswers().trusteePartnershipDetails(firstIndex, PartnershipDetails("PartnershipName"))
          .set(PartnershipEnterVATId(firstIndex))(ReferenceValue("098765432")).flatMap(
          _.set(PartnershipPayeVariationsId(firstIndex))(ReferenceValue("12345678"))).asOpt.value
        implicit val userAnswers = FakeDataRequest(answers)
        val expectedCompanyDetailsSection = partnershipDetailsSection(
          PartnershipDetailsId(firstIndex).row(partnershipNameRoute(), UpdateMode)++
          PartnershipEnterVATId(firstIndex).row(partnershipEnterVATRoute, UpdateMode)++
          PartnershipPayeVariationsId(firstIndex).row(partnershipPayeVariationsRoute, UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, srn)(userAnswers)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedCompanyDetailsSection, emptyPartnershipContactDetailsSection), UpdateMode, srn)
      }

      "return OK and the correct view for vat if new trustee" in {
        val answers = UserAnswers().trusteePartnershipDetails(firstIndex, PartnershipDetails("PartnershipName"))
          .set(PartnershipVatId(firstIndex))(Vat.Yes("098765432")).flatMap(
          _.set(PartnershipPayeId(firstIndex))(Paye.Yes("12345678"))).flatMap(
          _.set(IsTrusteeNewId(firstIndex))(true)).asOpt.value
        implicit val userAnswers = FakeDataRequest(answers)
        val expectedPartnershipDetailsSection = partnershipDetailsSection(
          PartnershipDetailsId(firstIndex).row(partnershipNameRoute(CheckUpdateMode, srn), UpdateMode)++
          PartnershipVatId(firstIndex).row(partnershipVatRoute(CheckUpdateMode, srn), UpdateMode)++
          PartnershipPayeId(firstIndex).row(partnershipPayeRoute(CheckUpdateMode, srn), UpdateMode)
        )
        val result = controller(answers.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, srn)(userAnswers)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(Seq(expectedPartnershipDetailsSection, emptyPartnershipContactDetailsSection), UpdateMode, srn)
      }
    }

    "on Submit" must {
      "redirect to next page " in {
        val result = controller().onSubmit(NormalMode, firstIndex, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends CheckYourAnswersControllerSpec {
  private val srn = Some("123")
  private val firstIndex = Index(0)
  implicit val countryOptions: CountryOptions = new FakeCountryOptions()
  private val onwardRoute = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None)
  private def partnershipVatRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.PartnershipVatController.onPageLoad(mode, firstIndex, srn).url
  private def partnershipNameRoute(mode: Mode = UpdateMode, srn: Option[String] = None) = routes.TrusteeDetailsController.onPageLoad(mode, firstIndex, srn).url
  private lazy val partnershipEnterVATRoute = routes.PartnershipEnterVATController.onPageLoad(CheckMode, firstIndex, None).url
  private def partnershipPayeRoute(mode: Mode = CheckMode, srn: Option[String] = None) = routes.PartnershipPayeController.onPageLoad(mode, firstIndex, srn).url
  private lazy val partnershipPayeVariationsRoute = routes.PartnershipPayeVariationsController.onPageLoad(CheckMode, firstIndex, None).url
  private lazy val partnershipUniqueTaxReferenceRoute = routes.PartnershipUniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex, None).url
  private lazy val partnershipDetailsRoute = routes.TrusteeDetailsController.onPageLoad(CheckMode, firstIndex, None).url

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
    new CheckYourAnswersController(frontendAppConfig, messagesApi, FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(),
      new DataRequiredActionImpl, FakeUserAnswersService, new FakeNavigator(onwardRoute), countryOptions, allowChangeHelper)

  implicit val partnershipAnswers: UserAnswers = UserAnswers()
    .set(PartnershipDetailsId(firstIndex))(PartnershipDetails("PartnershipName"))
    .flatMap(_.set(PartnershipVatId(firstIndex))(Vat.No))
    .flatMap(_.set(PartnershipUniqueTaxReferenceId(firstIndex))(UniqueTaxReference.Yes("0987654321")))
    .flatMap(_.set(PartnershipPayeId(firstIndex))(Paye.Yes("12345678")))
    .flatMap(_.set(PartnershipAddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnershipAddressYearsId(firstIndex))(UnderAYear))
    .flatMap(_.set(PartnershipPreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "US")))
    .flatMap(_.set(PartnershipContactDetailsId(firstIndex))(ContactDetails("e@mail.co", "98765")))
    .asOpt.value

  private def emptyPartnershipContactDetailsSection =
    AnswerSection(Some("messages__partnership__checkYourAnswers__partnership_contact_details"), Nil)

  def viewAsString(answerSections: Seq[AnswerSection], mode: Mode = NormalMode, srn: Option[String] = None): String = check_your_answers(
    frontendAppConfig, answerSections, routes.CheckYourAnswersController.onSubmit(mode, firstIndex, srn),
    None, srn = srn, hideEditLinks = false, hideSaveAndContinueButton = false)(fakeRequest, messages).toString

  private def partnershipDetailsSection(vatRow: Seq[AnswerRow]): AnswerSection =
    AnswerSection(Some("messages__partnership__checkYourAnswers__partnership_details"), vatRow)

  private def answerSections(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {
    val utrRows = UniqueTaxReferenceCYA[PartnershipUniqueTaxReferenceId]("messages__partnership__checkYourAnswers__utr",
      "messages__trustee_individual_utr_cya_label", "messages__partnership__checkYourAnswers__utr_no_reason",
      "messages__visuallyhidden__partnership__utr_yes_no", "messages__visuallyhidden__partnership__utr",
      "messages__visuallyhidden__partnership__utr_no"
    )().row(PartnershipUniqueTaxReferenceId(firstIndex))(partnershipUniqueTaxReferenceRoute, request.userAnswers)

    val payeRows = PayeCYA[PartnershipPayeId](Some("messages__partnership__checkYourAnswers__paye"),
      "messages__visuallyhidden__partnership__paye_yes_no", "messages__visuallyhidden__partnership__paye_number"
    )().row(PartnershipPayeId(firstIndex))(partnershipPayeRoute(), request.userAnswers)

    val vatRows = VatCYA(Some("messages__partnership__checkYourAnswers__vat"))().
      row(PartnershipVatId(firstIndex))(partnershipVatRoute(), request.userAnswers)

    val partnershipDetailsSection = AnswerSection(Some("messages__partnership__checkYourAnswers__partnership_details"),
      PartnershipDetailsId(firstIndex).row(partnershipDetailsRoute) ++ vatRows ++ payeRows ++ utrRows)

    val contactDetailsSection = AnswerSection(
      Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
      Seq(
        PartnershipAddressId(firstIndex).row(routes.PartnershipAddressController.onPageLoad(CheckMode, firstIndex, None).url),
        PartnershipAddressYearsId(firstIndex).row(routes.PartnershipAddressYearsController.onPageLoad(CheckMode, firstIndex, None).url),
        PartnershipPreviousAddressId(firstIndex).row(routes.PartnershipPreviousAddressController.onPageLoad(CheckMode, firstIndex, None).url),
        PartnershipContactDetailsId(firstIndex).row(routes.PartnershipContactDetailsController.onPageLoad(CheckMode, firstIndex, None).url)
      ).flatten
    )
    Seq(partnershipDetailsSection, contactDetailsSection)
  }
}
