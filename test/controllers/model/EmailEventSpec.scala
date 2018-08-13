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

package controllers.model

import org.joda.time.DateTime
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsValue, Json}

class EmailEventSpec extends WordSpec with MustMatchers {

  "Event" must {

    "format JSON" when {
      "to JSON" in {
        Json.toJson(EmailEvents(Seq(
          EmailEvent(Opened, DateTime.parse("2015-07-02T08:26:39.035Z")),
          EmailEvent(Delivered, DateTime.parse("2015-07-02T08:25:20.068Z"))
        ))) mustBe validJson
      }
      "from JSON" in {
        validJson.as[EmailEvents] mustBe EmailEvents(Seq(
          EmailEvent(Opened, DateTime.parse("2015-07-02T08:26:39.035Z")),
          EmailEvent(Delivered, DateTime.parse("2015-07-02T08:25:20.068Z"))
        ))
      }
    }
  }

  val validJson: JsValue = Json.parse(
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
