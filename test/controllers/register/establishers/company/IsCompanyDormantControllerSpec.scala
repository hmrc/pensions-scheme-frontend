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
import controllers.actions._
import forms.register.establishers.IsDormantFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import models.register.DeclarationDormant
import models.{CompanyDetails, NormalMode}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.register.establishers.isDormant

class IsCompanyDormantControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val formProvider = new IsDormantFormProvider()
  val form = formProvider()
  private val view = injector.instanceOf[isDormant]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): IsCompanyDormantController =
    new IsCompanyDormantController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  val index = 0
  val companyName = "test company name"
  def postCall: Call = routes.IsCompanyDormantController.onSubmit(NormalMode, srn, index)

  val validData: JsObject = Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString ->
            CompanyDetails("test company name"),
          IsCompanyDormantId.toString -> DeclarationDormant.values.head.toString
        )
      )
    )

  def viewAsString(form: Form[_] = form): String = view(form, companyName, postCall, None, srn)(fakeRequest, messages).toString

  "IsCompanyDormant Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, srn, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val result = controller(new FakeDataRetrievalAction(Some(validData))).onPageLoad(NormalMode, srn, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(DeclarationDormant.values.head))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationDormant.options.head.value))

      val result = controller().onSubmit(NormalMode, srn, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit(NormalMode, srn, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode, srn, index)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationDormant.options.head.value))
          val result = controller(dontGetAnyData).onSubmit(NormalMode, srn, index)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
      "scheme details cannot be retrieved" when {
        "GET" in {
          val result = controller(getEmptyData).onPageLoad(NormalMode, srn, index)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

          val result = controller(getEmptyData).onSubmit(NormalMode, srn, index)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

  }
}
