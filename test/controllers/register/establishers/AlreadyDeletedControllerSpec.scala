/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register.establishers

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import models.register.establishers.EstablisherKind
import models.{Index, NormalMode}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted

class AlreadyDeletedControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(NormalMode, None)

  private val establisherIndex = Index(0)

  private val view = injector.instanceOf[alreadyDeleted]

  def viewmodel(establisherName: String): AlreadyDeletedViewModel = AlreadyDeletedViewModel(
    title = Message("messages__alreadyDeleted__establisher_title"),
    deletedEntity = establisherName,
    returnCall = onwardRoute
  )

  def controller(dataRetrievalAction: DataRetrievalAction): AlreadyDeletedController =
    new AlreadyDeletedController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString(establisherName: String): String = view(
    viewmodel(establisherName)
  )(fakeRequest, messages).toString

  "AlreadyDeleted Establisher Controller" must {

    "return OK and the correct view for a GET for an individual" in {
      val result = controller(getMandatoryEstablisher).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Indivdual, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("Test Name")
    }

    "return OK and the correct view for a GET for a company" in {
      val result = controller(getMandatoryEstablisherCompany).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Company, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("test company name")
    }

    "return OK and the correct view for a GET for a partnership" in {
      val result = controller(getMandatoryEstablisherPartnership).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Partnership, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString("test partnership name")
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, EstablisherKind.Indivdual, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }
}
