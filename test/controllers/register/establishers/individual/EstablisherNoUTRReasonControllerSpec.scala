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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions.FakeDataRetrievalAction
import forms.ReasonFormProvider
import identifiers.register.establishers.individual.EstablisherNameId
import models.person.PersonName
import models.{EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import navigators.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.Future

class EstablisherNoUTRReasonControllerSpec extends ControllerSpecBase with MockitoSugar {

  val name = "Test Name"
  private val formProvider = new ReasonFormProvider()
  private val form = formProvider("messages__reason__error_utrRequired", name)
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val postCall = controllers.register.establishers.individual.routes.EstablisherNoUTRReasonController.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)
  private val viewModel = ReasonViewModel(
    postCall = postCall,
    title = Message("messages__whyNoUTR", Message("messages__theIndividual")),
    heading = Message("messages__whyNoUTR", name),
    srn = EmptyOptionalSchemeReferenceNumber
  )
  private val view = injector.instanceOf[reason]

  private def viewAsString(form: Form[_] = form): String = view(form, viewModel, None)(fakeRequest, messages).toString

  "EstablisherNoUTRReasonController" must {

    "return OK and the correct view for a GET" in {
      val app = applicationBuilder(getMandatoryEstablisherIndividual).build()

      val controller = app.injector.instanceOf[EstablisherNoUTRReasonController]

      val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString()

      app.stop()
    }

    "return OK and the correct view for a GET where valid reason given" in {
      val establisherDataWithNoUTRReasonAnswer = new FakeDataRetrievalAction(Some(validEstablisherIndividualData("noUtrReason" -> "blah")))

      val app = applicationBuilder(establisherDataWithNoUTRReasonAnswer).build()

      val controller = app.injector.instanceOf[EstablisherNoUTRReasonController]

      val result = controller.onPageLoad( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe viewAsString(form = form.fill(value = "blah"))

      app.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val app = applicationBuilder(getMandatoryEstablisherIndividual)
        .overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService),
          bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute))
        ).build()

      val validData = Json.obj(
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherNameId.toString ->
              PersonName("Test", "Name")
          )
        )
      )

      when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))

      val controller = app.injector.instanceOf[EstablisherNoUTRReasonController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "blah"))

      val result = controller.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }

    "return a Bad Request when invalid data is submitted" in {
      val app = applicationBuilder(getMandatoryEstablisherIndividual).build()

      val controller = app.injector.instanceOf[EstablisherNoUTRReasonController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("noUtrReason", ""))

      val boundForm = form.bind(Map("noUtrReason" -> ""))

      val result = controller.onSubmit( NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)

      app.stop()
    }

    "return a Bad Request with errors when invalid chars are submitted" in {
      val app = applicationBuilder(getMandatoryEstablisherIndividual).build()

      val controller = app.injector.instanceOf[EstablisherNoUTRReasonController]

      val postRequest = fakeRequest.withFormUrlEncodedBody(("noUtrReason", "<>?:-{}<>,/.,/;#\";]["))

      val boundForm = form.bind(Map("noUtrReason" -> "<>?:-{}<>,/.,/;#\";]["))

      val result = controller.onSubmit(NormalMode, Index(0), EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)
      contentAsString(result) must include(messages("messages__reason__error_utrRequired", name))
      app.stop()
    }
  }
}
