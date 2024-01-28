/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.register.trustees.individual

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import config.FrontendAppConfig
import controllers.actions._
import controllers.behaviours.AddressControllerBehaviours
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeAddressId, TrusteeNameId}
import models.address.Address
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{CountryOptions, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class TrusteeAddressControllerSpec extends AddressControllerBehaviours {

  val firstIndex: Index = Index(0)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val fakeAuditService = new StubSuccessfulAuditService()

  val personDetails: PersonName = PersonName("First", "Last")

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(TrusteeNameId.toString -> personDetails))
  )))

  private implicit val builder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAppConfig].to(frontendAppConfig),
      bind[Navigator].toInstance(FakeNavigator),
      bind[UserAnswersService].toInstance(FakeUserAnswersService),
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].to(retrieval),
      bind[CountryOptions].to(countryOptions)
    )

  private val controller = builder.build().injector.instanceOf[TrusteeAddressController]

  val viewmodel: ManualAddressViewModel = ManualAddressViewModel(
    postCall = controller.postCall(NormalMode, firstIndex, None),
    countryOptions = countryOptions.options,
    title = Messages("messages__common__confirmAddress__h1", Message("messages__theTrustee").resolve),
    heading = Messages("messages__common__confirmAddress__h1", personDetails.fullName)
  )

  behave like manualAddress(
    routes.TrusteeAddressController.onPageLoad(NormalMode, firstIndex, None),
    routes.TrusteeAddressController.onSubmit(NormalMode, firstIndex, None),
    TrusteeAddressId(firstIndex),
    viewmodel
  )

  "save address and redirect to next page on POST request" in {
    running(_ => builder) {
      implicit app =>

        val onwardCall = Call("GET", "www.example.com")

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postcode = Some("AB1 1AB"),
          country = "GB"
        )

        val fakeRequest = addCSRFToken(FakeRequest()
          .withHeaders("Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(
            ("addressLine1", address.addressLine1),
            ("addressLine2", address.addressLine2),
            ("postCode", address.postcode.get),
            "country" -> address.country))

        val controller = app.injector.instanceOf[TrusteeAddressController]
        val result = controller.onSubmit(NormalMode, firstIndex, None)(fakeRequest)

        status(result) must be(SEE_OTHER)
        redirectLocation(result).value mustEqual onwardCall.url

        FakeUserAnswersService.userAnswer.get(TrusteeAddressId(firstIndex)).value mustEqual address
    }
  }

  "send an audit event when valid data is submitted" in {

    val address = Address(
      addressLine1 = "value 1",
      addressLine2 = "value 2",
      None, None,
      postcode = Some("AB1 1AB"),
      country = "GB"
    )

    running(_.overrides(
      bind[FrontendAppConfig].to(frontendAppConfig),
      bind[Navigator].toInstance(FakeNavigator),
      bind[UserAnswersService].toInstance(FakeUserAnswersService),
      bind[AuthAction].to(FakeAuthAction),
      bind[CountryOptions].to(countryOptions),
      bind[DataRetrievalAction].to(retrieval),
      bind[AuditService].toInstance(fakeAuditService)
    )) {
      implicit app =>
        fakeAuditService.reset()

        val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(
          ("addressLine1", address.addressLine1),
          ("addressLine2", address.addressLine2),
          ("postCode", address.postcode.get),
          "country" -> address.country))
        val controller = app.injector.instanceOf[TrusteeAddressController]
        val result = controller.onSubmit(NormalMode, firstIndex, None)(request)

        whenReady(result) {
          _ =>
            fakeAuditService.verifySent(
              AddressEvent(
                FakeAuthAction.externalId,
                AddressAction.LookupChanged,
                s"Trustee Address: ${personDetails.fullName}",
                address
              )
            )
        }
    }
  }
}
