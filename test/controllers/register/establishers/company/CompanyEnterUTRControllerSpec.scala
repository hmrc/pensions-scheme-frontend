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
import forms.UTRFormProvider
import models.{CheckUpdateMode, EmptyOptionalSchemeReferenceNumber, Index, NormalMode, OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import navigators.Navigator
import org.scalatest.matchers.must.Matchers
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, status, _}
import services.{FakeUserAnswersService, UserAnswersService}
import utils.FakeNavigator
import utils.annotations.EstablishersCompany
import viewmodels.{Message, UTRViewModel}
import views.html.utr

class CompanyEnterUTRControllerSpec extends ControllerSpecBase with Matchers {

  import CompanyEnterUTRControllerSpec._

  "render the view correctly on a GET request" in {
    running(_.overrides(modules(getMandatoryEstablisherCompany): _*)) {
      app =>
        val controller = app.injector.instanceOf[CompanyEnterUTRController]
        val result = controller.onPageLoad(CheckUpdateMode, OptionalSchemeReferenceNumber(srn), index = 0)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe view(form, viewModel, Some("pension scheme details"))(fakeRequest, messages).toString()
    }
  }

  "redirect to the next page on a POST request" in {
    running(_.overrides(modules(getMandatoryEstablisherCompany) ++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[EstablishersCompany]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersService].toInstance(FakeUserAnswersService)
      ): _*)) {
      app =>
        val controller = app.injector.instanceOf[CompanyEnterUTRController]
        val postRequest = fakeRequest.withFormUrlEncodedBody(("utr", "1234567890"))
        val result = controller.onSubmit(NormalMode, EmptyOptionalSchemeReferenceNumber, index = 0)(postRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}


object CompanyEnterUTRControllerSpec extends CompanyEnterUTRControllerSpec {

  val form = new UTRFormProvider()()
  val firstIndex = Index(0)
  val srn = Some(SchemeReferenceNumber("S123"))

  private val view = injector.instanceOf[utr]

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val viewModel = UTRViewModel(
    routes.CompanyEnterUTRController.onSubmit(CheckUpdateMode, OptionalSchemeReferenceNumber(srn), firstIndex),
    title = Message("messages__enterUTR", Message("messages__theCompany").resolve),
    heading = Message("messages__enterUTR", "test company name"),
    hint = Message("messages_utr__hint"),
    srn = OptionalSchemeReferenceNumber(srn)
  )
}








