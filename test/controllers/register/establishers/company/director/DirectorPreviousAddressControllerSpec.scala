/*
 * Copyright 2021 HM Revenue & Customs
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
import audit.{AddressAction, AddressEvent}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorNameId, DirectorPreviousAddressId}
import models.address.Address
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{CountryOptions, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorPreviousAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  val establisherIndex = Index(0)
  val directorIndex = Index(0)

  val directorDetails = PersonName("first", "last")

  private val onwardCall = routes.DirectorEmailController.onPageLoad(NormalMode, establisherIndex, directorIndex, None)

  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val fakeAuditService = new StubSuccessfulAuditService()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj("director" -> Json.arr(
        Json.obj(DirectorNameId.toString -> directorDetails)
      )
      )))))

  private val view = injector.instanceOf[manualAddress]

  val viewmodel = ManualAddressViewModel(
    routes.DirectorPreviousAddressController.onSubmit(NormalMode, establisherIndex, directorIndex, None),
    countryOptions.options,
    Message("messages__common__confirmPreviousAddress__h1", Message("messages__theDirector")),
    Message("messages__common__confirmPreviousAddress__h1", directorDetails.fullName)
  )
  val address = Address(
    "value 1",
    "value 2",
    None, None,
    Some("AB1 1AB"),
    "GB"
  )
  "PreviousAddress Controller" must {

    "render manualAddress from GET request" in {
      running(_.overrides(modules(retrieval) ++
        Seq[GuiceableModule](bind[CountryOptions].to(countryOptions)): _*)) {
        app =>
          val controller = app.injector.instanceOf[DirectorPreviousAddressController]

          val result = controller.onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)

          status(result) must be(OK)

          contentAsString(result) mustEqual view(
            form,
            viewmodel,
            None
          )(fakeRequest, messages).toString

      }
    }

    "redirect to next page on POST request" which {
      "saves director address" in {
        val onwardCall = routes.DirectorEmailController.onPageLoad(NormalMode, establisherIndex, directorIndex, None)
        running(_.overrides(modules(retrieval) ++
          Seq[GuiceableModule](bind[CountryOptions].to(countryOptions),
            bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardCall)),
            bind[UserAnswersService].toInstance(FakeUserAnswersService)
          ): _*)) { app =>

          val postRequest = fakeRequest.withFormUrlEncodedBody(
            ("addressLine1", address.addressLine1),
            ("addressLine2", address.addressLine2),
            ("postCode", address.postcode.get),
            "country" -> address.country)

          val controller = app.injector.instanceOf[DirectorPreviousAddressController]

          val result = controller.onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardCall.url

          FakeUserAnswersService.userAnswer.get(DirectorPreviousAddressId(establisherIndex, directorIndex)).value mustEqual address
        }
      }
    }

    "send an audit event when valid data is submitted" in {

      running(_.overrides(modules(retrieval) ++
        Seq[GuiceableModule](bind[CountryOptions].to(countryOptions),
          bind[Navigator].toInstance(new FakeNavigator(desiredRoute = onwardCall)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) { app =>

        val postRequest = fakeRequest.withFormUrlEncodedBody(
          ("addressLine1", address.addressLine1),
          ("addressLine2", address.addressLine2),
          ("postCode", address.postcode.get),
          "country" -> address.country)

        val controller = app.injector.instanceOf[DirectorPreviousAddressController]

        val result = controller.onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)

        fakeAuditService.reset()

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
