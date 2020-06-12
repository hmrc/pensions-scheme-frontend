/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.{CountryOptions, UserAnswers}
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersDirectors}
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import viewmodels.AnswerRow

case class DirectorPhoneNumberId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[String] {
  override def path: JsPath =
    EstablishersId(establisherIndex)
      .path \ "director" \ directorIndex \ "directorContactDetails" \ DirectorPhoneNumberId.toString
}


object DirectorPhoneNumberId {
  override def toString: String = "phoneNumber"

  implicit def cya(implicit userAnswers: UserAnswers,
                   messages: Messages,
                   countryOptions: CountryOptions): CheckYourAnswers[DirectorPhoneNumberId] = {

    new CheckYourAnswersDirectors[DirectorPhoneNumberId] {

      private def label(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__director__cya__phone")

      private def hiddenLabel(establisherIndex: Int, directorIndex: Int, ua: UserAnswers): String =
        dynamicMessage(establisherIndex, directorIndex, ua, "messages__visuallyhidden__dynamic_phone")

      override def row(id: DirectorPhoneNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some(label(id.establisherIndex, id.directorIndex, userAnswers)),
          Some(hiddenLabel(id.establisherIndex, id.directorIndex, userAnswers)))()
          .row(id)(changeUrl, userAnswers)


      override def updateRow(id: DirectorPhoneNumberId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        row(id)(changeUrl, userAnswers)
    }
  }
}
