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
import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import forms.CompanyRegistrationNumberVariationsFormProvider
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
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.html.register.companyRegistrationNumberVariations

import scala.concurrent.Future

class CompanyEnterCRNControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import CompanyEnterCRNControllerSpec._

  val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "CompanyEnterCRNControllerSpec$" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyEnterCRNController.onPageLoad(CheckUpdateMode, srn, firstIndex))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe
            companyRegistrationNumberVariations(
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

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.CompanyEnterCRNController.onSubmit(CheckUpdateMode, srn, firstIndex))
          .withFormUrlEncodedBody(("companyRegistrationNumber", "1234567"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }

}

object CompanyEnterCRNControllerSpec extends CompanyEnterCRNControllerSpec {

  val companyName = "test company name"
  val form = new CompanyRegistrationNumberVariationsFormProvider()(companyName)
  val firstIndex = Index(0)
  val srn = Some("S123")

  def viewModel(companyName: String = companyName): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__enterCRN", Message("messages__theCompany").resolve),
      heading = Message("messages__enterCRN", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  val postCall = routes.CompanyEnterCRNController.onSubmit _

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getMandatoryTrusteeCompany),
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
