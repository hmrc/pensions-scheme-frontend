/*
 * Copyright 2018 HM Revenue & Customs
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
import identifiers.register.trustees.{MoreThanTenTrusteesId, TrusteesId}
import models.CompanyDetails
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CompanyDetailsCYA}

case class CompanyDetailsId(index: Int) extends TypedIdentifier[CompanyDetails] {
  override def path: JsPath = TrusteesId(index).path \ CompanyDetailsId.toString

  override def cleanup(value: Option[CompanyDetails], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.allTrusteesAfterDelete.lengthCompare(10) match {
      case lengthCompare if lengthCompare <= 0 => userAnswers.remove(MoreThanTenTrusteesId)
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object CompanyDetailsId {
  override lazy val toString: String = "companyDetails"

  implicit val cya: CheckYourAnswers[CompanyDetailsId] = CompanyDetailsCYA(
    changeVat = "messages__visuallyhidden__trustee__vat_number",
    changePaye = "messages__visuallyhidden__trustee__paye_number")()
}
