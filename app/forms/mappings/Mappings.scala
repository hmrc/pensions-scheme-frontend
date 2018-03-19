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

  protected def schemeTypeMapping(requiredTypeKey: String = "messages__error__selection",
                                  invalidTypeKey: String = "messages__error__scheme_type_invalid",
                                  requiredOtherKey: String = "messages__error__scheme_type_information",
                                  invalidOtherKey: String = "messages__error__scheme_type_length"): Mapping[SchemeType] = {
    val schemeTypeDetailsMaxLength = 150
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
        BodyCorporate
      ).map(v => (v.toString, v)).toMap

      schemeTypeTuple match {
        case (key, Some(value)) if key == other => Other(value)
        case (key, _) if mappings.keySet.contains(key) => {
          mappings.apply(key)
        }
      }
    }

    tuple(
      "type" -> text(requiredTypeKey).verifying(schemeTypeConstraint(invalidTypeKey)),
      "schemeTypeDetails" -> mandatoryIfEqual("schemeType.type", other, text(requiredOtherKey).
        verifying(maxLength(schemeTypeDetailsMaxLength, invalidOtherKey)))
    ).transform(toSchemeType, fromSchemeType)
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

  protected def sortCodeMapping(requiredKey: String = "error.required", invalidKey: String, maxErrorKey: String): Mapping[SortCode] = {

    val formatter: Formatter[SortCode] = new Formatter[SortCode] {

      val baseFormatter: Formatter[String] = stringFormatter(requiredKey)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], SortCode] = {

        baseFormatter.bind(key, data)
          .right.map(_.trim.replaceAll("[ -]", ""))
          .right.flatMap {
          case str if !str.matches(regexSortCode)  =>
            Left(Seq(FormError(key, invalidKey)))
          case str if str.trim.replaceAll("[- ]", "").length > 6 =>
            Left(Seq(FormError(key, maxErrorKey)))
          case str =>
            str.sliding(2, 2).toList match {
              case a :: b :: c :: Nil =>
                Right(SortCode(a, b, c))
              case _ =>
                Left(Seq(FormError(key, invalidKey)))
            }
        }
      }

      override def unbind(key: String, value: SortCode): Map[String, String] =
        baseFormatter.unbind(key, s"${value.first} ${value.second} ${value.third}")
    }

    Forms.of(formatter)
  }

  protected def vatMapping(invalidKey: String, maxErrorKey: String): FieldMapping[String] = {
    of(vatFormatter(invalidKey, maxErrorKey))
  }

  protected def companyRegistrationNumberMapping(requiredKey: String = "messages__error__has_crn_company",
                                       requiredCRNKey: String = "messages__error__crn",
                                       requiredReasonKey: String = "messages__company__no_crn",
                                       reasonLengthKey: String = "messages__error__no_crn_length",
                                       invalidCRNKey: String = "messages__error__crn_invalid",
                                       noReasonKey: String = "messages__error__no_crn_company"):
  Mapping[CompanyRegistrationNumber] = {
    val reasonMaxLength =150

    def fromCompanyRegistrationNumber(crn: CompanyRegistrationNumber): (Boolean, Option[String], Option[String]) = {
      crn match {
        case CompanyRegistrationNumber.Yes(crn) => (true, Some(crn), None)
        case CompanyRegistrationNumber.No(reason) =>  (false, None, Some(reason))
      }
    }

    def toCompanyRegistrationNumber(crnTuple: (Boolean, Option[String], Option[String])) = {

      crnTuple match {
        case (true, Some(crn), None)  => CompanyRegistrationNumber.Yes(crn)
        case (false, None, Some(reason))  => CompanyRegistrationNumber.No(reason)
        case _ => throw new RuntimeException("Invalid selection")
      }
    }

    tuple("hasCrn" -> boolean(requiredKey),
      "crn" -> mandatoryIfTrue("companyRegistrationNumber.hasCrn", text(requiredCRNKey).verifying(validCrn(invalidCRNKey))),
      "reason" -> mandatoryIfFalse("companyRegistrationNumber.hasCrn", text(noReasonKey).
        verifying(maxLength(reasonMaxLength,reasonLengthKey)))).transform(toCompanyRegistrationNumber, fromCompanyRegistrationNumber)
  }

  protected def postCodeMapping(requiredKey: String, invalidKey: String): Mapping[Option[String]] = {

    def toPostCode(data: (Option[String], Option[String])): Option[String] = data._2

    def fromPostCode(data: Option[String]): (Option[String], Option[String]) = (data, data)

    tuple("postCode" -> mandatoryIfEqual[String]("country", "GB", text(requiredKey).verifying(
      regexp(postCodeRegex, invalidKey))),
      "postCode" -> optional(text(requiredKey))).transform(toPostCode, fromPostCode)
  }
}
