/*
 * Copyright 2019 HM Revenue & Customs
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

package identifiers.register.trustees.individual

import config.FeatureSwitchManagementService
import identifiers._
import identifiers.register.trustees
import identifiers.register.trustees.TrusteesId
import models.ReferenceValue
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.checkyouranswers.{CheckYourAnswers, ReferenceValueCYA}
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeNewNinoId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeNewNinoId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(TrusteeNoNINOReasonId(this.index))
}

object TrusteeNewNinoId {

  override lazy val toString: String = "trusteeNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions,
                   featureSwitchManagementService: FeatureSwitchManagementService): CheckYourAnswers[TrusteeNewNinoId] = {

    new CheckYourAnswers[TrusteeNewNinoId] {

      val name = (index: Int) =>
          userAnswers.get(TrusteeNameId(index)).map(_.fullName)

      def trusteeName(index: Int) = name(index).getOrElse(messages("messages__theTrustee"))
      def label(index: Int): String = messages("messages__trustee__individual__nino__heading", trusteeName(index))
      def hiddenLabel(index: Int) = messages("messages__visuallyhidden__dynamic_nino", trusteeName(index))

      override def row(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        ReferenceValueCYA[TrusteeNewNinoId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(trustees.IsTrusteeNewId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[TrusteeNewNinoId](label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[TrusteeNewNinoId](label(id.index), hiddenLabel(id.index))().updateRow(id)(changeUrl, userAnswers)
        }
      }
    }
  }
}
