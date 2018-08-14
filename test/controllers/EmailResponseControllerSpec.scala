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

import audit.AuditService
import audit.testdoubles.StubSuccessfulAuditService
import controllers.model.{Delivered, EmailEvent, EmailEvents}
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.inject.bind

class EmailResponseControllerSpec extends ControllerSpecBase {

  import EmailResponseControllerSpec._

  "EmailResponseController" must {

    "respond OK when given EmailEvents" which {
      "will send events to audit service" in {

        running(_.overrides(
          bind[AuditService].to(fakeAuditService)
        )) { app =>

          val controller = app.injector.instanceOf[EmailResponseController]

          val result = controller.post("id")(fakeRequest.withBody(Json.toJson(emailEvents)))

          status(result) mustBe OK
          fakeAuditService.verifySent(audit.EmailEvent("id", Delivered)) mustBe true

        }
      }
    }

  }

  "respond with BAD_REQUEST when not given EmailEvents" in {

    running(_.overrides(
      bind[AuditService].to(fakeAuditService)
    )) { app =>

      fakeAuditService.reset()

      val controller = app.injector.instanceOf[EmailResponseController]

      val result = controller.post("id")(fakeRequest.withBody(validJson))

      status(result) mustBe BAD_REQUEST
      fakeAuditService.verifyNothingSent mustBe true

    }

  }

  "respond with FORBIDDEN when URL contains an id does not match PSAID pattern" in {

    running(_.overrides(
      bind[AuditService].to(fakeAuditService)
    )) { app =>

      fakeAuditService.reset()

      val controller = app.injector.instanceOf[EmailResponseController]

      val result = controller.post("id")(fakeRequest.withBody(Json.toJson(emailEvents)))

      status(result) mustBe FORBIDDEN
      fakeAuditService.verifyNothingSent mustBe true
    }
  }

}

object EmailResponseControllerSpec {

  val emailEvents = EmailEvents(Seq(EmailEvent(Delivered, DateTime.now())))

  val fakeAuditService = new StubSuccessfulAuditService()

  val validJson = Json.obj("name" -> "value")

}