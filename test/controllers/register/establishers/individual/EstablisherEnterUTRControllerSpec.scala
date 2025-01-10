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
import forms.UTRFormProvider
import models.{CheckUpdateMode, Index, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class EstablisherEnterUTRControllerSpec extends ControllerSpecBase with Matchers {

  import EstablisherEnterUTRControllerSpec._

  "EstablisherEnterUTRController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(modules(getMandatoryEstablisherIndividual): _*)) {
        app =>
          val controller = app.injector.instanceOf[EstablisherEnterUTRController]
          val result = controller.onPageLoad(CheckUpdateMode, firstIndex, OptionalSchemeReferenceNumber(srn))(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(fakeRequest, messages).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(
        modules(getMandatoryEstablisherIndividual) ++
          Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(FakeUserAnswersService)
          ): _*)) {
        app =>
          val controller = app.injector.instanceOf[EstablisherEnterUTRController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", "1234567890"))
          val result = controller.onSubmit(CheckUpdateMode, firstIndex, OptionalSchemeReferenceNumber(srn))(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object EstablisherEnterUTRControllerSpec extends EstablisherEnterUTRControllerSpec {

  val form = new UTRFormProvider()()
  val firstIndex = Index(0)
  val srn: OptionalSchemeReferenceNumber = OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("S123")))

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val view = injector.instanceOf[utr]

  val viewModel = UTRViewModel(
    routes.EstablisherEnterUTRController.onSubmit(CheckUpdateMode, firstIndex, OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__enterUTR", Message("messages__theIndividual")),
    heading = Message("messages__enterUTR", "Test Name"),
    hint = Message("messages_utr__hint"),
    srn = OptionalSchemeReferenceNumber(srn)
  )
}
