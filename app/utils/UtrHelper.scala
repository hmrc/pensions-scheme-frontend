/*
 * Copyright 2021 HM Revenue & Customs
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

package utils

import identifiers.TypedIdentifier
import identifiers.register.establishers.company.CompanyEnterUTRId
import identifiers.register.establishers.company.director.DirectorEnterUTRId
import identifiers.register.trustees.individual.TrusteeUTRId
import identifiers.register.trustees.partnership.PartnershipEnterUTRId
import models.ReferenceValue

object UtrHelper {

  def stripUtr(userAnswers: UserAnswers): UserAnswers = {
    (0 to 9).foldLeft(userAnswers) {
      (ua, index) =>
        filterUserAnswers(
          filterUserAnswers(ua, CompanyEnterUTRId(index)), PartnershipEnterUTRId(index)
        )
    }
  }

//  PartnershipEnterUTRId(index)
//                            DirectorEnterUTRId(0, index),
//              TrusteeUTRId(index)

  private def filterUserAnswers(userAnswers: UserAnswers, id: TypedIdentifier[ReferenceValue]): UserAnswers = {
      userAnswers.get(id) match {
          case None => userAnswers
          case Some(v) =>
            val validUtr = strip(v.value)
            UserAnswers(userAnswers.json).setOrException(id)(ReferenceValue(validUtr))
        }
    }

  private def strip(utr: String): String = {
    val r = utr.replaceAll("""[a-zA-Z\s]""", "")
    val regexLongUtr = """^[\d]{13}$"""
    r match {
      case _ if r.matches(regexLongUtr) => r.substring(3)
      case _ => r
    }
  }
}
