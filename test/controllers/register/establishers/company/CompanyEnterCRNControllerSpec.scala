/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.CompanyRegistrationNumberFormProvider
import identifiers.register.establishers.company.CompanyEnterCRNId
import models._
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.annotations.EstablishersCompany
import utils.{FakeNavigator, _}
import viewmodels.{CompanyRegistrationNumberViewModel, Message}
import views.html.register.companyRegistrationNumber

class CompanyEnterCRNControllerSpec extends ControllerSpecBase with Matchers {

  import CompanyEnterCRNControllerSpec._

  private val view = app.injector.instanceOf[companyRegistrationNumber]

  "CompanyEnterCRNControllerSpec" must {

    "render the view correctly on a GET request when there is no existing answer" in {
      running(_.overrides(modules(getMandatoryEstablisherCompany):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterCRNController]
          val result = controller.onPageLoad(CheckUpdateMode, srn, index = 0)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(viewModel(), form, None, postCall(CheckUpdateMode, srn, firstIndex),
              srn)(fakeRequest, messages).toString
      }
    }

    "render the view correctly on a GET request when there is an existing answer" in {
      val data = UserAnswers().establisherCompanyDetails(0, CompanyDetails("test company name")).
        set(CompanyEnterCRNId(0))(value = ReferenceValue("1234567")).asOpt.getOrElse(UserAnswers()).dataRetrievalAction
      running(_.overrides(modules(data):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterCRNController]
          val result = controller.onPageLoad(CheckUpdateMode, srn, index = 0)(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(viewModel(), form.fill(ReferenceValue("1234567")), None, postCall(CheckUpdateMode, srn, firstIndex),
            srn)(fakeRequest, messages).toString
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompany)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterCRNController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("companyRegistrationNumber", "1234567"))
          val result = controller.onSubmit(NormalMode, None, index = 0)(postRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "return BAD REQUEST for invalid POST request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompany)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterCRNController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("companyRegistrationNumber", "123456{0"))
          val result = controller.onSubmit(NormalMode, None, index = 0)(postRequest)
          status(result) mustBe BAD_REQUEST
      }
    }
  }
}

object CompanyEnterCRNControllerSpec extends CompanyEnterCRNControllerSpec {

  val companyName = "test company name"
  val form = new CompanyRegistrationNumberFormProvider()(companyName)
  val firstIndex = Index(0)
  val srn = Some("S123")

  def viewModel(companyName: String = companyName): CompanyRegistrationNumberViewModel = {
    CompanyRegistrationNumberViewModel(
      title = Message("messages__enterCRN", Message("messages__theCompany").resolve),
      heading = Message("messages__enterCRN", companyName),
      hint = Message("messages__common__crn_hint", companyName)
    )
  }

  private val postCall = routes.CompanyEnterCRNController.onSubmit _

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
}
