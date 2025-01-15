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

package controllers.register.trustees.company

import config.FrontendAppConfig
import controllers.ControllerSpecBase
import controllers.actions._
import forms.CompanyRegistrationNumberFormProvider
import models.{CheckUpdateMode, Index, Mode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.html.register.companyRegistrationNumber

class CompanyEnterCRNControllerSpec extends ControllerSpecBase with Matchers {

  import CompanyEnterCRNControllerSpec._

  val appConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  private val view = injector.instanceOf[companyRegistrationNumber]

  "CompanyEnterCRNControllerSpec$" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteeCompany),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())
      )) {
        app =>
        val request = addCSRFToken(FakeRequest())
        val controller = app.injector.instanceOf[CompanyEnterCRNController]
        val result = controller.onPageLoad(CheckUpdateMode, OptionalSchemeReferenceNumber(srn), firstIndex)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe
            view(
              viewModel(),
              form,
              None,
              postCall(CheckUpdateMode, OptionalSchemeReferenceNumber(srn), firstIndex),
              srn
            )(request, messages).toString

        }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteeCompany),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())
      )) {
        app =>
        val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("companyRegistrationNumber", "1234567")))
        val controller = app.injector.instanceOf[CompanyEnterCRNController]
        val result = controller.onSubmit(CheckUpdateMode, OptionalSchemeReferenceNumber(srn), firstIndex)(request)

        status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
    }
  }

}

object CompanyEnterCRNControllerSpec extends CompanyEnterCRNControllerSpec {

  val companyName = "test company name"
  val form = new CompanyRegistrationNumberFormProvider()(companyName)
  val firstIndex: Index = Index(0)
  val srn: OptionalSchemeReferenceNumber = OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("S123")))

  def viewModel(companyName: String = companyName): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__enterCRN", Message("messages__theCompany").resolve),
      heading = Message("messages__enterCRN", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  val postCall: (Mode, OptionalSchemeReferenceNumber, Index) => Call = routes.CompanyEnterCRNController.onSubmit _

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

}
