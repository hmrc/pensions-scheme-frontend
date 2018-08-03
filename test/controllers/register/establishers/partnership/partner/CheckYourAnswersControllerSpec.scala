/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.establishers.partnership.partner

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.partnership.partner._
import models.address.Address
import models.person.PersonDetails
import models.register.SchemeDetails
import models.register.SchemeType.SingleTrust
import models.{CheckMode, Index, _}
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import utils.checkyouranswers.Ops._
import utils.{FakeCountryOptions, FakeDataRequest, FakeNavigator, FakeSectionComplete, UserAnswers, _}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(partnerAnswers)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      new FakeNavigator(desiredRoute),
      countryOptions
    )

  "CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {
        val partnerDetails = AnswerSection(
          Some("messages__partner__cya__details_heading"),
          Seq(
            PartnerDetailsId(firstIndex, firstIndex).
              row(routes.PartnerDetailsController.onPageLoad(CheckMode, firstIndex, firstIndex).url),
            PartnerNinoId(firstIndex, firstIndex).
              row(routes.PartnerNinoController.onPageLoad(CheckMode, firstIndex, firstIndex).url),
            PartnerUniqueTaxReferenceId(firstIndex, firstIndex).
              row(routes.PartnerUniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex, firstIndex).url)
          ).flatten
        )

        val partnerContactDetails = AnswerSection(
          Some("messages__partner__cya__contact__details_heading"),
          Seq(
            PartnerAddressId(firstIndex, firstIndex).
              row(routes.PartnerAddressController.onPageLoad(CheckMode, firstIndex, firstIndex).url),
            PartnerAddressYearsId(firstIndex, firstIndex).
              row(routes.PartnerAddressYearsController.onPageLoad(CheckMode, firstIndex, firstIndex).url),
            PartnerPreviousAddressId(firstIndex, firstIndex).
              row(routes.PartnerPreviousAddressController.onPageLoad(CheckMode, firstIndex, firstIndex).url),
            PartnerContactDetailsId(firstIndex, firstIndex).
              row(routes.PartnerContactDetailsController.onPageLoad(CheckMode, firstIndex, firstIndex).url)
          ).flatten
        )

        val viewAsString = check_your_answers(
          frontendAppConfig,
          Seq(partnerDetails, partnerContactDetails),
          Some(schemeName),
          routes.CheckYourAnswersController.onSubmit(firstIndex, firstIndex)
        )(fakeRequest, messages).toString

        val result = controller(partnerAnswers.dataRetrievalAction).onPageLoad(firstIndex, firstIndex)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }

      "redirect to Session Expired when scheme name is not present" in {
        val result = controller().onPageLoad(firstIndex, firstIndex)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(firstIndex, firstIndex)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeSectionComplete.verify(IsPartnerCompleteId(firstIndex, firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends OptionValues {
  val firstIndex = Index(0)
  val schemeName = "test scheme name"
  val desiredRoute = controllers.routes.IndexController.onPageLoad()

  val partnerAnswers = UserAnswers()
    .set(SchemeDetailsId)(SchemeDetails(schemeName, SingleTrust))
    .flatMap(_.set(PartnerDetailsId(firstIndex, firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false)))
    .flatMap(_.set(PartnerNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")))
    .flatMap(_.set(PartnerUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890")))
    .flatMap(_.set(PartnerAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(PartnerPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(PartnerContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value
}
