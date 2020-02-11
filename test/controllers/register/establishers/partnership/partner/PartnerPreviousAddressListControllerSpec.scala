/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions.FakeDataRetrievalAction
import forms.address.AddressListFormProvider
import identifiers.register.establishers.partnership.partner.{PartnerNameId, PartnerPreviousAddressPostcodeLookupId}
import models.address.TolerantAddress
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.EstablishersPartner
import utils.{FakeNavigator, UserAnswers}
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class PartnerPreviousAddressListControllerSpec extends ControllerSpecBase {

  private val partnerDetails = PersonName("Joe", "Bloggs")

  private val previousAddressTitle = s"What was the partner’s previous address?"
  private val previousAddressHeading = s"What was ${partnerDetails.fullName}’s previous address?"

  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  private val data =
    UserAnswers(Json.obj())
      .set(PartnerNameId(0, 0))(partnerDetails)
      .flatMap(_.set(PartnerPreviousAddressPostcodeLookupId(0, 0))(addresses))
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  private val onwardRoute = routes.PartnerAddressYearsController.onPageLoad(NormalMode, Index(0), Index(0), None)

  "Partnership Partners Previous Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      running(_.overrides(modules(dataRetrievalAction): _*)) { app =>
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val view = app.injector.instanceOf[addressList]
        val result = controller.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(fakeRequest)

        status(result) mustBe OK

        val viewModel: AddressListViewModel = addressListViewModel(addresses)
        val form = new AddressListFormProvider()(viewModel.addresses)

        contentAsString(result) mustBe view(form, viewModel, None)(fakeRequest, messages).toString
      }
    }

    "redirect to Partner Address Post Code Lookup if no address data on a GET request" in {
      running(_.overrides(modules(UserAnswers().dataRetrievalAction): _*)) { app =>
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val result = controller.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None).url)
      }
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {
      running(_.overrides(modules(dontGetAnyData): _*)) { app =>
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val result = controller.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to the next page on POST of valid data" ignore {
      running(_.overrides(modules(dataRetrievalAction) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersPartner]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) { app =>
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe onwardRoute.url
      }
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      running(_.overrides(modules(dontGetAnyData) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersPartner]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) { app =>
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "redirect to Partner Address Post Code Lookup if no address data on a POST request" in {
      running(_.overrides(modules(getEmptyData) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersPartner]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) { app =>
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "0"))
        val controller = app.injector.instanceOf[PartnerPreviousAddressListController]
        val result = controller.onSubmit(NormalMode, establisherIndex = 0, partnerIndex = 0, None)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(
          routes.PartnerPreviousAddressPostcodeLookupController.onPageLoad(NormalMode, establisherIndex = 0, partnerIndex = 0, None).url)
      }
    }
  }

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.PartnerPreviousAddressListController.onSubmit(NormalMode, Index(0), Index(0), None),
      routes.PartnerPreviousAddressController.onPageLoad(NormalMode, Index(0), Index(0), None),
      addresses,
      title = previousAddressTitle,
      heading = previousAddressHeading,
      entityName = partnerDetails.fullName
    )
  }
}
