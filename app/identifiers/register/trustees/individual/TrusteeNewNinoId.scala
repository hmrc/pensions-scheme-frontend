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
import models.Link
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow


case class TrusteeNewNinoId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath =  TrusteesId(index).path \ TrusteeNewNinoId.toString \ "nino"
}

object TrusteeNewNinoId {

  override lazy val toString: String = "trusteeNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[TrusteeNewNinoId] = {

    new CheckYourAnswers[TrusteeNewNinoId] {

      override def row(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA[TrusteeNewNinoId]()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: TrusteeNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsTrusteeNewId(id.index)) match {
          case Some(true) => StringCYA[TrusteeNewNinoId]()().row(id)(changeUrl, userAnswers)
          case _ =>
            userAnswers.get(id) match {
              case Some(nino) => Seq(AnswerRow("messages__common__nino", Seq(nino), answerIsMessageKey = false, None))
              case _ => Seq(AnswerRow("messages__common__nino", Seq("site.not_entered"), answerIsMessageKey = true,
                Some(Link("site.add", changeUrl, Some(s"messages__visuallyhidden__director__nino_add")))))
            }
        }
      }
    }
  }
}
