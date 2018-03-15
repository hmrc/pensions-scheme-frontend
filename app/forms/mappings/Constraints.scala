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

import models.register.{CountryOptions, SchemeType}
import org.joda.time.LocalDate
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino

trait Constraints {

  val postCodeRegex = "^(?i)[A-Z]{1,2}[0-9][0-9A-Z]?[ ]?[0-9][A-Z]{2}"
  val regexSortCode: String = """\d*""".r.toString()
  val regexUtr = "^[0-9]{10}$"
  val regexFirstName = "[a-zA-Z]{1}[a-zA-Z-‘]*"
  val regexMiddleName ="[a-zA-Z-‘]*"
  val regexLastName = "[a-zA-Z0-9,.‘(&)-/ ]*"
  val regexAccountNo = "[0-9]*"
  val emailRegex = "^[^@<>‘“]+@[^@<>‘“]+$"
  val regexPhoneNumber ="^[0-9 +()-]+$"

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def schemeTypeConstraint(invalidKey: String): Constraint[String] = {

    val validSchemeTypes: Seq[String] = Seq(SchemeType.SingleTrust.toString,
      SchemeType.GroupLifeDeath.toString, SchemeType.BodyCorporate.toString, "other")

    Constraint {
      case schemeType if(validSchemeTypes.contains(schemeType)) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def validCountries(invalidKey: String, countries: CountryOptions): Constraint[String] = {
    val validCountries = countries.options.map(_.value)

    Constraint {
      case country if(validCountries.contains(country)) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def futureDate(invalidKey: String): Constraint[LocalDate] = {
    Constraint {
      case date if(date.isAfter(LocalDate.now())) => Invalid(invalidKey)
      case _ => Valid
    }
  }

  protected def validNino(invalidKey: String) : Constraint[String] = {
    Constraint {
      case nino if(Nino.isValid(nino.replaceAll(" ", "").toUpperCase)) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def validCrn(invalidKey: String) : Constraint[String] = {
    val validCrnString = "^(\\d{7}|[A-Z]\\d{6}|[A-Z][A-Z]\\d{6})$"

    Constraint {
      case crn if (crn.replaceAll(" ", "").toUpperCase).matches(validCrnString) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  def returnOnFirstFailure[T](constraints: Constraint[T]*): Constraint[T] =
    Constraint {
      field =>
        constraints
          .map(_.apply(field))
          .filterNot(_ == Valid)
          .headOption.getOrElse(Valid)
  }
}
