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
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId, IsEstablisherNewId}
import models.Link
import models.person.PersonDetails
import models.requests.DataRequest
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import play.api.mvc.AnyContent
import utils.checkyouranswers.CheckYourAnswers
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Message}

case class EstablisherDetailsId(index: Int) extends TypedIdentifier[PersonDetails] {
  override def path: JsPath = EstablishersId(index).path \ EstablisherDetailsId.toString
}

object EstablisherDetailsId {
  override lazy val toString: String = "establisherDetails"

  def isComplete(index: Int)(implicit request: DataRequest[AnyContent]): Option[Boolean] =
    request.userAnswers.get[Boolean](JsPath \ EstablishersId(index) \ IsEstablisherCompleteId.toString)

  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[EstablisherDetailsId] = {
    new CheckYourAnswers[EstablisherDetailsId] {

      private def personDetailsCYARow(personDetails: PersonDetails, changeUrlName: Option[Link], changeUrlDob: Option[Link]): Seq[AnswerRow] = {
        Seq(
          AnswerRow("messages__common__cya__name", Seq(personDetails.fullName), answerIsMessageKey = false, changeUrlName),
          AnswerRow("messages__common__dob", Seq(DateHelper.formatDate(personDetails.date)), answerIsMessageKey = false, changeUrlDob)
        )
      }

      override def row(id: EstablisherDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map { personDetails =>
        personDetailsCYARow(personDetails,
          Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName).resolve))),
          Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName).resolve)))
        )
      }.getOrElse(Seq.empty[AnswerRow])

      override def updateRow(id: EstablisherDetailsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = userAnswers.get(id).map {
        personDetails =>
          userAnswers.get(IsEstablisherNewId(id.index)) match {
            case Some(true) => personDetailsCYARow(personDetails,
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__name", personDetails.fullName).resolve))),
              Some(Link("site.change", changeUrl, Some(Message("messages__visuallyhidden__common__dob", personDetails.fullName).resolve)))
            )
            case _  => personDetailsCYARow(personDetails, None, None)
          }

      }.getOrElse(Seq.empty[AnswerRow])
    }
  }
}
