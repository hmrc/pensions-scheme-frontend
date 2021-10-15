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

import models.{TypeOfBenefits, MoneyPurchaseBenefits}
import org.scalatest.flatspec.AnyFlatSpec
import play.api.libs.json.Json
import org.scalatest.matchers.should.Matchers

class TcmpAuditEventSpec
  extends AnyFlatSpec
    with ArgumentMatchers {

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

  "TcmpAuditEvent" should "return correct TCMP Audit value for 01" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "01"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "01"
  }

  "TcmpAuditEvent" should "return correct TCMP Audit value for 02" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.CashBalance)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "02"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.CashBalance)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "02"
  }

  "TcmpAuditEvent" should "return correct TCMP Audit value for 03" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "03"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "03"
  }

  "TcmpAuditEvent" should "return correct TCMP Audit value for 04" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "04"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "04"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "04"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "04"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "04"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "04"
  }

  "TcmpAuditEvent" should "return correct TCMP Audit value for 05" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "05"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other)),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "05"
  }

  "TcmpAuditEvent" should "return TCMP not defined" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq.empty),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "TCMP not defined"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        Some(Seq.empty),
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "TCMP not defined"
  }

  "TcmpAuditEvent" should "return No MoneyPurchaseBenefits returned" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        None,
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchaseDefinedMix
    ) shouldBe "No MoneyPurchaseBenefits returned"

    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        None,
      typeOfBenefits =
        TypeOfBenefits.MoneyPurchase
    ) shouldBe "No MoneyPurchaseBenefits returned"
  }

  "TcmpAuditEvent" should "return \'Defined\' when Type Of Benefit is \'Defined\' returned" in {
    TcmpAuditEvent.tcmpAuditValue(
      moneyPurchaseBenefit =
        None,
      typeOfBenefits =
        TypeOfBenefits.Defined
    ) shouldBe s"No TCMP - Type Of Benefit = ${TypeOfBenefits.Defined.toString}"
  }
}
