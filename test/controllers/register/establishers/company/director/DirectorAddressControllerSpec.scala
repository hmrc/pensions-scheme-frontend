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

package controllers.register.establishers.company.director

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.{DirectorAddressId, DirectorNameId}
import models.address.Address
import models.person.PersonName
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import navigators.Navigator
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{CountryOptions, FakeNavigator, InputOption}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class DirectorAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)

  private val onwardCall = routes.DirectorAddressYearsController.onPageLoad(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)
  private val director = PersonName("first", "last")

  private val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  private val fakeAuditService = new StubSuccessfulAuditService()
  private val formProvider = new AddressFormProvider(countryOptions)
  private val form: Form[Address] = formProvider()

  private val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString -> director
          )
        )
      )
    )
  )
  )
  )

  private def viewmodel(postCall: Call) = ManualAddressViewModel(
    postCall,
    countryOptions.options,
    title = Message("messages__common__confirmAddress__h1", Message("messages__theDirector")),
    heading = Message("messages__common__confirmAddress__h1", director.fullName),
    srn = EmptyOptionalSchemeReferenceNumber
  )

  private val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

  private val view = injector.instanceOf[manualAddress]

  "Address Controller" must {

    "render manualAddress from GET request" in {
      val postCall = routes.DirectorAddressController.onSubmit( NormalMode, Index(establisherIndex), Index(directorIndex), EmptyOptionalSchemeReferenceNumber)
      running(_.overrides(modules(retrieval) ++
        Seq[GuiceableModule](bind[CountryOptions].to(countryOptions)): _*)) {
        app =>
          val controller = app.injector.instanceOf[DirectorAddressController]

          val result = controller.onPageLoad(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

          status(result) must be(OK)

          contentAsString(result) mustEqual view(
            form,
            viewmodel(postCall),
            None
          )(fakeRequest, messages).toString

      }
    }

    "redirect to next page on POST request" which {
      "saves director address" in {
        val onwardCall = routes.DirectorAddressYearsController.onPageLoad(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)
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

          val controller = app.injector.instanceOf[DirectorAddressController]

          val result = controller.onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardCall.url

          FakeUserAnswersService.userAnswer.get(DirectorAddressId(establisherIndex, directorIndex)).value mustEqual address
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

        val controller = app.injector.instanceOf[DirectorAddressController]

        val result = controller.onSubmit(NormalMode, establisherIndex, directorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

        fakeAuditService.reset()

        whenReady(result) {
          _ =>
            fakeAuditService.verifySent(
              AddressEvent(
                FakeAuthAction.externalId,
                AddressAction.LookupChanged,
                s"Company Director Address: ${director.fullName}",
                address
              )
            )
        }
      }
    }
  }
}
