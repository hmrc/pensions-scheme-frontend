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

import identifiers._
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class TrusteeUTRId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ TrusteeUTRId.toString
}

object TrusteeUTRId {
  override def toString: String = "utr"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[TrusteeUTRId] = {

    val label: String = messages("messages__common__utr")
    val hiddenLabel = messages("messages__visuallyhidden__trustee__utr")

    new CheckYourAnswers[TrusteeUTRId] {
      override def row(id: TrusteeUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label), Some(hiddenLabel))().row(id)(changeUrl, userAnswers)


      override def updateRow(id: TrusteeUTRId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        val trusteeUtr = userAnswers.get(TrusteeUTRId(id.index))
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => row(id)(changeUrl, userAnswers)
          case _ =>
            trusteeUtr.fold(Seq(AnswerRow(label, Seq("site.not_entered"), answerIsMessageKey = true, None)))(
              utr => Seq(AnswerRow(label, Seq(utr), answerIsMessageKey = false, None)))
        }
      }
    }
  }
}
