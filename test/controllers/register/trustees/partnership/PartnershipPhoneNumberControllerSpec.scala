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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import forms.PhoneFormProvider
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode, PartnershipDetails}
import navigators.Navigator
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phoneNumber

class PartnershipPhoneNumberControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new PhoneFormProvider()
  private val form: Form[String] = formProvider()
  private val firstIndex = Index(0)
  private val phone = "1234"
  private val trusteePartnershipDetails = PartnershipDetails("test partnership")
  private val schemeName = "Scheme Name"

  private val fullAnswers =
    UserAnswers()
      .trusteePartnershipDetails(firstIndex, trusteePartnershipDetails)
      .schemeName(schemeName)

  private val view = injector.instanceOf[phoneNumber]

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      CommonFormWithHintViewModel(
        routes.PartnershipPhoneNumberController.onSubmit(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber),
        Message("messages__enterPhoneNumber", Message("messages__thePartnership").resolve),
        Message("messages__enterPhoneNumber", trusteePartnershipDetails.name),
        Some(Message("messages__contact_phone__hint", trusteePartnershipDetails.name, schemeName)),
        EmptyOptionalSchemeReferenceNumber
      ),
      Some(schemeName)
    )(fakeRequest, messages).toString

  "PartnershipPhoneNumberController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        running(_.overrides(modules(fullAnswers.dataRetrievalAction): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipPhoneNumberController]
            val result = controller.onPageLoad(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

            status(result) mustBe OK
            contentAsString(result) mustBe viewAsString()
        }
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        running(_.overrides(
          modules(fullAnswers.dataRetrievalAction) ++
            Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[UserAnswersService].toInstance(FakeUserAnswersService)
            ): _*)) {
          app =>
            val controller = app.injector.instanceOf[PartnershipPhoneNumberController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("phone", phone))
            val result = controller.onSubmit(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }
    }
  }
}
