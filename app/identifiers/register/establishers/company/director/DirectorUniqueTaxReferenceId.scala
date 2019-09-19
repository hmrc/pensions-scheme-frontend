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

package identifiers.register.establishers.company.director

import identifiers._
import identifiers.register.establishers.EstablishersId
import models.UniqueTaxReference
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, UniqueTaxReferenceCYA}
import viewmodels.AnswerRow

case class DirectorUniqueTaxReferenceId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[UniqueTaxReference] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorUniqueTaxReferenceId.toString
}

object DirectorUniqueTaxReferenceId {

  override def toString: String = "directorUniqueTaxReference"

  implicit def uniqueTaxReference(implicit messages: Messages): CheckYourAnswers[DirectorUniqueTaxReferenceId] = {

    val directorUtrCya = (establisherIndex: Int, directorIndex: Int, ua: UserAnswers) => {
      val (label, reasonLabel) = ua.get(DirectorNameId(establisherIndex, directorIndex)) match {
        case Some(name) =>
          (messages("messages__director__cya__utr_yes_no", name.fullName), messages("messages__director__cya__utr_no_reason", name.fullName))
        case None   => ("messages__director__cya__utr_yes_no_fallback", "messages__director__cya__utr_no_reason__fallback")
      }

      UniqueTaxReferenceCYA[DirectorUniqueTaxReferenceId](
        label = label,
        reasonLabel = reasonLabel,
        changeHasUtr = "messages__visuallyhidden__director__utr_yes_no",
        changeUtr = "messages__visuallyhidden__director__utr",
        changeNoUtr = "messages__visuallyhidden__director__utr_no"
      )()
    }

    new CheckYourAnswers[DirectorUniqueTaxReferenceId] {

      override def row(id: DirectorUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        directorUtrCya(id.establisherIndex, id.directorIndex, userAnswers).row(id)(changeUrl, userAnswers)

      override def updateRow(id: DirectorUniqueTaxReferenceId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IsNewDirectorId(id.establisherIndex, id.directorIndex)) match {
          case Some(true) => directorUtrCya(id.establisherIndex, id.directorIndex, userAnswers).row(id)(changeUrl, userAnswers)
          case _          => directorUtrCya(id.establisherIndex, id.directorIndex, userAnswers).updateRow(id)(changeUrl, userAnswers)
        }
    }
  }

}
