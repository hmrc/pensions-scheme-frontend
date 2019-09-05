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

package controllers

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions._
import controllers.routes._
import forms.address.AddressFormProvider
import identifiers.{AdviserAddressId, AdviserNameId}
import models.NormalMode
import models.address.Address
import models.person.PersonDetails
import navigators.Navigator
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{CountryOptions, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class AdviserAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val fakeAuditService = new StubSuccessfulAuditService()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    AdviserNameId.toString -> "name"))
  )

  "AdviserAddress Controller" must {

    "render manualAddress from GET request" in {

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataRetrievalAction].to(retrieval),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AuthAction].to(FakeAuthAction),
        bind[CountryOptions].to(countryOptions),
        bind[AuditService].toInstance(fakeAuditService)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[AdviserAddressController]

          val viewmodel = ManualAddressViewModel(
            controller.postCall(NormalMode),
            countryOptions.options,
            Message(controller.title),
            Message(controller.heading("name")))

          val request = addToken(
            FakeRequest(AdviserAddressController.onPageLoad(NormalMode))
              .withHeaders("Csrf-Token" -> "nocheck")
          )

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual manualAddress(
            frontendAppConfig,
            form,
            viewmodel,
            None
          )(request, messages).toString

      }

    }

    "redirect to next page when valid data submitted" which {
      "save address" in {

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postcode = Some("AB1 1AB"),
          country = "GB"
        )

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[DataRetrievalAction].to(retrieval),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressFormProvider].to(formProvider)
        )) {
          implicit app =>

            val fakeRequest = addToken(FakeRequest(AdviserAddressController.onSubmit(NormalMode))
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postcode.get),
                "country" -> address.country))

            val result = route(app, fakeRequest).value

            status(result) must be(SEE_OTHER)

            FakeUserAnswersService.userAnswer.get(AdviserAddressId).value mustEqual address
        }
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
        bind[DataRetrievalAction].to(retrieval),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AuthAction].to(FakeAuthAction),
        bind[CountryOptions].to(countryOptions),
        bind[AuditService].toInstance(fakeAuditService)
      )) {
        implicit app =>

          val fakeRequest = addToken(FakeRequest(AdviserAddressController.onSubmit(NormalMode))
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
                  "Adviser Address",
                  address
                )
              )
          }
      }
    }
  }
}
