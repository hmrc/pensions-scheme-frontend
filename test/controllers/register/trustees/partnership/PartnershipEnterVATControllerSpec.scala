/*
 * Copyright 2022 HM Revenue & Customs
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
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.EnterVATFormProvider
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{EnterVATViewModel, Message}
import views.html.enterVATView

class PartnershipEnterVATControllerSpec extends ControllerSpecBase with Matchers {

  import PartnershipEnterVATControllerSpec._

  private val view = injector.instanceOf[enterVATView]

  "PartnershipEnterVATController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteePartnership),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService)
      )) {
        app =>
          val controller = app.injector.instanceOf[PartnershipEnterVATController]
          val request = addCSRFToken(FakeRequest())
          val result = controller.onPageLoad(NormalMode, firstIndex, None)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(request, messages).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteePartnership),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService)
      )) {
        app =>
          val controller = app.injector.instanceOf[PartnershipEnterVATController]
          val request = addCSRFToken(FakeRequest()
            .withFormUrlEncodedBody(("vat", "123456789")))
          val result = controller.onSubmit(NormalMode, firstIndex, None)(request)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object PartnershipEnterVATControllerSpec extends PartnershipEnterVATControllerSpec {

  val form = new EnterVATFormProvider()("test partnership")
  val firstIndex: Index = Index(0)

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel: EnterVATViewModel = EnterVATViewModel(
    routes.PartnershipEnterVATController.onSubmit(NormalMode, firstIndex, None),
    title = Message("messages__enterVAT", Message("messages__thePartnership").resolve),
    heading = Message("messages__enterVAT", "test partnership name"),
    hint = Message("messages__enterVAT__hint", "test partnership name"),
    subHeading = None
  )
}


