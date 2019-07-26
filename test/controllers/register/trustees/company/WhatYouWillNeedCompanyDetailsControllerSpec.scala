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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions._
import models.{Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.register.trustees.company.whatYouWillNeedCompanyDetails

class WhatYouWillNeedCompanyDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): WhatYouWillNeedCompanyDetailsController =
    new WhatYouWillNeedCompanyDetailsController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl
    )

  lazy val postCall = controllers.register.trustees.company.routes.HasCompanyNumberController.onSubmit(NormalMode, Index(0), None)

  def viewAsString(): String = whatYouWillNeedCompanyDetails(frontendAppConfig, None, postCall, None)(fakeRequest, messages).toString


  "WhatYouWillNeedCompanyDetailsControllerSpec" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, index=Index(0), None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

