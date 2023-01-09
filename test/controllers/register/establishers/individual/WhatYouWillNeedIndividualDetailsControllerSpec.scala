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
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.register.establishers.individual.routes._
import identifiers.register.establishers.individual.EstablisherNameId
import models.person.PersonName
import models.{Index, Mode, NormalMode, UpdateMode}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.whatYouWillNeedIndividualDetails

class WhatYouWillNeedIndividualDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val establisherName = "Test Name"
  private val mandatoryEstablisher =
    UserAnswers().set(EstablisherNameId(0))(
      PersonName("Test", "Name")
    ).asOpt.value.dataRetrievalAction

  private val view = injector.instanceOf[whatYouWillNeedIndividualDetails]

  def controller(dataRetrievalAction: DataRetrievalAction = mandatoryEstablisher): WhatYouWillNeedIndividualDetailsController =
    new WhatYouWillNeedIndividualDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      view,
      controllerComponents
    )

  def viewAsString(mode: Mode): String = {
    val href = EstablisherDOBController.onPageLoad(mode, 0, None)
    view(None, href, None, establisherName)(fakeRequest, messages).toString
  }

  "WhatYouWillNeedIndividualDetailsControllerSpec" when {

    "in Subscription journey" must {
      "on a GET it must return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode, Index(0), None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode)
      }
    }

    "in Variations journey" must {
      "on a GET it must return OK and the correct view" in {
        val result = controller().onPageLoad(UpdateMode, Index(0), None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode)
      }
    }
  }
}
