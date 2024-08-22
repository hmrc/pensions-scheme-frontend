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

import controllers.ControllerSpecBase
import controllers.actions._
import forms.PayeFormProvider
import models.{CheckUpdateMode, Index}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import viewmodels.{Message, PayeViewModel}
import views.html.paye

class CompanyEnterPAYEControllerSpec extends ControllerSpecBase with Matchers {

  import CompanyEnterPAYEControllerSpec._

  private val view = injector.instanceOf[paye]

  "CompanyEnterPAYEController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(
        bind[AuthAction].to(FakeAuthAction),
        bind[DataRetrievalAction].toInstance(getMandatoryTrusteeCompany),
        bind(classOf[Navigator]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService),
        bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider())

      )) {
        implicit app =>
          val request = addCSRFToken(FakeRequest())
          val controller = app.injector.instanceOf[CompanyEnterPAYEController]
          val result = controller.onPageLoad(CheckUpdateMode, firstIndex, srn)(request)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(request, messages).toString()
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
        implicit app =>
          val request = addCSRFToken(FakeRequest().withFormUrlEncodedBody(("paye", "123456789")))
          val controller = app.injector.instanceOf[CompanyEnterPAYEController]
          val result = controller.onSubmit(CheckUpdateMode, firstIndex, srn)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
    }

  }

}

object CompanyEnterPAYEControllerSpec extends CompanyEnterPAYEControllerSpec{

  val form = new PayeFormProvider()("test company name")
  val firstIndex: Index = Index(0)
  val srn: SchemeReferenceNumber = Some("S123")

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel: PayeViewModel = PayeViewModel(
    routes.CompanyEnterPAYEController.onSubmit(CheckUpdateMode, firstIndex, srn),
    title = Message("messages__enterPAYE", Message("messages__theCompany").resolve),
    heading = Message("messages__enterPAYE", "test company name"),
    hint = Some(Message("messages__enterPAYE__hint")),
    srn = srn,
    entityName = Some("test company name")
  )

}








