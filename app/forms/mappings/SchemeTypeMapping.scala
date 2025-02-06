/*
 * Copyright 2024 HM Revenue & Customs
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

import models.register.SchemeType
import models.register.SchemeType.{BodyCorporate, GroupLifeDeath, MasterTrust, Other, SingleTrust}
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual

trait SchemeTypeMapping extends Formatters with Constraints with Mappings {

  protected def schemeTypeMapping(requiredTypeKey: String = "messages__scheme_type__error__required",
                                  invalidTypeKey: String = "messages__error__scheme_type_invalid",
                                  requiredOtherKey: String = "messages__error__scheme_type_information",
                                  lengthOtherKey: String = "messages__error__scheme_type_other_length",
                                  invalidOtherKey: String = "messages__error__scheme_type_other_invalid")
  : Mapping[SchemeType] = {
    val schemeTypeDetailsMaxLength = 160
    val other = "other"

    def fromSchemeType(schemeType: SchemeType): (String, Option[String]) = {
      schemeType match {
        case SchemeType.Other(someValue) => (other, Some(someValue))
        case _ => (schemeType.toString, None)
      }
    }

    def toSchemeType(schemeTypeTuple: (String, Option[String])): SchemeType = {

      val mappings: Map[String, SchemeType] = Seq(
        SingleTrust,
        GroupLifeDeath,
        BodyCorporate,
        MasterTrust
      ).map(v => (v.toString, v)).toMap

      schemeTypeTuple match {
        case (key, Some(value)) if key == other => Other(value)
        case (key, _) if mappings.keySet.contains(key) =>
          mappings.apply(key)
        case _ => throw new Exception(s"Invalid scheme type: ${schemeTypeTuple._1}")
      }
    }

    tuple(
      "type" -> text(requiredTypeKey).verifying(schemeTypeConstraint(invalidTypeKey)),
      "schemeTypeDetails" -> mandatoryIfEqual("schemeType.type", other, text(requiredOtherKey).
        verifying(firstError(
          maxLength(schemeTypeDetailsMaxLength, lengthOtherKey),
          tightText(invalidOtherKey))))
    ).transform(toSchemeType, fromSchemeType)
  }
}
