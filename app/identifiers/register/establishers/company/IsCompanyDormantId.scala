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

package identifiers.register.establishers.company

import identifiers.TypedIdentifier
import identifiers.register.establishers.{EstablishersId, IsEstablisherNewId}
import models.Link
import models.register.DeclarationDormant
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.BooleanCYA
import viewmodels.AnswerRow

case class IsCompanyDormantId(index: Int) extends TypedIdentifier[DeclarationDormant] {
  override def path: JsPath = EstablishersId(index).path \ IsCompanyDormantId.toString
}

object IsCompanyDormantId {
  override def toString: String = "isCompanyDormant"



  implicit def cya(implicit userAnswers: UserAnswers, messages: Messages): CheckYourAnswers[IsCompanyDormantId] = {

    def companyName(index: Int) =
      userAnswers.get(CompanyDetailsId(index)) match {
        case Some(companyDetails) => companyDetails.companyName
        case _ => messages("messages__theCompany")
      }

    def label(index: Int):String = messages("messages__company__cya__dormant", companyName(index))

    def hiddenLabel(index: Int):Option[String] = Some(messages("messages__visuallyhidden__dynamic_company__dormant", companyName(index)))


    new CheckYourAnswers[IsCompanyDormantId] {
      override def row(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(DeclarationDormant.Yes) => Seq(
            AnswerRow(
              label(id.index),
              Seq("site.yes"),
              answerIsMessageKey = true,
              Some(Link("site.change", changeUrl,
                hiddenLabel(id.index)))
            )
          )
          case Some(DeclarationDormant.No) => Seq(
            AnswerRow(
              label(id.index),
              Seq("site.no"),
              answerIsMessageKey = true,
              Some(Link("site.change", changeUrl,
                hiddenLabel(id.index)))
            ))
          case _ => Seq.empty[AnswerRow]
        }

      override def updateRow(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = Nil
    }

//    new CheckYourAnswers[IsCompanyDormantId] {
//      override def row(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
//        BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
//
//      override def updateRow(id: IsCompanyDormantId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
//        userAnswers.get(IsEstablisherNewId(id.index)) match {
//          case Some(true) => BooleanCYA(label(id.index), hiddenLabel(id.index))().row(id)(changeUrl, userAnswers)
//          case _ => Seq.empty[AnswerRow]
//        }
//    }
  }
}
