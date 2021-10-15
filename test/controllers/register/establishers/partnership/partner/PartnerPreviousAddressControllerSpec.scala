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

package controllers.register.establishers.partnership.partner

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.partner.{PartnerNameId, PartnerPreviousAddressId}
import models.address.Address
import models.person.PersonName
import models.{Index, NormalMode}
import navigators.Navigator
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
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

class PartnerPreviousAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  val establisherIndex = Index(0)
  val partnerIndex = Index(0)

  val partnerDetails = PersonName("first", "last")

  private val onwardCall = routes.PartnerEmailController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)
  private val postCall = routes.PartnerPreviousAddressController.onSubmit(NormalMode, establisherIndex, partnerIndex, None)
  val countryOptions = new CountryOptions(
    Seq(InputOption("GB", "GB"))
  )

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()

  val fakeAuditService = new StubSuccessfulAuditService()

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj("partner" -> Json.arr(
        Json.obj(PartnerNameId.toString -> partnerDetails)
      )
      )))))

  def viewmodel(postCall: Call = postCall) = ManualAddressViewModel(
    postCall,
    countryOptions.options,
    Message("messages__common__confirmPreviousAddress__h1", Message("messages__thePartner")),
    Message("messages__common__confirmPreviousAddress__h1", partnerDetails.fullName)
  )

  val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")

  private val view = injector.instanceOf[manualAddress]

  "PreviousAddress Controller" must {

    "render manualAddress from GET request" in {
      val postCall = routes.PartnerPreviousAddressController.onSubmit(NormalMode, establisherIndex, partnerIndex, None)
      running(_.overrides(modules(retrieval) ++
        Seq[GuiceableModule](bind[CountryOptions].to(countryOptions)): _*)) {
        app =>
          val controller = app.injector.instanceOf[PartnerPreviousAddressController]

          val result = controller.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)(fakeRequest)

          status(result) must be(OK)

          contentAsString(result) mustEqual view(
            form,
            viewmodel(postCall),
            None
          )(fakeRequest, messages).toString

      }
    }

    "redirect to next page on POST request" which {
      "saves partner address" in {

        val onwardCall = routes.PartnerEmailController.onPageLoad(NormalMode, establisherIndex, partnerIndex, None)
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

          val controller = app.injector.instanceOf[PartnerPreviousAddressController]

          val result = controller.onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

          status(result) must be(SEE_OTHER)
          redirectLocation(result).value mustEqual onwardCall.url

          FakeUserAnswersService.userAnswer.get(PartnerPreviousAddressId(establisherIndex, partnerIndex)).value mustEqual address
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

        val controller = app.injector.instanceOf[PartnerPreviousAddressController]

        val result = controller.onSubmit(NormalMode, establisherIndex, partnerIndex, None)(postRequest)

        fakeAuditService.reset()

        whenReady(result) {
          _ =>
            fakeAuditService.verifySent(
              AddressEvent(
                FakeAuthAction.externalId,
                AddressAction.LookupChanged,
                s"Partnership Partners Previous Address: ${partnerDetails.fullName}",
                address
              )
            )
        }

      }
    }
  }
}
