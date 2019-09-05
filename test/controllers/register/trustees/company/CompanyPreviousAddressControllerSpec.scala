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

package controllers.register.trustees.company

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.CSRFRequest
import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.trustees.company.routes._
import forms.address.AddressFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.{CompanyDetailsId, CompanyPreviousAddressId}
import models.address.Address
import models.{CompanyDetails, Index, NormalMode}
import navigators.Navigator
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
import utils.annotations.TrusteesCompany
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class CompanyPreviousAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  def countryOptions: CountryOptions = new CountryOptions(options)

  val options = Seq(InputOption("territory:AE-AZ", "Abu Dhabi"), InputOption("country:AF", "Afghanistan"))

  val firstIndex = Index(0)

  val formProvider = new AddressFormProvider(FakeCountryOptions())
  val companyDetails = CompanyDetails("companyName")

  val fakeAuditService = new StubSuccessfulAuditService()

  val form: Form[Address] = formProvider()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    TrusteesId.toString -> Json.arr(Json.obj(CompanyDetailsId.toString -> companyDetails))
  )))

  "PreviousAddress Controller" must {

    "render manualAddress from GET request" in {

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(retrieval),
        bind[CountryOptions].to(countryOptions),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[CompanyPreviousAddressController]

          val viewmodel = ManualAddressViewModel(
            controller.postCall(NormalMode, firstIndex, None),
            countryOptions.options,
            Message(controller.title),
            Message(controller.heading, companyDetails.companyName)
          )

          val request = addToken(
            FakeRequest(CompanyPreviousAddressController.onPageLoad(NormalMode, firstIndex, None))
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

    "redirect to next page on POST request" which {
      "saves address" in {

        val onwardCall = controllers.routes.IndexController.onPageLoad()

        val address = Address(
          addressLine1 = "value 1",
          addressLine2 = "value 2",
          None, None,
          postcode = Some("AB1 1AB"),
          country = "GB"
        )

        running(_.overrides(
          bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardCall)),
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].to(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressFormProvider].to(formProvider)
        )) {
          implicit app =>


            val fakeRequest = addToken(FakeRequest(CompanyPreviousAddressController.onSubmit(NormalMode, firstIndex, None))
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postcode.get),
                "country" -> address.country))

            val result = route(app, fakeRequest).value

            status(result) must be(SEE_OTHER)
            redirectLocation(result).value mustEqual onwardCall.url

            FakeUserAnswersService.userAnswer.get(CompanyPreviousAddressId(firstIndex)).value mustEqual address
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

          val fakeRequest = addToken(FakeRequest(routes.CompanyPreviousAddressController.onSubmit(NormalMode, firstIndex, None))
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
                  s"Trustee Company Previous Address: ${companyDetails.companyName}",
                  address
                )
              )
          }
      }
    }
  }
}
