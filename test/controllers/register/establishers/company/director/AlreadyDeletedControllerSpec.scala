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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import models.{Index, NormalMode}
import play.api.mvc.Call
import play.api.test.Helpers._
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.html.alreadyDeleted
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class AlreadyDeletedControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.register.establishers.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode, None, establisherIndex)

  private val establisherIndex = Index(0)
  private val directorIndex = Index(0)
  private val directorName = "first last"
  private val companyName = "test company name"

  private val view = injector.instanceOf[alreadyDeleted]

  private def viewmodel = AlreadyDeletedViewModel(Message("messages__alreadyDeleted__director_title"), directorName, onwardRoute)

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirectorWithDirectorName): AlreadyDeletedController =
    new AlreadyDeletedController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString(): String = view(
    viewmodel
  )(fakeRequest, messages).toString

  "AlreadyDeleted Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(establisherIndex, directorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, directorIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }
}
