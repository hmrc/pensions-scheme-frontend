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

import models.EstablisherNino
import models.register.establishers.individual.UniqueTaxReference
import models.register.{SchemeType, SortCode}
import org.apache.commons.lang3.RandomStringUtils
import org.joda.time.LocalDate
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.data.{Form, FormError}
import utils.Enumerable

object MappingsSpec {

  sealed trait Foo
  case object Bar extends Foo
  case object Baz extends Foo

  object Foo {

    val values: Set[Foo] = Set(Bar, Baz)

    implicit val fooEnumerable: Enumerable[Foo] =
      Enumerable(values.toSeq.map(v => v.toString -> v): _*)
  }
}

class MappingsSpec extends WordSpec with MustMatchers with OptionValues with Mappings {

  import MappingsSpec._

  "text" must {

    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "return a custom error message" in {
      val form = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "boolean" must {

    val testForm: Form[Boolean] =
      Form(
        "value" -> boolean()
      )

    "bind true" in {
      val result = testForm.bind(Map("value" -> "true"))
      result.get mustEqual true
    }

    "bind false" in {
      val result = testForm.bind(Map("value" -> "false"))
      result.get mustEqual false
    }

    "not bind a non-boolean" in {
      val result = testForm.bind(Map("value" -> "not a boolean"))
      result.errors must contain(FormError("value", "error.boolean"))
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind" in {
      val result = testForm.fill(true)
      result.apply("value").value.value mustEqual "true"
    }
  }

  "int" must {

    val testForm: Form[Int] =
      Form(
        "value" -> int()
      )

    "bind a valid integer" in {
      val result = testForm.bind(Map("value" -> "1"))
      result.get mustEqual 1
    }

    "not bind an empty value" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "unbind a valid value" in {
      val result = testForm.fill(123)
      result.apply("value").value.value mustEqual "123"
    }
  }

  "enumerable" must {

    val testForm = Form(
      "value" -> enumerable[Foo]()
    )

    "bind a valid option" in {
      val result = testForm.bind(Map("value" -> "Bar"))
      result.get mustEqual Bar
    }

    "not bind an invalid option" in {
      val result = testForm.bind(Map("value" -> "Not Bar"))
      result.errors must contain(FormError("value", "error.invalid"))
    }

    "not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }
  }

  "schemeType" must {
    val validSchemeTypeDetailsLength = 150
    val invalidSchemeTypeDetailsLength = 151

    val testForm: Form[SchemeType] = Form(
      "schemeType" -> schemeTypeMapping("schemeType.error.required", "schemeType.error.invalid",
        "messages__error__scheme_type_information", "messages__error__scheme_type_length")
    )

    "bind a valid schemeType SingleTrust" in {
      val result = testForm.bind(Map("schemeType.type" -> "single"))
      result.get mustEqual SchemeType.SingleTrust
    }

    "bind a valid schemeType GroupLifeDeath" in {
      val result = testForm.bind(Map("schemeType.type" -> "group"))
      result.get mustEqual SchemeType.GroupLifeDeath
    }

    "bind a valid schemeType BodyCorporate" in {
      val result = testForm.bind(Map("schemeType.type" -> "corp"))
      result.get mustEqual SchemeType.BodyCorporate
    }

    "bind a valid schemeType Other" in {
      val result = testForm.bind(Map("schemeType.type" -> "other", "schemeType.schemeTypeDetails" -> "some value"))
      result.get mustEqual SchemeType.Other("some value")
    }

    "not bind an empty Map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("schemeType.type", "schemeType.error.required"))
    }

    "not bind a Map with invalid schemeType" in {
      val result = testForm.bind(Map("schemeType.type" -> "Invalid"))
      result.errors must contain(FormError("schemeType.type", "schemeType.error.invalid"))
    }

    "not bind a Map with type other but no schemeTypeDetails" in {
      val result = testForm.bind(Map("schemeType.type" -> "other"))
      result.errors must contain(FormError("schemeType.schemeTypeDetails", "messages__error__scheme_type_information"))
    }

    "not bind a Map with type other and schemeTypeDetails exceeds max length 150" in {
      val testString = RandomStringUtils.random(invalidSchemeTypeDetailsLength)
      val result = testForm.bind(Map("schemeType.type" -> "other", "schemeType.schemeTypeDetails" -> testString))
      result.errors must contain(FormError("schemeType.schemeTypeDetails", "messages__error__scheme_type_length",
        Seq(validSchemeTypeDetailsLength)))
    }

    "unbind a valid schemeType SingleTrust" in {
      val result = testForm.fill(SchemeType.SingleTrust)
      result.apply("schemeType.type").value.value mustEqual "single"
    }

    "unbind a valid schemeType GroupLifeDeath" in {
      val result = testForm.fill(SchemeType.GroupLifeDeath)
      result.apply("schemeType.type").value.value mustEqual "group"
    }

    "unbind a valid schemeType BodyCorporate" in {
      val result = testForm.fill(SchemeType.BodyCorporate)
      result.apply("schemeType.type").value.value mustEqual "corp"
    }

    "unbind a valid schemeType Other" in {
      val result = testForm.fill(SchemeType.Other("some value"))
      result.apply("schemeType.type").value.value mustEqual "other"
      result.apply("schemeType.schemeTypeDetails").value.value mustEqual "some value"
    }
  }

  "date" must {
    val testForm: Form[LocalDate] = Form("date"->dateMapping("messages__error__date"))

    "bind a valid date" in {
      val result = testForm.bind(Map("date.day" -> "1", "date.month" -> "5", "date.year" -> LocalDate.now().getYear.toString))
      result.get mustEqual new LocalDate(LocalDate.now().getYear, 5, 1)
    }

    "not bind an invalid Date" in {
      val result = testForm.bind(Map("date.day" -> "31", "date.month" -> "2", "date.year" -> LocalDate.now().getYear.toString))
      result.errors mustEqual Seq(FormError("date", "messages__error__date"))
    }

    "not bind an empty Map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors mustEqual Seq(FormError("date.day", "messages__error__date"),
        FormError("date.month", "messages__error__date"), FormError("date.year", "messages__error__date"))
    }

