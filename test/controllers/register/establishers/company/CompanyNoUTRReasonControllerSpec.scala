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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions.FakeDataRetrievalAction
import forms.ReasonFormProvider
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.{CompanyDetailsId, CompanyNoUTRReasonId}
import models.{CompanyDetails, NormalMode}
import navigators.Navigator
import play.api.data.Form
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.EstablishersCompany
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{Message, ReasonViewModel}
import views.html.reason

class CompanyNoUTRReasonControllerSpec extends ControllerSpecBase {

  import CompanyNoUTRReasonControllerSpec._

  private val formProvider = new ReasonFormProvider()
  private val form: Form[String] = formProvider("", companyName)

  private val view = injector.instanceOf[reason]

  val viewmodel = ReasonViewModel(
    postCall = routes.CompanyNoUTRReasonController.onSubmit(NormalMode, srn, index = 0),
    title = Message("messages__whyNoUTR", Message("messages__theCompany").resolve),
    heading = Message("messages__whyNoUTR", companyName),
    srn = srn
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad

  "CompanyNoUTRReasonController" when {
    "on a GET" must {
      "render the view correctly when there is no existing answer" in {
        running(_.overrides(modules(getMandatoryEstablisherCompany):_*)) {
          app =>
            val controller = app.injector.instanceOf[CompanyNoUTRReasonController]
            val result = controller.onPageLoad(NormalMode, srn, index = 0)(fakeRequest)
            status(result) mustBe OK
            contentAsString(result) mustBe view(form, viewmodel, None)(fakeRequest, messages).toString
        }
      }

      "render the view correctly when there is a existing answer" in {
        val data = new FakeDataRetrievalAction(Some(UserAnswers().set(CompanyDetailsId(0))(CompanyDetails("test company name")).flatMap(
          _.set(CompanyNoUTRReasonId(0))("no reason")).asOpt.getOrElse(UserAnswers()).json))
        running(_.overrides(modules(data):_*)) {
          app =>
            val controller = app.injector.instanceOf[CompanyNoUTRReasonController]
            val result = controller.onPageLoad(NormalMode, srn, index = 0)(fakeRequest)
            status(result) mustBe OK
            contentAsString(result) mustBe view(form.fill(value = "no reason"), viewmodel, None)(fakeRequest, messages).toString
        }
      }
    }

    "on a POST" must {
      "redirect to the next page on a POST request" in {
        running(_.overrides(modules(getMandatoryEstablisherCompany)++
          Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(FakeUserAnswersService)
          ):_*)) {
          app =>
            val controller = app.injector.instanceOf[CompanyNoUTRReasonController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "123456789"))
            val result = controller.onSubmit(NormalMode, srn, index = 0)(postRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }

      "return a bad request when the submitted data is invalid" in {
        running(_.overrides(modules(getMandatoryEstablisherCompany)++
          Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
            bind[UserAnswersService].toInstance(FakeUserAnswersService)
          ):_*)) {
          app =>
            val controller = app.injector.instanceOf[CompanyNoUTRReasonController]
            val postRequest = fakeRequest.withFormUrlEncodedBody(("reason", "1234567{0}"))
            val result = controller.onSubmit(NormalMode, srn, index = 0)(postRequest)
            status(result) mustBe BAD_REQUEST
        }
      }
    }
  }
}


object CompanyNoUTRReasonControllerSpec {

  object FakeIdentifier extends TypedIdentifier[String]

  val companyName = "test company name"
  val errorKey = "messages__reason__error_utrRequired"
}
