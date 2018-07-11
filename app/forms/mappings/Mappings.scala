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

import org.joda.time.LocalDate
import play.api.data.Forms.{of, _}
import play.api.data.{FieldMapping, Mapping}
import utils.Enumerable

import scala.util.Try

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def optionalText(): FieldMapping[Option[String]] =
    of(optionalStringFormatter)

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

    def toLocalDate(date: (String, String, String)): LocalDate = {
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

    tuple(
      "day" -> text(invalidKey),
      "month" -> text(invalidKey),
      "year" -> text(invalidKey))
      .verifying(invalidKey, validateDate(_))
      .transform(toLocalDate, fromLocalDate)
  }
}
