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

package identifiers.register.establishers.individual

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.person.PersonDetails
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.PersonalDetailsCYA
import viewmodels.AnswerRow

case class EstablisherDetailsId(index: Int) extends TypedIdentifier[PersonDetails] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherDetailsId.toString
}

object EstablisherDetailsId {
  override lazy val toString: String = "establisherDetails"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[EstablisherDetailsId] = {
    new CheckYourAnswers[EstablisherDetailsId] {

      override def row(id: EstablisherDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        PersonalDetailsCYA()().row(id)(changeUrl, userAnswers)

      override def updateRow(id: EstablisherDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsEstablisherNewId(id.index)) match {
          case Some(true) => PersonalDetailsCYA()().row(id)(changeUrl, userAnswers)
          case _ => PersonalDetailsCYA()().updateRow(id)(changeUrl, userAnswers)
        }
    }
  }
}
