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

import models.Nino
import models.register.{SchemeType, SortCode}
import models.register.SchemeType.{BodyCorporate, GroupLifeDeath, Other, SingleTrust}
import models.register.establishers.individual.UniqueTaxReference
import org.joda.time.LocalDate
import play.api.data.Forms.{of, _}
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError, Forms, Mapping}
import uk.gov.voa.play.form.ConditionalMappings._
import utils.Enumerable
import models._

import scala.util.Try

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

  protected def dateMapping(invalidKey: String): Mapping[LocalDate] = {

    def toLocalDate(date: (String, String, String)): LocalDate =
    {
      date match {
        case (day, month, year) =>
          new LocalDate(year.toInt, month.toInt, day.toInt)
      }
    }

    def fromLocalDate(date: LocalDate): (String, String, String) = {
      (date.getDayOfMonth.toString, date.getMonthOfYear.toString, date.getYear.toString)
    }

    def validateDate(date: (String, String, String)): Boolean =
      Try(toLocalDate(date)).isSuccess

    tuple("day" -> text(invalidKey),
      "month" -> text(invalidKey),
      "year" -> text(invalidKey)).verifying(invalidKey, validateDate(_)).transform(toLocalDate, fromLocalDate)
  }

  protected def uniqueTaxReferenceMapping(
                                           key: String = "uniqueTaxReference",
                                           requiredKey: String = "messages__error__has_sautr_establisher",
                                           requiredUtrKey: String = "messages__error__sautr",
                                           requiredReasonKey: String = "messages__error__no_sautr_establisher",
                                           invalidUtrKey: String = "messages__error__sautr_invalid",
                                           maxLengthReasonKey: String = "messages__error__no_sautr_length"):
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
    "utr" -> mandatoryIfTrue(s"$key.hasUtr", text(requiredUtrKey).verifying(regexp(regexUtr, invalidUtrKey))),
    "reason" -> mandatoryIfFalse(s"$key.hasUtr",
      text(requiredReasonKey).verifying(maxLength(reasonMaxLength, maxLengthReasonKey)))).
      transform(toUniqueTaxReference, fromUniqueTaxReference)
  }

  protected def postCodeMapping(requiredKey: String, invalidKey: String): Mapping[Option[String]] = {

    def toPostCode(data: (Option[String], Option[String])): Option[String] = data._2

    def fromPostCode(data: Option[String]): (Option[String], Option[String]) = (data, data)

    tuple("postCode" -> mandatoryIfEqual[String]("country", "GB", text(requiredKey).verifying(
      regexp(postCodeRegex, invalidKey))),
      "postCode" -> optional(text(requiredKey))).transform(toPostCode, fromPostCode)
  }
}
