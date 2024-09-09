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
import controllers.actions._
import forms.PayeFormProvider
import models.{CheckUpdateMode, Index, NormalMode}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{Message, PayeViewModel}
import views.html.paye

class PartnershipEnterPAYEControllerSpec extends ControllerSpecBase with Matchers {

  import PartnershipEnterPAYEControllerSpec._

  private val view = injector.instanceOf[paye]

  "PartnershipEnterPAYEController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteePartnership),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn))

      )) {
        app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[PartnershipEnterPAYEController]
          val result = controller.onPageLoad(CheckUpdateMode, firstIndex, srn)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(request, messages).toString()
        }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteePartnership),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn))

      )) {
        app =>
          val request =
            addCSRFToken(FakeRequest().withFormUrlEncodedBody(("paye", "123456789")))
          val controller = app.injector.instanceOf[PartnershipEnterPAYEController]
          val result = controller.onSubmit(NormalMode, Index(0), srn)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
    }

  }

}

object PartnershipEnterPAYEControllerSpec extends PartnershipEnterPAYEControllerSpec {

  val form = new PayeFormProvider()("test partnership name")
  val firstIndex = Index(0)

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel = PayeViewModel(
    routes.PartnershipEnterPAYEController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__enterPAYE", Message("messages__thePartnership").resolve),
    heading = Message("messages__enterPAYE", "test partnership name"),
    hint = Some(Message("messages__enterPAYE__hint")),
    srn = srn,
    entityName = Some("test partnership name")
  )

}








