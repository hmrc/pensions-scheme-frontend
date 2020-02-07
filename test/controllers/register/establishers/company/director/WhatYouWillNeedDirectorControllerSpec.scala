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
import controllers.actions._
import controllers.register.establishers.company.director.routes.DirectorNameController
import models.{Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import views.html.register.establishers.company.director.whatYouWillNeed
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents

class WhatYouWillNeedDirectorControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val view = injector.instanceOf[whatYouWillNeed]

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): WhatYouWillNeedDirectorController =
    new WhatYouWillNeedDirectorController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  private def href: Call = DirectorNameController.onPageLoad(NormalMode, 0, 0, None)

  private def viewAsString(): String = view(None, None, href)(fakeRequest, messages).toString

  "WhatYouWillNeedCompanyDetailsControllerSpec" when {

    "on a GET" must {
      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, None, Index(0))(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

