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

import models.Paye
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

trait PayeMapping extends Mappings with Transforms {

  def payeMapping(requiredKey: String = "messages__error__has_paye_establisher",
                  requiredPayeKey: String = "messages__error__paye_required",
                  invalidPayeKey: String = "messages__error__paye_invalid",
                  payeLengthKey: String = "messages__error__paye_length"):
  Mapping[Paye] = {

    tuple("hasPaye" -> boolean(requiredKey),
      "paye" -> mandatoryIfTrue(
        "paye.hasPaye",
        text(requiredPayeKey)
          .transform(payeTransform, noTransform)
          .verifying(
            firstError(
              maxLength(PayeMapping.maxPayeLength, payeLengthKey),
              payeEmployerReferenceNumber(invalidPayeKey)
            )
          )
      )
    ).transform(toPaye, fromPaye)
  }

  private[this] def fromPaye(paye: Paye): (Boolean, Option[String]) = {
    paye match {
      case Paye.Yes(payeNo) => (true, Some(payeNo))
      case Paye.No => (false, None)
    }
  }

  private[this] def toPaye(payeTuple: (Boolean, Option[String])): Paye = {
    payeTuple match {
      case (true, Some(paye)) => Paye.Yes(paye)
      case (false, None) => Paye.No
      case _ => throw new RuntimeException("Invalid selection")
    }
  }
}

object PayeMapping {
  val maxPayeLength = 16
}


