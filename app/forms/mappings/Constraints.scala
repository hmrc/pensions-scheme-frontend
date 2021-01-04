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

package forms.mappings

import models.register.SchemeType
import java.time.LocalDate
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino
import utils.{CountryOptions, NameMatching}

import scala.language.implicitConversions

trait Constraints {
  val regexPostcode = """^[A-Z]{1,2}[0-9][0-9A-Z]?\s?[0-9][A-Z]{2}$"""
  val regexPostCodeNonUk = """^([0-9]+-)*[0-9]+$"""
  val regexSortCode: String = """\d{6,}""".r.toString()
  val regexUtr = """^\d{10}$"""
  val regexName = """^[a-zA-Z &`\-\'\.^]{1,35}$"""
  val regexPersonOrOrganisationName =   """^[a-zA-Z\u00C0-\u00FF '‘’\u2014\u2013\u2010\u002d]{1,107}"""
  val regexUserResearch = """^[a-zA-Z\u00C0-\u00FF '‘’\u2014\u2013\u2010\u002d]{1,160}$"""
  val regexAccountNo = """[0-9]*"""
  val regexEmailRestrictive: String = "^(?:[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
    "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")" +
    "@(?:(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?|" +
    "\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-zA-Z0-9-]*[a-zA-Z0-9]:" +
    "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])$"
  val regexPhoneNumber ="""^[0-9 ()+--]{1,24}$"""
  val regexCrn = "^[A-Za-z0-9 -]{7,8}$"
  val regexVat = """^\d{9}$"""
  val regexPaye = """^[0-9]{3}[0-9A-Za-z]{1,13}$"""
  val regexSafeText = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,160}$"""
  val regexAddressLine = """^[A-Za-z0-9 &!'‘’(),./\u2014\u2013\u2010\u002d]{1,35}$"""
  val adviserNameRegex = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\\[\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,107}$"""
  val regexPolicyNumber = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’"“”«»()*+,./:;=?@\\\[\]|~£€¥\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,55}$"""

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

  protected def exactLength(exact: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length == exact =>
        Valid
      case _ =>
        Invalid(errorKey, exact)
    }

  protected def psaNameMatch(psaName: String, errorKey: String): Constraint[String] = {
    Constraint {
      input =>

        if (NameMatching(input, psaName).isMatch) {
          Invalid(errorKey)
        } else {
          Valid
        }
    }
  }

  protected def schemeTypeConstraint(invalidKey: String): Constraint[String] = {

    val validSchemeTypes: Seq[String] = Seq(SchemeType.SingleTrust.toString,
      SchemeType.GroupLifeDeath.toString, SchemeType.BodyCorporate.toString, SchemeType.MasterTrust.toString, "other")

    Constraint {
      case schemeType if validSchemeTypes.contains(schemeType) => Valid
      case _ => Invalid(invalidKey)
    }
  }


  protected def futureDate(invalidKey: String): Constraint[LocalDate] = {
    Constraint {
      case date if date.isAfter(LocalDate.now()) => Invalid(invalidKey)
      case _ => Valid
    }
  }

  protected def notBeforeYear(errorKey: String, year:Int): Constraint[LocalDate] =
    Constraint {
      case date if date.getYear >= year => Valid
      case _ => Invalid(errorKey)
    }

  protected def validNino(invalidKey: String): Constraint[String] = {
    Constraint {
      case nino if Nino.isValid(nino) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def validCrn(invalidKey: String): Constraint[String] = {
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

  protected def country(countryOptions: CountryOptions, errorKey: String): Constraint[String] =
    Constraint {
      input =>
        countryOptions.options
          .find(_.value == input)
          .map(_ => Valid)
          .getOrElse(Invalid(errorKey))
    }

  implicit def convertToOptionalConstraint[T](constraint: Constraint[T]): Constraint[Option[T]] =
    Constraint {
      case Some(t) => constraint.apply(t)
      case _ => Valid
    }

  protected def emailAddressRestrictive(errorKey: String): Constraint[String] = regexp(regexEmailRestrictive, errorKey)

  protected def postCode(errorKey: String): Constraint[String] = regexp(regexPostcode, errorKey)

  protected def postCodeNonUk(errorKey: String): Constraint[String] = regexp(regexPostCodeNonUk, errorKey)

  protected def addressLine(errorKey: String): Constraint[String] = regexp(regexAddressLine, errorKey)

  protected def phoneNumber(errorKey: String): Constraint[String] = regexp(regexPhoneNumber, errorKey)

  protected def vatRegistrationNumber(errorKey: String): Constraint[String] = regexp(regexVat, errorKey)

  protected def payeEmployerReferenceNumber(errorKey: String): Constraint[String] = regexp(regexPaye, errorKey)

  protected def safeText(errorKey: String): Constraint[String] = regexp(regexSafeText, errorKey)

  protected def personOrOrganisationName(errorKey:String): Constraint[String] = regexp(regexPersonOrOrganisationName, errorKey)

  protected def name(errorKey: String): Constraint[String] = regexp(regexName, errorKey)

  protected def adviserName(errorKey: String): Constraint[String] = regexp(adviserNameRegex, errorKey)

  protected def userResearchName(errorKey: String): Constraint[String] = regexp(regexUserResearch, errorKey)

  protected def policyNumber(errorKey: String): Constraint[String] = regexp(regexPolicyNumber, errorKey)
}
