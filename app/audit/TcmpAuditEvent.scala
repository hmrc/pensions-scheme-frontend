/*
 * Copyright 2022 HM Revenue & Customs
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

import models.{MoneyPurchaseBenefits, TypeOfBenefits}
import play.api.libs.json.{JsObject, JsValue, Json}

case class TcmpAuditEvent(
                           psaId: String,
                           tcmp: String,
                           payload: JsValue,
                           auditType: String
                         ) extends ExtendedAuditEvent {

  override def details: JsObject = Json.obj(
    "pensionSchemeAdministratorId" -> psaId,
    "taxationCollectiveMoneyPurchaseType" -> tcmp,
    "payload" -> payload
  )
}

object TcmpAuditEvent {

  def tcmpAuditValue(
                      typeOfBenefits: TypeOfBenefits,
                      moneyPurchaseBenefit: Option[Seq[MoneyPurchaseBenefits]]
                    ): String =
    typeOfBenefits match {
      case TypeOfBenefits.MoneyPurchase | TypeOfBenefits.MoneyPurchaseDefinedMix =>
        moneyPurchaseBenefit match {
          case Some(mpb) =>
            mpb match {
              case Seq(MoneyPurchaseBenefits.Collective) => "01"
              case Seq(MoneyPurchaseBenefits.CashBalance) => "02"
              case Seq(MoneyPurchaseBenefits.Other) => "03"
              case Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance) |
                   Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.Other) |
                   Seq(MoneyPurchaseBenefits.Collective, MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other) => "04"
              case Seq(MoneyPurchaseBenefits.CashBalance, MoneyPurchaseBenefits.Other) => "05"
              case _ => "TCMP not defined"
            }
          case _ =>
            "No MoneyPurchaseBenefits returned"
        }
      case _ =>
        s"No TCMP - Type Of Benefit = ${TypeOfBenefits.Defined.toString}"
    }
}
