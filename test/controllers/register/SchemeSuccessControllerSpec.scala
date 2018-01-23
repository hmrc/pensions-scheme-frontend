/*
 * Copyright 2018 HM Revenue & Customs
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

import controllers.actions._
import play.api.test.Helpers._
import views.html.register.schemeSuccess
import controllers.ControllerSpecBase
import identifiers.register.SchemeDetailsId
import models.{SchemeDetails, SchemeType}
import org.joda.time.LocalDate
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.cache.client.CacheMap

class SchemeSuccessControllerSpec extends ControllerSpecBase {

  val validData: JsObject = Json.obj(
    SchemeDetailsId.toString -> Json.toJson(SchemeDetails("test scheme name", SchemeType.SingleTrust))
  )

  def controller(dataRetrievalAction: DataRetrievalAction =
                 new FakeDataRetrievalAction(Some(validData))):SchemeSuccessController =
    new SchemeSuccessController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)

  //TODO: Change the hardcoded reference number
  def viewAsString(): String = schemeSuccess(frontendAppConfig, Some("test scheme name"),
    LocalDate.now(), "XX123456789132")(fakeRequest, messages).toString

  "SchemeSuccess Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}




