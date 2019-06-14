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

package identifiers.register.establishers

import identifiers._
import models.Link
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow


case class EstablisherNewNinoId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherNewNinoId.toString \ "nino"
}

object EstablisherNewNinoId {

  override lazy val toString: String = "establisherNino"

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[EstablisherNewNinoId] = {

    new CheckYourAnswers[EstablisherNewNinoId] {

      override def row(id: EstablisherNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA[EstablisherNewNinoId]()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: EstablisherNewNinoId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => StringCYA[EstablisherNewNinoId]()().row(id)(changeUrl, userAnswers)
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