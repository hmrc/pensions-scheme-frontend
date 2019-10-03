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

package controllers.register.establishers.partnership.partner

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions._
import forms.UTRFormProvider
import models.{Index, NormalMode}
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
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.Future

class PartnerEnterUTRControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnerEnterUTRControllerSpec._

  "PartnerEnterUTRController" must {
    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnerEnterUTRController.onPageLoad(NormalMode, establisherIndex, partnerIndex, srn))),
        (request, result) => {

          val expected = utr(frontendAppConfig, form, viewModel, Some("pension scheme details"))(request, messages).toString()
          status(result) mustBe OK
          contentAsString(result) mustBe expected
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnerEnterUTRController.onSubmit(NormalMode, establisherIndex, partnerIndex, srn))
          .withFormUrlEncodedBody(("utr", "1234567890"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }
}

object PartnerEnterUTRControllerSpec extends PartnerEnterUTRControllerSpec {
  val establisherIndex = Index(0)
  val partnerIndex = Index(0)
  val form = new UTRFormProvider()()
  val firstIndex = Index(0)
  val srn = None

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = UTRViewModel(
    routes.PartnerEnterUTRController.onSubmit(NormalMode, establisherIndex, partnerIndex, srn),
    title = Message("messages__enterUTR", Message("messages__thePartner").resolve),
    heading = Message("messages__enterUTR", "first last"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryPartner),
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
