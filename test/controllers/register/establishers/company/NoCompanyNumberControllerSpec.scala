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
import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.company.NoCompanyNumberFormProvider
import models.{CheckUpdateMode, Index}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.EstablishersCompany
import utils.{FakeNavigator, Navigator}
import viewmodels.{Message, NoCompanyNumberViewModel}
import views.html.register.establishers.company.noCompanyNumber

import scala.concurrent.Future

class NoCompanyNumberControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import NoCompanyNumberControllerSpec._

  val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "NoCompanyNumberController" when {

    "on a GET" must {

      "render the view correctly" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.NoCompanyNumberController.onPageLoad(CheckUpdateMode, srn, firstIndex))),
          (request, result) => {
            status(result) mustBe OK
            contentAsString(result) mustBe
              noCompanyNumber(
                appConfig,
                viewModel(),
                form,
                None,
                postCall(CheckUpdateMode, srn, firstIndex),
                srn
              )(request, messages).toString

          }
        )
      }
    }

    "on a POST" must {

      "return BadRequest when validation fails" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.NoCompanyNumberController.onSubmit(CheckUpdateMode, srn, firstIndex))
            .withFormUrlEncodedBody(("reason", ""))),
          (_, result) => {
            status(result) mustBe BAD_REQUEST
          }
        )
      }

      "redirect to the next page on a successful POST request" in {
        requestResult(
          implicit app => addToken(FakeRequest(routes.NoCompanyNumberController.onSubmit(CheckUpdateMode, srn, firstIndex))
            .withFormUrlEncodedBody(("reason", "no reason"))),
          (_, result) => {
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
          }
        )
      }
    }

  }

}


object NoCompanyNumberControllerSpec extends NoCompanyNumberControllerSpec {

  val companyName = "test company name"
  val form = new NoCompanyNumberFormProvider()(companyName)
  val firstIndex = Index(0)
  val srn = Some("S123")

  def onwardRoute: Call = controllers.routes.SessionExpiredController.onPageLoad

  val postCall = routes.NoCompanyNumberController.onSubmit _

  def viewModel(companyName: String = companyName): NoCompanyNumberViewModel = {
    NoCompanyNumberViewModel(
      title = Message("messages__noCompanyNumber__establisher__title"),
      heading = Message("messages__noCompanyNumber__establisher__heading", companyName)
    )
  }

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

