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

package controllers.register.trustees.individual

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.actions._
import controllers.behaviours.ControllerBehaviours
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.individual.{TrusteeAddressId, TrusteeDetailsId}
import models.address.Address
import models.person.PersonDetails
import models.{Index, NormalMode}
import navigators.Navigator
import org.joda.time.LocalDate
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.TrusteesIndividual
import utils.{CountryOptions, FakeFeatureSwitchManagementService, FakeNavigator, InputOption}
import viewmodels.address.ManualAddressViewModel

class TrusteeAddressControllerSpec extends ControllerBehaviours {

  val firstIndex = Index(0)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val fakeAuditService = new StubSuccessfulAuditService()

  val personDetails = PersonDetails("First", None, "Last", LocalDate.now())

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(TrusteeDetailsId.toString -> personDetails))
  )))

  private implicit val builder: GuiceApplicationBuilder = new GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAppConfig].to(frontendAppConfig),
      bind[Navigator].toInstance(FakeNavigator),
      bind[UserAnswersService].toInstance(FakeUserAnswersService),
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].to(retrieval),
      bind[CountryOptions].to(countryOptions),
      bind[FeatureSwitchManagementService].to(new FakeFeatureSwitchManagementService(false))
    )

  private val controller = builder.build().injector.instanceOf[TrusteeAddressController]

  val viewmodel = ManualAddressViewModel(
    postCall = controller.postCall(NormalMode, firstIndex, None),
    countryOptions = countryOptions.options,
    title = messages("messages__trustee__individual__address__confirm__title"),
    heading = messages("messages__common__confirmAddress__h1", personDetails.fullName)
  )

  behave like manualAddress(
    routes.TrusteeAddressController.onPageLoad(NormalMode, firstIndex, None),
    routes.TrusteeAddressController.onSubmit(NormalMode, firstIndex, None),
    TrusteeAddressId(firstIndex),
    viewmodel
  )

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
      bind[AuditService].toInstance(fakeAuditService),
      bind[FeatureSwitchManagementService].to(new FakeFeatureSwitchManagementService(false))
    )) {
      implicit app =>

        val fakeRequest = addToken(FakeRequest(routes.TrusteeAddressController.onSubmit(NormalMode, firstIndex, None))
          .withHeaders("Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(
            ("addressLine1", address.addressLine1),
            ("addressLine2", address.addressLine2),
            ("postCode", address.postcode.get),
            "country" -> address.country))

        fakeAuditService.reset()

        val result = route(app, fakeRequest).value

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
