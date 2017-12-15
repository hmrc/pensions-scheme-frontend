/*
 * Copyright 2017 HM Revenue & Customs
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

import models.SchemeType
import models.SchemeType.{BodyCorporate, GroupLifeDeath, Other, SingleTrust}
import play.api.data.{FieldMapping, Mapping}
import play.api.data.Forms.of
import utils.Enumerable
import play.api.data.Forms._
import uk.gov.voa.play.form.ConditionalMappings._

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric"): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))

  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid")(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey))

  protected def schemeTypeMapping(requiredTypeKey: String, invalidTypeKey: String,
                                  requiredOtherKey: String, invalidOtherKey: String): Mapping[SchemeType] = {
    tuple(
      "type" -> text(requiredTypeKey).verifying(schemeTypeConstraint(invalidTypeKey)),
      "schemeTypeDetails" -> mandatoryIfEqual("schemeType.type", "Other", text(requiredOtherKey).
        verifying(maxLength(150, invalidOtherKey)))
    ).transform(toSchemeType, fromSchemeType)
  }

  def fromSchemeType(schemeType: SchemeType): (String, Option[String]) = {
    schemeType match {
      case SchemeType.Other(someValue) => ("Other", Some(someValue))
      case _ => (schemeType.toString, None)
    }
  }

  def toSchemeType(schemeTypeTuple: (String, Option[String])): SchemeType = {

    val mappings: Map[String, SchemeType] = Seq(
      SingleTrust,
      GroupLifeDeath,
      BodyCorporate
    ).map(v => (v.toString, v)).toMap

    schemeTypeTuple match {
      case ("Other", Some(value)) => Other(value)
      case (key, _) if mappings.keySet.contains(key) => {
        mappings.apply(key)
      }
    }
  }
}
