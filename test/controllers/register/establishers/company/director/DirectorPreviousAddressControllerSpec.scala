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

package controllers.register.establishers.company.director

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent, AuditService}
import base.CSRFRequest
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorDetailsId, DirectorPreviousAddressId}
import models.address.Address
import models.person.PersonDetails
import models.{Index, NormalMode}
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
import utils.{CountryOptions, FakeFeatureSwitchManagementService, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorPreviousAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with CSRFRequest with OptionValues {

  val establisherIndex = Index(0)
  val directorIndex = Index(0)

  val directorDetails = PersonDetails("first", Some("middle"), "last", LocalDate.now())

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val fakeAuditService = new StubSuccessfulAuditService()
  val fakeFeatureSwitch = new FakeFeatureSwitchManagementService(false)

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj("director" -> Json.arr(
        Json.obj(DirectorDetailsId.toString -> directorDetails)
      )
      )))))

  "PreviousAddress Controller" must {

    "render manualAddress from GET request" in {

      running(_.overrides(
        bind[FrontendAppConfig].to(frontendAppConfig),
        bind[Navigator].toInstance(FakeNavigator),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].to(retrieval),
        bind[CountryOptions].to(countryOptions),
        bind[FeatureSwitchManagementService].to(fakeFeatureSwitch)
      )) {
        implicit app =>

          val controller = app.injector.instanceOf[DirectorPreviousAddressController]

          val viewmodel = ManualAddressViewModel(
            controller.postCall(NormalMode, establisherIndex, directorIndex, None),
            countryOptions.options,
            controller.title,
            Message(controller.heading, directorDetails.fullName),
            secondaryHeader = Some(directorDetails.fullName)
          )

          val request = addToken(
            FakeRequest(routes.DirectorPreviousAddressController.onPageLoad(NormalMode, establisherIndex, directorIndex, None))
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
      "saves director address" in {

        val onwardCall = routes.DirectorContactDetailsController.onPageLoad(NormalMode, establisherIndex, directorIndex, None)

        running(_.overrides(
          bind[FrontendAppConfig].to(frontendAppConfig),
          bind[MessagesApi].to(messagesApi),
          bind[UserAnswersService].toInstance(FakeUserAnswersService),
          bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardCall)),
          bind[AuthAction].to(FakeAuthAction),
          bind[DataRetrievalAction].to(retrieval),
          bind[DataRequiredAction].to(new DataRequiredActionImpl),
          bind[AddressFormProvider].to(formProvider),
          bind[FeatureSwitchManagementService].to(fakeFeatureSwitch)
        )) {
          implicit app =>

            val address = Address(
              "value 1",
              "value 2",
              None, None,
              Some("AB1 1AB"),
              "GB"
            )

            val fakeRequest = addToken(FakeRequest(routes.DirectorPreviousAddressController.onSubmit(NormalMode, establisherIndex, directorIndex, None))
              .withHeaders("Csrf-Token" -> "nocheck")
              .withFormUrlEncodedBody(
                ("addressLine1", address.addressLine1),
                ("addressLine2", address.addressLine2),
                ("postCode", address.postcode.get),
                "country" -> address.country))

            val result = route(app, fakeRequest).value

            status(result) must be(SEE_OTHER)
            redirectLocation(result).value mustEqual onwardCall.url

            FakeUserAnswersService.userAnswer.get(DirectorPreviousAddressId(establisherIndex, directorIndex)).value mustEqual address
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
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AuthAction].to(FakeAuthAction),
        bind[CountryOptions].to(countryOptions),
        bind[DataRetrievalAction].to(retrieval),
        bind[AuditService].toInstance(fakeAuditService)
      )) {
        implicit app =>

          val fakeRequest = addToken(FakeRequest(routes.DirectorPreviousAddressController.onSubmit(NormalMode, establisherIndex, directorIndex, None))
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
                  s"Company Director Previous Address: ${directorDetails.fullName}",
                  address
                )
              )
          }
      }
    }
  }
}
