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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import models.register.trustees.TrusteeKind
import models.{Index, NormalMode}
import play.api.mvc.Call
import play.api.test.Helpers._
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

class AlreadyDeletedControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, None)

  private val trusteeIndex = Index(0)

  def viewmodel(trusteeName: String): AlreadyDeletedViewModel = AlreadyDeletedViewModel(
    title = Message("messages__alreadyDeleted__trustee_title"),
    deletedEntity = trusteeName,
    returnCall = onwardRoute
  )


  private val view = injector.instanceOf[alreadyDeleted]

  def controller(dataRetrievalAction: DataRetrievalAction): AlreadyDeletedController =
    new AlreadyDeletedController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  def viewAsString(trusteeName: String): String = view(
    viewmodel(trusteeName)
  )(fakeRequest, messages).toString

  "AlreadyDeleted Trustee Controller" must {

    "return OK and the correct view for a GET for an individual trustee" in {
      val result = controller(getMandatoryTrustee).onPageLoad(NormalMode, trusteeIndex, TrusteeKind.Individual, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("Test Name")
    }

    "return OK and the correct view for a GET for a company trustee" in {
      val result = controller(getMandatoryTrusteeCompany).onPageLoad(NormalMode, trusteeIndex, TrusteeKind.Company, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("test company name")
    }

    "return OK and the correct view for a GET for a partnership trustee" in {
      val result = controller(getMandatoryTrusteePartnership).onPageLoad(NormalMode, trusteeIndex, TrusteeKind.Partnership, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("test partnership name")
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, trusteeIndex, TrusteeKind.Individual, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

  }
}
