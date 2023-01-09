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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import models.NormalMode
import play.api.mvc.Call
import play.api.test.Helpers._
import viewmodels.Message
import views.html.register.whatYouWillNeedAddress

class WhatYouWillNeedIndividualAddressControllerSpec extends ControllerSpecBase {

  private val view = injector.instanceOf[whatYouWillNeedAddress]
  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): WhatYouWillNeedIndividualAddressController =
    new WhatYouWillNeedIndividualAddressController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  private def href: Call = controllers.register.establishers.individual.routes.PostCodeLookupController.onSubmit(NormalMode, index = 0, None)

  private def viewAsString(): String =
    view(
      None, href, None, "Test Name", Message("messages__theIndividual")
    )(fakeRequest, messages).toString

  "WhatYouWillNeedIndividualAddressController" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, index = 0, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

