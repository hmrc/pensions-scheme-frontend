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
import forms.PayeFormProvider
import models._
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import utils.annotations.EstablishersCompany
import viewmodels.{Message, PayeViewModel}
import views.html.paye

class CompanyEnterPAYEControllerSpec extends ControllerSpecBase with Matchers {

  import CompanyEnterPAYEControllerSpec._

  private val view = app.injector.instanceOf[paye]

  "CompanyEnterPAYEController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompany): _*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterPAYEController]
          val result = controller.onPageLoad(CheckUpdateMode, index = 0, OptionalSchemeReferenceNumber(srn))(fakeRequest)
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel, None)(fakeRequest, messages).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getMandatoryEstablisherCompany) ++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersService].toInstance(FakeUserAnswersService)
        ): _*)) {
        app =>
          val controller = app.injector.instanceOf[CompanyEnterPAYEController]
          val postRequest = fakeRequest.withFormUrlEncodedBody(("paye", "123456789"))
          val result = controller.onSubmit(NormalMode, index = 0, EmptyOptionalSchemeReferenceNumber)(postRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

object CompanyEnterPAYEControllerSpec extends CompanyEnterPAYEControllerSpec {

  val form = new PayeFormProvider()("test company name")
  val firstIndex = Index(0)
  val srn: OptionalSchemeReferenceNumber = OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("S123")))

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel = PayeViewModel(
    routes.CompanyEnterPAYEController.onSubmit(CheckUpdateMode, firstIndex, OptionalSchemeReferenceNumber(srn)),
    title = Message("messages__enterPAYE", Message("messages__theCompany")),
    heading = Message("messages__enterPAYE", "test company name"),
    hint = Some(Message("messages__enterPAYE__hint")),
    srn = OptionalSchemeReferenceNumber(srn),
    entityName = Some("test company name")
  )

}








