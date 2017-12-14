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

package forms.register

import forms.behaviours.FormBehaviours
import models.{Field, Required, SchemeDetails, SchemeType}
import org.scalatest.MustMatchers

class SchemeDetailsFormProviderSpec extends FormBehaviours {

  val validData: Map[String, String] = Map(
    "schemeName" -> "scheme Name 1",
    "schemeType" -> "other",
    "schemeTypeDetails" -> "some value"
  )

  val form = new SchemeDetailsFormProvider()()

  "SchemeDetails form" must {

    behave like formWithMandatoryTextFields(
      Field("schemeName", Required -> "schemeDetails.schemeName.error.required"),
      Field("schemeType", Required -> "schemeDetails.schemeType.error.required"))

    "successfully bind when the schemeType is other with schemeTypeDetails and have valid scheme name" in {
      val result = form.bind(Map(
        "schemeName" -> "scheme Name 1",
        "schemeType" -> "other",
        "schemeTypeDetails" -> "some value")).get

      result shouldEqual SchemeDetails("scheme Name 1", SchemeType.Other("some value"))
    }

    "successfully bind when the schemeType is singleTrust and have valid scheme name" in {
      val result = form.bind(Map(
        "schemeName" -> "scheme Name 1",
        "schemeType" -> "singleTrust")).get

      result shouldEqual SchemeDetails("scheme Name 1", SchemeType.SingleTrust)
    }

    "successfully bind when the schemeType is GroupLifeDeath and have valid scheme name" in {
      val result = form.bind(Map(
        "schemeName" -> "scheme Name 1",
        "schemeType" -> "groupLifeDeath")).get

      result shouldEqual SchemeDetails("scheme Name 1", SchemeType.GroupLifeDeath)
    }

    "successfully bind when the schemeType is BodyCorporate and have valid scheme name" in {
      val result = form.bind(Map(
        "schemeName" -> "scheme Name 1",
        "schemeType" -> "bodyCorporate")).get

      result shouldEqual SchemeDetails("scheme Name 1", SchemeType.BodyCorporate)
    }

    "fail to bind when there is no schemeType" in {
      val data = Map(
        "schemeName" -> "scheme Name 1")
      val expectedError = error("schemeType", "schemeDetails.schemeType.error.required")
      checkForError(form, data, expectedError)
    }

    "fail to bind when the the schemeType is other without any schemeTypeDetails" in {
      val data = Map(
        "schemeName" -> "scheme Name 1",
        "schemeType" -> "other")
      val expectedError = error("schemeTypeDetails", "schemeDetails.schemeType.error.invalid")
      checkForError(form, data, expectedError)
    }
  }
}
