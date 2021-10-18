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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.UTRFormProvider
import models.{CheckUpdateMode, Index}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class TrusteeEnterUTRControllerSpec extends ControllerSpecBase with Matchers {

  import TrusteeEnterUTRControllerSpec._

  private val view = injector.instanceOf[utr]

  "TrusteeEnterUTRController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrustee),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())
      )) {
        implicit app =>
        val request = addCSRFToken(FakeRequest())
        val controller = app.injector.instanceOf[TrusteeEnterUTRController]
        val result = controller.onPageLoad(CheckUpdateMode, firstIndex, srn)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(request, messages).toString()
        }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrustee),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())
      )) {
        implicit app =>
        val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("utr", "1234567890")))
        val controller = app.injector.instanceOf[TrusteeEnterUTRController]
        val result = controller.onSubmit(CheckUpdateMode, firstIndex, srn)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
    }
  }
}

object TrusteeEnterUTRControllerSpec extends TrusteeEnterUTRControllerSpec {

  val form = new UTRFormProvider()()
  val firstIndex: Index = Index(0)
  val srn: Option[String] = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel: UTRViewModel = UTRViewModel(
    routes.TrusteeEnterUTRController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__enterUTR", Message("messages__theIndividual").resolve),
    heading = Message("messages__enterUTR", "Test Name"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )
}












