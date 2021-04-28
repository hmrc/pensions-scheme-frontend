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

package audit

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class TcmpAuditEventSpec
  extends FlatSpec
    with Matchers {

  "TcmpAuditEvent" should "serialise audit event correctly" in {

    val auditEvent: TcmpAuditEvent = TcmpAuditEvent(
      psaId = "A0000000",
      tcmp = "01",
      payload = Json.obj("some" -> "payload"),
      auditType = "TaxationCollectiveMoneyPurchaseAuditEvent"
    )

    auditEvent.auditType shouldBe "TaxationCollectiveMoneyPurchaseAuditEvent"

    auditEvent.details shouldBe Json.obj(
      "pensionSchemeAdministratorId" -> "A0000000",
      "taxationCollectiveMoneyPurchaseType" -> "01",
      "payload" -> Json.parse("""{"some":"payload"}""".stripMargin)
    )
  }
}
