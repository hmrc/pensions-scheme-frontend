/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Index, NormalMode}
import org.scalatest.BeforeAndAfterEach
import org.mockito.MockitoSugar
import play.api.test.Helpers._

import views.html.register.establishers.company.whatYouWillNeedCompanyDetails

class WhatYouWillNeedCompanyDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach {

  private val view = injector.instanceOf[whatYouWillNeedCompanyDetails]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): WhatYouWillNeedCompanyDetailsController =
    new WhatYouWillNeedCompanyDetailsController(frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      view,
      controllerComponents
    )

  lazy val href = controllers.register.establishers.company.routes.HasCompanyCRNController.onSubmit(NormalMode, None, index=Index(0))

  def viewAsString(): String = view(None, href, None, "test company name")(fakeRequest, messages).toString

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