    "unbind a valid date" in {
      val result = testForm.fill(new LocalDate(LocalDate.now().getYear, 6, 1))
      result.apply("date.day").value.value mustEqual "1"
      result.apply("date.month").value.value mustEqual "6"
      result.apply("date.year").value.value mustEqual LocalDate.now().getYear.toString
    }
  }

  "sortCode" must {

    val testForm: Form[SortCode] = Form("sortCode" -> sortCodeMapping("error.required", "error.invalid", "error.max.error"))

    Seq("12 34 56", "12-34-56", " 123456").foreach{ sortCode =>
      s"bind a valid sort code $sortCode" in {
        val result = testForm.bind(Map("sortCode" -> sortCode))
        result.get mustEqual SortCode("12", "34", "56")
      }
    }

    "not bind an empty Map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors mustEqual Seq(FormError("sortCode", "error.required"))
    }

    Seq("12%34&56", "abdgfg").foreach { sortCode =>
      s"not bind an invalid sort code $sortCode" in {
        val result = testForm.bind(Map("sortCode" -> sortCode))
        result.errors mustEqual Seq(FormError("sortCode", "error.invalid"))
      }
    }

    Seq("12 34 56 56", "12345678").foreach { sortCode =>
      s"not bind a sort code $sortCode which exceeds max length" in {
        val result = testForm.bind(Map("sortCode" -> sortCode))
        result.errors mustEqual Seq(FormError("sortCode", "error.max.error"))
      }
    }
  }

  "uniqueTaxReference" must {

    val regexUtr = "\\d{10}"

    val testForm: Form[UniqueTaxReference] = Form("uniqueTaxReference" -> uniqueTaxReferenceMapping("error.required",
      "error.utr.required", "error.reason.required", "error.utr.invalid", "error.reason.length"))

    "bind a valid uniqueTaxReference with utr when yes is selected" in {
      val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true", "uniqueTaxReference.utr" -> "1234556676"))
      result.get mustEqual UniqueTaxReference.Yes("1234556676")
    }

    "bind a valid uniqueTaxReference with reason when no is selected" in {
      val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "false", "uniqueTaxReference.reason" -> "haven't got utr"))
      result.get mustEqual UniqueTaxReference.No("haven't got utr")
    }

    "not bind an empty Map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors mustEqual Seq(FormError("uniqueTaxReference.hasUtr", "error.required"))
    }

    Seq("1234", "12345678766655", "sdfghjkloi").foreach { utr =>
      s"not bind an invalid utr $utr" in {
        val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true", "uniqueTaxReference.utr" -> utr))
        result.errors mustEqual Seq(FormError("uniqueTaxReference.utr", "error.utr.invalid", Seq(regexUtr)))
      }
    }

    "not bind a uniqueTaxReference without utr when yes is selected" in {
      val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "true"))
      result.errors mustEqual Seq(FormError("uniqueTaxReference.utr", "error.utr.required"))
    }

    "not bind a uniqueTaxReference without reason when no is selected" in {
      val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "false"))
      result.errors mustEqual Seq(FormError("uniqueTaxReference.reason", "error.reason.required"))
    }

    "not bind a reason greater than 150 characters" in {
      val reason = RandomStringUtils.randomAlphabetic(151)
      val result = testForm.bind(Map("uniqueTaxReference.hasUtr" -> "false", "uniqueTaxReference.reason" -> reason))
      result.errors mustEqual Seq(FormError("uniqueTaxReference.reason", "error.reason.length", Seq(150)))
    }
  }

  "establisherNino" must {

    val testForm: Form[EstablisherNino] = Form("establisherNino" ->  establisherNinoMapping())

    "fail to bind when yes is selected but NINO is not provided" in {
      val result = testForm.bind(Map("establisherNino.hasNino" -> "true"))
      result.errors mustEqual Seq(FormError("establisherNino.nino", "messages__error__nino"))
    }

    "fail to bind when no is selected but reason is not provided" in {
      val result = testForm.bind(Map("establisherNino.hasNino" -> "false"))
      result.errors mustEqual Seq(FormError("establisherNino.reason", "messages__establisher__no_nino"))
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val result = testForm.bind(Map("establisherNino.hasNino" -> "true", "establisherNino.nino" -> nino))
        result.errors mustEqual Seq(FormError("establisherNino.nino", "messages__error__nino_invalid"))
      }
    }

    "fail to bind when no is selected and reason exceeds max length of 150" in {
      val testString = RandomStringUtils.randomAlphabetic(151)
      val result = testForm.bind(Map("establisherNino.hasNino" -> "false", "establisherNino.reason" -> testString))
      result.errors mustEqual Seq(FormError("establisherNino.reason", "messages__error__no_nino_length", Seq(150)))
    }
  }

  "vat number" must {

    val testForm = Form("vatNumber" -> vatMapping("error.invalid", "error.maxlength"))

    Seq("GB123456789", "123435464", "gb123456789").foreach{ vatNo =>
      s"successfully bind valid vat number $vatNo" in {
        val coForm = testForm.bind(Map("vatNumber" -> vatNo))

        coForm.get mustEqual vatNo.toUpperCase.replace("GB", "")
      }
    }

    Seq("AB123456", "GB", "12345ff56", "12345").foreach { vatNo =>
      s"fail to bind when vat number $vatNo is not valid" in {
        val coForm = testForm.bind(Map("vatNumber" -> vatNo))

        coForm.errors mustEqual Seq(FormError("vatNumber", "error.invalid"))
      }
    }

    Seq("GB1234568908", "1234567898").foreach { vatNo =>
      s"fail to bind when vat number $vatNo exceeds max lenght 9" in {
        val coForm = testForm.bind(Map("vatNumber" -> vatNo))

        coForm.errors mustEqual Seq(FormError("vatNumber", "error.maxlength"))
      }
    }
  }
}
