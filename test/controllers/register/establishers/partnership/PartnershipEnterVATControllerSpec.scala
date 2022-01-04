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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import forms.EnterVATFormProvider
import models.{CheckUpdateMode, Index}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{EnterVATViewModel, Message}
import views.html.enterVATView

class PartnershipEnterVATControllerSpec extends ControllerSpecBase with Matchers {

  import PartnershipEnterVATControllerSpec._

  "PartnershipEnterVATController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(modules(getMandatoryEstablisherPartnership):_*)) {
        app =>
          val controller = app.injector.instanceOf[PartnershipEnterVATController]
          val result = controller.onPageLoad(CheckUpdateMode, index = 0, srn)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(fakeRequest, messages).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getMandatoryEstablisherPartnership)++
        Seq[GuiceableModule](bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[PartnershipEnterVATController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("vat", "123456789"))
          val result = controller.onSubmit(CheckUpdateMode, index = 0, None)(postRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object PartnershipEnterVATControllerSpec extends PartnershipEnterVATControllerSpec {
  private val partnershipName = "test partnership name"
  val form = new EnterVATFormProvider()(partnershipName)
  val firstIndex = Index(0)
  val srn = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel = EnterVATViewModel(
    routes.PartnershipEnterVATController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__enterVAT", Message("messages__thePartnership").resolve),
    heading = Message("messages__enterVAT", partnershipName),
    hint = Message("messages__enterVAT__hint", partnershipName),
    subHeading = None,
    srn = srn
  )

  private val view = injector.instanceOf[enterVATView]
}






