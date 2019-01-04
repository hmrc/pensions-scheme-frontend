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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.partnership._
import models.AddressYears.UnderAYear
import models._
import models.address.Address
import models.register.{DeclarationDormant, SchemeDetails}
import models.register.SchemeType.SingleTrust
import play.api.test.Helpers._
import utils._
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val firstIndex = Index(0)
  val partnershipName = "PartnershipName"
  val schemeName = "testScheme"
  val partnershipAnswers = UserAnswers()
    .set(SchemeDetailsId)(SchemeDetails(schemeName, SingleTrust))
    .flatMap(_.set(PartnershipDetailsId(firstIndex))(PartnershipDetails(partnershipName)))
    .flatMap(_.set(PartnershipVatId(firstIndex))(Vat.No))
    .flatMap(_.set(PartnershipUniqueTaxReferenceID(firstIndex))(UniqueTaxReference.Yes("0987654321")))
    .flatMap(_.set(PartnershipAddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnershipAddressYearsId(firstIndex))(UnderAYear))
    .flatMap(_.set(PartnershipPreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "US")))
    .flatMap(_.set(PartnershipContactDetailsId(firstIndex))(ContactDetails("e@mail.co", "98765")))
    .flatMap(_.set(IsPartnershipDormantId(firstIndex))(DeclarationDormant.Yes))
    .asOpt.value

  implicit val request = FakeDataRequest(partnershipAnswers)
  implicit val countryOptions = new FakeCountryOptions()
  private val onwardRoute = routes.AddPartnersController.onPageLoad(firstIndex)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      new FakeNavigator(onwardRoute),
      countryOptions
    )

  "CheckYourAnswersController" must {

    "display answers" in {

      val partnershipDetails = AnswerSection(
        Some("messages__partnership__checkYourAnswers__partnership_details"),
        Seq(
          PartnershipDetailsId(firstIndex).row(routes.PartnershipDetailsController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipVatId(firstIndex).row(routes.PartnershipVatController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipPayeId(firstIndex).row(routes.PartnershipPayeController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipUniqueTaxReferenceID(firstIndex).row(routes.PartnershipUniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url),
          IsPartnershipDormantId(firstIndex).row(routes.IsPartnershipDormantController.onPageLoad(CheckMode, firstIndex).url)
        ).flatten
      )

      val partnershipContactDetails = AnswerSection(
        Some("messages__partnership__checkYourAnswers__partnership_contact_details"),
        Seq(
          PartnershipAddressId(firstIndex).row(routes.PartnershipAddressController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipAddressYearsId(firstIndex).row(routes.PartnershipAddressYearsController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipPreviousAddressId(firstIndex).row(routes.PartnershipPreviousAddressController.onPageLoad(CheckMode, firstIndex).url),
          PartnershipContactDetailsId(firstIndex).row(routes.PartnershipContactDetailsController.onPageLoad(CheckMode, firstIndex).url)
        ).flatten
      )

      val result = controller(partnershipAnswers.dataRetrievalAction).onPageLoad(firstIndex)(request)

      lazy val viewAsString: String = check_your_answers(
        frontendAppConfig,
        Seq(partnershipDetails, partnershipContactDetails),
        routes.CheckYourAnswersController.onSubmit(firstIndex)
      )(fakeRequest, messages).toString

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired when establisher name cannot be retrieved" in {

      val result = controller().onPageLoad(firstIndex)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Add Partners page on submit" which {
      "marks partnership as complete on submit" in {

        val result = controller().onSubmit(firstIndex)(request)

        status(result) mustBe 303
        redirectLocation(result) mustBe Some(onwardRoute.url)

        FakeSectionComplete.verify(IsPartnershipCompleteId(firstIndex), true)
      }
    }
  }

}
