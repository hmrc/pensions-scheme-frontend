/*
 * Copyright 2019 HM Revenue & Customs
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
import controllers.actions._
import models.{Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.register.establishers.company.director.whatYouWillNeed

class WhatYouWillNeedDirectorControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private def onwardRoute: Call =
    controllers.register.establishers.company.director.routes.DirectorNameController
      .onSubmit(NormalMode, establisherIndex = Index(0), directorIndex = Index(1), None)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedDirectorController =
    new WhatYouWillNeedDirectorController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl
    )

  private def postCall = controllers.register.establishers.company.director.routes.WhatYouWillNeedDirectorController
    .onSubmit(NormalMode, None, establisherIndex = Index(0), directorIndex = Index(1))

  private def viewAsString(): String = whatYouWillNeed(frontendAppConfig, None, postCall, None)(fakeRequest, messages).toString

  "WhatYouWillNeedCompanyDetailsControllerSpec" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, None, Index(0), Index(1))(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      "redirect to relavant page" in {
        val result = controller().onSubmit(NormalMode, None, Index(0), Index(1))(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}

