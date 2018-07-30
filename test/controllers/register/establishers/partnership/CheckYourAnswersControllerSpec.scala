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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.establishers.partnership._
import models.AddressYears.UnderAYear
import models.address.Address
import models.{ContactDetails, Index, PartnershipDetails, Vat}
import play.api.test.Helpers._
import utils._

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  val firstIndex = Index(0)
  val partnershipName = "PartnershipName"

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      FakeNavigator
    )

  "CheckYourAnswersController" must {

    val partnershipAnswers = UserAnswers()
      .set(PartnershipDetailsId(firstIndex))(PartnershipDetails(partnershipName))
      .flatMap(_.set(PartnershipVatId(firstIndex))(Vat.No))
      .flatMap(_.set(PartnershipAddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
      .flatMap(_.set(PartnershipAddressYearsId(firstIndex))(UnderAYear))
      .flatMap(_.set(PartnershipPreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "US")))
      .flatMap(_.set(PartnershipContactDetailsId(firstIndex))(ContactDetails("e@mail.co", "98765")))
      .asOpt.value

    "display answers" in {

      val request = FakeDataRequest(partnershipAnswers)
      val result = controller(partnershipAnswers.dataRetrievalAction).onPageLoad(firstIndex)(request)

      status(result) mustBe OK

      val content = contentAsString(result)

      content must include(messages("messages__partnership__checkYourAnswers__partnership_details"))
      content must include(messages("messages__partnership__checkYourAnswers__partnership_contact_details"))
    }

    "redirect to Session Expired when establisher name cannot be retrieved" in {

      val request = FakeDataRequest(partnershipAnswers)
      val result = controller().onPageLoad(firstIndex)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)

    }

  }

}