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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.trustees.individual.TrusteeNameId
import models.person.PersonName
import models.{Index, Mode, NormalMode, UpdateMode}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.whatYouWillNeedIndividualDetails

class WhatYouWillNeedIndividualDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val personName = "Test Name"
  private val mandatoryTrustee = UserAnswers().set(TrusteeNameId(0))(PersonName("Test", "Name")).asOpt.value.dataRetrievalAction
  private val view = injector.instanceOf[whatYouWillNeedIndividualDetails]

  def controller(dataRetrievalAction: DataRetrievalAction = mandatoryTrustee): WhatYouWillNeedIndividualDetailsController =
    new WhatYouWillNeedIndividualDetailsController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  def viewAsString(mode: Mode): String = {
    val href = controllers.register.trustees.individual.routes.TrusteeDOBController.onPageLoad(mode, index = Index(0), EmptyOptionalSchemeReferenceNumber)
    view(None, href, None, personName)(fakeRequest, messages).toString
  }

  "WhatYouWillNeedIndividualDetailsControllerSpec" when {

    "in Subscription journey" must {
      "on a GET it must return OK and the correct view" in {
        val result = controller().onPageLoad( NormalMode,  Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(NormalMode)
      }
    }

    "in Variations journey" must {
      "on a GET it must return OK and the correct view" in {
        val result = controller().onPageLoad(UpdateMode, Index(0), EmptyOptionalSchemeReferenceNumber)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(UpdateMode)
      }
    }
  }
}

