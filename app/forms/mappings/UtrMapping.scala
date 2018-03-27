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

package forms.mappings

import models.register.establishers.individual.UniqueTaxReference
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.{mandatoryIfFalse, mandatoryIfTrue}

trait UtrMapping extends Mappings {

  protected def uniqueTaxReferenceMapping(requiredKey: String = "messages__error__has_sautr_establisher",
                                          requiredUtrKey: String = "messages__error__sautr",
                                          requiredReasonKey: String = "messages__error__no_sautr_establisher",
                                          invalidUtrKey: String = "messages__error__sautr_invalid",
                                          maxLengthReasonKey: String = "messages__error__no_sautr_length"
                                         ):
  Mapping[UniqueTaxReference] = {

    val reasonMaxLength = 150
    def fromUniqueTaxReference(utr: UniqueTaxReference): (Boolean, Option[String], Option[String]) = {
      utr match {
        case UniqueTaxReference.Yes(utrNo) => (true, Some(utrNo), None)
        case UniqueTaxReference.No(reason) =>  (false, None, Some(reason))
      }
    }

    def toUniqueTaxReference(utrTuple: (Boolean, Option[String], Option[String])) = {

      utrTuple match {
        case (true, Some(utr), None) => UniqueTaxReference.Yes(utr)
        case (false, None, Some(reason)) => UniqueTaxReference.No(reason)
        case _ => throw new RuntimeException("Invalid selection")
      }
    }

    tuple("hasUtr" -> boolean(requiredKey),
      "utr" -> mandatoryIfTrue("uniqueTaxReference.hasUtr", text(requiredUtrKey).verifying(regexp(regexUtr, invalidUtrKey))),
      "reason" -> mandatoryIfFalse("uniqueTaxReference.hasUtr",
        text(requiredReasonKey).verifying(maxLength(reasonMaxLength, maxLengthReasonKey)))).
      transform(toUniqueTaxReference, fromUniqueTaxReference)
  }
}
