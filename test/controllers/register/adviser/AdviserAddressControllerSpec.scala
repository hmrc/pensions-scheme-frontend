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

package controllers.register.adviser

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.CSRFRequest
import config.FrontendAppConfig
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.adviser.routes._
import forms.address.AddressFormProvider
import identifiers.register.adviser.AdviserAddressId
import models.NormalMode
import models.address.{Address, TolerantAddress}
import models.register.establishers.company.director.DirectorDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{CountryOptions, FakeNavigator, InputOption, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class AdviserAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear

  val date = new LocalDate(year, month, day)
  val director = DirectorDetails("first", Some("middle"), "last", LocalDate.now())

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val fakeAuditService = new StubSuccessfulAuditService()

  "AdviserAddress Controller" must {

    "render manualAddress from GET request" in {

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
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
            Message(controller.heading),
            secondaryHeader = Some(controller.secondary))

          def viewAsString(form: Form[_] = form): String = manualAddress(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

          val request = addToken(
            FakeRequest(AdviserAddressController.onPageLoad(NormalMode))
              .withHeaders("Csrf-Token" -> "nocheck")
          )

          val result = route(app, request).value

          status(result) must be(OK)

          contentAsString(result) mustEqual manualAddress(
            frontendAppConfig,
            form,
            viewmodel
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
          bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
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

            FakeDataCacheConnector.verify(AdviserAddressId, address)
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
        bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
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
              .advisersAddress(existingAddress)
              .advisersAddressList(selectedAddress)
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
}
