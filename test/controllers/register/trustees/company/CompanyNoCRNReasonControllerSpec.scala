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

package controllers.register.trustees.company

import base.CSRFRequest
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.register.NoCompanyNumberFormProvider
import models.{Index, NormalMode}
import navigators.Navigator
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.TrusteesCompany
import utils.FakeNavigator
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

import scala.concurrent.Future

class CompanyNoCRNReasonControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import CompanyNoCRNReasonControllerSpec._
  "CompanyNoCRNReasonController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(controllers.register.trustees.company.routes.CompanyNoCRNReasonController.onPageLoad(NormalMode, firstIndex, None))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe reason(frontendAppConfig, form, viewModel, None)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(controllers.register.trustees.company.routes.CompanyNoCRNReasonController.onSubmit(NormalMode, firstIndex, None))
          .withFormUrlEncodedBody(("reason", "blaa"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

  }

}

object CompanyNoCRNReasonControllerSpec extends CompanyNoCRNReasonControllerSpec {

  val form = new NoCompanyNumberFormProvider()("test company name")
  val firstIndex = Index(0)

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val viewModel = ReasonViewModel(
    postCall = controllers.register.trustees.company.routes.CompanyNoCRNReasonController.onSubmit(NormalMode, firstIndex, None),
    title = Message("messages__whyNoCRN", Message("messages__theCompany").resolve),
    heading = Message("messages__whyNoCRN", "test company name"),
    srn = None
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryTrusteeCompany),
      bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersService].toInstance(FakeUserAnswersService)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}




