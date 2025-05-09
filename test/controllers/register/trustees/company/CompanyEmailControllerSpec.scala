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
import forms.EmailFormProvider
import identifiers.SchemeNameId
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.company.CompanyDetailsId
import models.{CompanyDetails, EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.emailAddress

class CompanyEmailControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new EmailFormProvider()
  val form: Form[String] = formProvider()
  val firstIndex = Index(0)
  private val schemeName = "Scheme Name"

  private def getMandatoryTrusteeCompanyDetails: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(
      TrusteesId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name")
        )
      ),
      SchemeNameId.toString -> schemeName
    ))
  )

  private val view = injector.instanceOf[emailAddress]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrusteeCompanyDetails): CompanyEmailController =
    new CompanyEmailController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeUserAnswersService,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      new FakeNavigator(desiredRoute = onwardRoute),
      formProvider,
      controllerComponents,
      view
    )

  def viewAsString(form: Form[?] = form): String =
    view(
      form,
      CommonFormWithHintViewModel(
        routes.CompanyEmailController.onSubmit(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber),
        Message("messages__trustee_email__title"),
        Message("messages__enterEmail", "test company name"),
        Some(Message("messages__contact_email__hint", "test company name", schemeName)),
        EmptyOptionalSchemeReferenceNumber
      ),
      Some(schemeName)
    )(fakeRequest, messages).toString

  "CompanyEmailController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to relevant page" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("email", "test@test.com"))
        val result = controller().onSubmit(NormalMode, firstIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

