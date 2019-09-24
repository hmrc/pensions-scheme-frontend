/*
 * Copyright 2019 HM Revenue & Customs
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

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions._
import forms.PayeVariationsFormProvider
import models.{CheckUpdateMode, Index}
import navigators.Navigator
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.TrusteesPartnership
import utils.FakeNavigator
import viewmodels.{Message, PayeViewModel}
import views.html.payeVariations

import scala.concurrent.Future

class PartnershipEnterPAYEControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipEnterPAYEControllerSpec._

  "PartnershipEnterPAYEController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipEnterPAYEController.onPageLoad(CheckUpdateMode, firstIndex, srn))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe payeVariations(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipEnterPAYEController.onSubmit(CheckUpdateMode, firstIndex, srn))
          .withFormUrlEncodedBody(("paye", "123456789"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

  }

}

object PartnershipEnterPAYEControllerSpec extends PartnershipEnterPAYEControllerSpec {

  val form = new PayeVariationsFormProvider()("test partnership name")
  val firstIndex = Index(0)
  val srn = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = PayeViewModel(
    routes.PartnershipEnterPAYEController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__payeVariations__partnership_title"),
    heading = Message("messages__payeVariations__heading", "test partnership name"),
    hint = Some(Message("messages__payeVariations__hint")),
    srn = srn,
    entityName = Some("test partnership name")
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryTrusteePartnership),
      bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersService].toInstance(FakeUserAnswersService),
      bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())

    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}








