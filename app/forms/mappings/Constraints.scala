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

  val regexPostcode = "^(?i)[A-Z]{1,2}[0-9][0-9A-Z]?[ ]?[0-9][A-Z]{2}"
  val regexSortCode: String = """\d*""".r.toString()
  val regexUtr = "^[0-9]{10}$"
  val regexName = """^[a-zA-Z\u00C0-\u00FF'‘’\u2014\u2013\u2010\u002d]{1,35}$"""
  val regexAccountNo = "[0-9]*"
  val regexEmail = "^[^@<>‘“]+@[^@<>‘“]+$"
  val regexPhoneNumber ="^[0-9 +()-]+$"
  val regexCrn = "^[A-Za-z0-9 -]{7,8}$"
  val regexVat = """^\d{9}$"""
  val regexPaye = """^[0-9]{3}[0-9A-Za-z]{1,13}$"""
  val regexSafeText = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,160}$"""
  val regexNino = "^[0-9a-zA-Z]{1,9}|((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D]?$"
  val regexAddressLine = """^[A-Za-z0-9 !'‘’"“”(),./\u2014\u2013\u2010\u002d]{1,35}$"""




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
      case schemeType if validSchemeTypes.contains(schemeType) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def validCountries(invalidKey: String, countries: CountryOptions): Constraint[String] = {
    val validCountries = countries.options.map(_.value)

    Constraint {
      case country if validCountries.contains(country) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def futureDate(invalidKey: String): Constraint[LocalDate] = {
    Constraint {
      case date if date.isAfter(LocalDate.now()) => Invalid(invalidKey)
      case _ => Valid
    }
  }

  protected def validNino(invalidKey: String) : Constraint[String] = {
    Constraint {
      case nino if Nino.isValid(nino.replaceAll(" ", "").toUpperCase) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def validCrn(invalidKey: String) : Constraint[String] = {
    Constraint {
      case crn if crn.matches(regexCrn) => Valid
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

  protected def emailAddress(errorKey: String): Constraint[String] = regexp(regexEmail, errorKey)

  protected def phoneNumber(errorKey: String): Constraint[String] = regexp(regexPhoneNumber, errorKey)

  protected def vatRegistrationNumber(errorKey: String): Constraint[String] = regexp(regexVat, errorKey)

  protected def payeEmployerReferenceNumber(errorKey: String): Constraint[String] = regexp(regexPaye, errorKey)

  protected def safeText(errorKey: String): Constraint[String] = regexp(regexSafeText, errorKey)


}
