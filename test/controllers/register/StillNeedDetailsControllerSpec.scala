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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.trustees.MoreThanTenTrusteesId
import models.{OptionalSchemeReferenceNumber, SchemeReferenceNumber}
import play.api.libs.json._
import play.api.test.Helpers._
import views.html.register.stillNeedDetails

class StillNeedDetailsControllerSpec extends ControllerSpecBase {
  appRunning()

  val schemeName = "Test Scheme Name"
  val srn = SchemeReferenceNumber("A2343243")
  val validData: JsObject = Json.obj(
    MoreThanTenTrusteesId.toString -> false
  )

  private val view = injector.instanceOf[stillNeedDetails]
  def controller(dataRetrievalAction: DataRetrievalAction = getMandatorySchemeNameHs): StillNeedDetailsController =
    new StillNeedDetailsController(messagesApi,
      FakeAuthAction, dataRetrievalAction, controllerComponents, view)

  def viewAsString(): String = view(OptionalSchemeReferenceNumber(Some(srn)), Some(schemeName))(fakeRequest, messages).toString

  "StillNeedDetails Controller" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(OptionalSchemeReferenceNumber(Some(srn)))(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}
