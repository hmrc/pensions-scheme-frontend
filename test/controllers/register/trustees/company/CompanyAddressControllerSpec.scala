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

package controllers.register.trustees.company

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import controllers.behaviours.ControllerBehaviours
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.{CompanyAddressId, CompanyDetailsId}
import models.address.{Address, TolerantAddress}
import models.{CompanyDetails, Index, NormalMode}
import navigators.TrusteesCompanyNavigator
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.TrusteesCompany
import utils.{CountryOptions, FakeNavigator, InputOption, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel

class CompanyAddressControllerSpec extends ControllerBehaviours {

  val firstIndex = Index(0)

  val companyDetails = CompanyDetails("companyName", None, None)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val fakeAuditService = new StubSuccessfulAuditService()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(CompanyDetailsId.toString -> companyDetails))
  )))

  implicit val builder = new GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAppConfig].to(frontendAppConfig),
      bind[Navigator].qualifiedWith(classOf[TrusteesCompany]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].to(retrieval),
      bind[CountryOptions].to(countryOptions)
    )

  val controller = builder.build().injector.instanceOf[CompanyAddressController]

  val viewmodel = ManualAddressViewModel(
    controller.postCall(NormalMode, firstIndex),
    countryOptions.options,
    Message(controller.title),
    Message(controller.heading),
    secondaryHeader = Some(companyDetails.companyName),
    Some(Message(controller.hint))
  )

  behave like manualAddress(
    routes.CompanyAddressController.onPageLoad(NormalMode, firstIndex),
    routes.CompanyAddressController.onSubmit(NormalMode, firstIndex),
    CompanyAddressId(firstIndex),
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
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
      bind[AuthAction].to(FakeAuthAction),
      bind[CountryOptions].to(countryOptions),
      bind[AuditService].toInstance(fakeAuditService)
    )) {
      implicit app =>

        val fakeRequest = addToken(FakeRequest(routes.CompanyAddressController.onSubmit(NormalMode, firstIndex))
          .withHeaders("Csrf-Token" -> "nocheck")
          .withFormUrlEncodedBody(
            ("addressLine1", address.addressLine1),
            ("addressLine2", address.addressLine2),
            ("postCode", address.postcode.get),
            "country" -> address.country))

        val existingAddress = Address(
          "existing-line-1",
          "existing-line-2",
          None,
          None,
          None,
          "existing-country"
        )

        val selectedAddress = TolerantAddress(None, None, None, None, None, None)

        val data =
          UserAnswers()
            .trusteesCompanyAddress(firstIndex, existingAddress)
            .trusteesCompanyAddressList(firstIndex, selectedAddress)
            .dataRetrievalAction

        fakeAuditService.reset()

        val result = route(app, fakeRequest).value

        whenReady(result) {
          _ =>
            fakeAuditService.verifySent(
              AddressEvent(
                FakeAuthAction.externalId,
                AddressAction.LookupChanged
              )
            )
        }
    }
  }

}
