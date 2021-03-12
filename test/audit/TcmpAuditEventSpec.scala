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

import models.MoneyPurchaseBenefits
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class TcmpAuditEventSpec
  extends FlatSpec
    with Matchers {

  "TcmpAuditEvent" should "only audit if there are changes" in {
    val a = Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.Other)

    val b = Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other, MoneyPurchaseBenefits.Collective)

    println(s"\n\n\n\n${a.sortWith(_.toString < _.toString)}\n\n\n\n")
    println(s"\n\n\n\n${Json.toJson(a.map(_.toString))}\n\n\n\n")

    a.sortWith(_.toString < _.toString) == b.sortWith(_.toString < _.toString) shouldBe true
  }
}
