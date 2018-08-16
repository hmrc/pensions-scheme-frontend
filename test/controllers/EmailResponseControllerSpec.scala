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

package controllers

import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._

class EmailResponseControllerSpec extends ControllerSpecBase {

  import EmailResponseControllerSpec._

  "EmailResponseController" must {

    "respond 200 when given json" in {

      val controller = app.injector.instanceOf[EmailResponseController]

      val result = controller.post("id")(fakeRequest.withBody[JsValue](validJson))

      status(result) mustBe OK

    }

  }

}

object EmailResponseControllerSpec {

  val validJson = Json.parse(
    """{
      |    "events": [
      |        {
      |            "event": "opened",
      |            "detected": "2015-07-02T08:26:39.035Z"
      |        },
      |        {
      |            "event": "delivered",
      |            "detected": "2015-07-02T08:25:20.068Z"
      |        }
      |    ]
      |}""".stripMargin
  )

}
