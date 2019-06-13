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

package identifiers.register.trustees.company

import identifiers.TypedIdentifier
import identifiers.register.trustees.TrusteesId
import models.Link
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.checkyouranswers.CheckYourAnswers
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{CountryOptions, UserAnswers}
import viewmodels.AnswerRow

case class CompanyPayeVariationsId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = TrusteesId(index).path \ "companyPaye" \ CompanyPayeVariationsId.toString
}

object CompanyPayeVariationsId {
  override def toString: String = "paye"

  val hiddenLabelPaye = "messages__visuallyhidden__trustee__paye_number"

  implicit def cya(implicit messages: Messages, countryOptions: CountryOptions): CheckYourAnswers[CompanyPayeVariationsId] = {
    new CheckYourAnswers[CompanyPayeVariationsId] {

      override def row(id: CompanyPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA(Some("messages__common__cya__paye"), Some(hiddenLabelPaye))().row(id)(changeUrl, userAnswers)

      override def updateRow(id: CompanyPayeVariationsId)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id) match {
          case Some(paye) => Seq(AnswerRow("messages__common__cya__paye", Seq(paye), answerIsMessageKey = false, None))
          case _ => Seq(AnswerRow("messages__common__cya__paye", Seq("site.not_entered"), answerIsMessageKey = true,
            Some(Link("site.add", changeUrl, Some(s"${hiddenLabelPaye}_add")))))
        }
    }
  }
}


