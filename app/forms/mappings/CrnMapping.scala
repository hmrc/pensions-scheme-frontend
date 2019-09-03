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

package forms.mappings

import models.CompanyRegistrationNumber
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.{mandatoryIfFalse, mandatoryIfTrue}

trait CrnMapping extends Mappings with Transforms {


  protected def companyRegistrationNumberMapping(requiredKey: String = "messages__error__has_crn_company",
                                                 requiredCRNKey: String = "messages__error__crn",
                                                 requiredReasonKey: String = "messages__company__no_crn",
                                                 reasonLengthKey: String = "messages__error__no_crn_length",
                                                 invalidCRNKey: String = "messages__error__crn_invalid",
                                                 noReasonKey: String = "messages__error__no_crn_company",
                                                 invalidReasonKey: String = "messages__error__no_crn_invalid"):
  Mapping[CompanyRegistrationNumber] = {
    val reasonMaxLength = 160

    def fromCompanyRegistrationNumber(crn: CompanyRegistrationNumber): (Boolean, Option[String], Option[String]) = {
      crn match {
        case CompanyRegistrationNumber.Yes(crn) => (true, Some(crn), None)
        case CompanyRegistrationNumber.No(reason) => (false, None, Some(reason))
      }
    }

    def toCompanyRegistrationNumber(crnTuple: (Boolean, Option[String], Option[String])) = {

      crnTuple match {
        case (true, Some(crn), None) => CompanyRegistrationNumber.Yes(crn)
        case (false, None, Some(reason)) => CompanyRegistrationNumber.No(reason)
        case _ => throw new RuntimeException("Invalid selection")
      }
    }

    tuple("hasCrn" -> boolean(requiredKey),
      "crn" -> mandatoryIfTrue("companyRegistrationNumber.hasCrn", text(requiredCRNKey).
        transform(noSpaceWithUpperCaseTransform, noTransform).verifying(validCrn(invalidCRNKey))),
      "reason" -> mandatoryIfFalse("companyRegistrationNumber.hasCrn", text(noReasonKey).
        verifying(firstError(
          maxLength(reasonMaxLength, reasonLengthKey),
          safeText(invalidReasonKey))))).transform(toCompanyRegistrationNumber, fromCompanyRegistrationNumber)
  }

  def companyRegistrationNumberStringMapping(crnLengthKey: String = "messages__error__no_crn_length",
                        requiredCRNKey: String = "messages__error__company_number",
                        invalidCRNKey: String = "messages__error__crn_invalid"):
    Mapping[String] = text(requiredCRNKey)
      .transform(noSpaceWithUpperCaseTransform, noTransform)
      .verifying(validCrn(invalidCRNKey))
}
