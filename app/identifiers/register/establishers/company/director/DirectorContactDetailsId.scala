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

import identifiers.TypedIdentifier
import identifiers.register.establishers.EstablishersId
import models.{ContactDetails, Link}
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.ContactDetailsCYA
import viewmodels.AnswerRow


case class DirectorContactDetailsId(establisherIndex: Int, directorIndex: Int) extends TypedIdentifier[ContactDetails] {
  override def path: JsPath = EstablishersId(establisherIndex).path \ "director" \ directorIndex \ DirectorContactDetailsId.toString
}

object DirectorContactDetailsId {
  override def toString: String = "directorContactDetails"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorContactDetailsId] = {

    new CheckYourAnswers[DirectorContactDetailsId] {
      val changeEmailAddress: String = "messages__visuallyhidden__common__email_address"
      val changePhoneNumber: String = "messages__visuallyhidden__common__phone_number"

      override def row(id: DirectorContactDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map {
        contactDetails =>{

          val emailLabel = userAnswers.get(DirectorNameId(id.establisherIndex, id.directorIndex)) match {
            case Some(name) => messages("messages__director__cya__email_address", name.fullName)
            case None => "messages__director__cya__email_address__fallback"
          }

          val phoneLabel = userAnswers.get(DirectorNameId(id.establisherIndex, id.directorIndex)) match {
            case Some(name) => messages("messages__director__cya__phone", name.fullName)
            case None => "messages__director__cya__phone__fallback"
          }

          Seq(
            AnswerRow(
              emailLabel,
              Seq(s"${contactDetails.emailAddress}"),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl,
                Some(changeEmailAddress)))
            ),
            AnswerRow(
              phoneLabel,
              Seq(s"${contactDetails.phoneNumber}"),
              answerIsMessageKey = false,
              Some(Link("site.change", changeUrl,
                Some(changePhoneNumber)))
            ))
        }
      }.getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: DirectorContactDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = row(id)(changeUrl, userAnswers)

      ContactDetailsCYA(
        changeEmailAddress = "messages__visuallyhidden__director__email_address",
        changePhoneNumber = "messages__visuallyhidden__director__phone_number"
      )()
    }
  }
}
