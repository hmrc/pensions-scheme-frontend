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

package controllers.register.establishers.partnership

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions._
import forms.EnterVATFormProvider
import models.{CheckUpdateMode, Index}
import navigators.Navigator
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{EnterVATViewModel, Message}
import views.html.enterVATView

import scala.concurrent.Future

class PartnershipEnterVATControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipEnterVATControllerSpec._

  "PartnershipEnterVATController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipEnterVATController.onPageLoad(CheckUpdateMode, firstIndex, srn))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe enterVATView(frontendAppConfig, form, viewModel, Some("pension scheme details"))(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipEnterVATController.onSubmit(CheckUpdateMode, firstIndex, srn))
          .withFormUrlEncodedBody(("vat", "123456789"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }
}

object PartnershipEnterVATControllerSpec extends PartnershipEnterVATControllerSpec {
  private val partnershipName = "test partnership name"
  val form = new EnterVATFormProvider()(partnershipName)
  val firstIndex = Index(0)
  val srn = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = EnterVATViewModel(
    routes.PartnershipEnterVATController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__enterVAT__partnership_title"),
    heading = Message("messages__enterVAT__heading", partnershipName),
    hint = Message("messages__enterVAT__hint", partnershipName),
    subHeading = None,
    srn = srn
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryEstablisherPartnership),
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






