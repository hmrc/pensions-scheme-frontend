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

package controllers.register.establishers.company

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions._
import forms.UTRFormProvider
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
import utils.annotations.EstablishersCompany
import utils.FakeNavigator
import viewmodels.{Message, UTRViewModel}
import views.html.utr

import scala.concurrent.Future

class CompanyEnterUTRControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import CompanyEnterUTRControllerSpec._

  "CompanyEnterUTRController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyEnterUTRController.onPageLoad(CheckUpdateMode, srn, firstIndex))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe utr(frontendAppConfig, form, viewModel, Some("pension scheme details"))(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyEnterUTRController.onSubmit(CheckUpdateMode, srn, firstIndex))
          .withFormUrlEncodedBody(("utr", "1234567890"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }
}



object CompanyEnterUTRControllerSpec extends CompanyEnterUTRControllerSpec {

  val form = new UTRFormProvider()()
  val firstIndex = Index(0)
  val srn = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = UTRViewModel(
    routes.CompanyEnterUTRController.onSubmit(CheckUpdateMode, srn, firstIndex),
    title = Message("messages__enterUTR", Message("messages__theCompany").resolve),
    heading = Message("messages__enterUTR", "test company name"),
    hint = Message("messages_utr__hint"),
    srn = srn
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryEstablisherCompany),
      bind(classOf[Navigator]).qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
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








