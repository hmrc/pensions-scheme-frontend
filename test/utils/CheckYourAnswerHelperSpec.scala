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

package utils

import base.SpecBase
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.individual.{AddressId, EstablisherDetailsId, EstablisherNinoId, UniqueTaxReferenceId}
import models.{CheckMode, EstablisherNino, Index}
import models.addresslookup.Address
import models.register.{CountryOptions, SchemeDetails, SchemeType}
import models.register.establishers.individual.EstablisherDetails
import models.register.establishers.individual.UniqueTaxReference.{No, Yes}
import org.joda.time.LocalDate
import play.api.libs.json._
import viewmodels.AnswerRow

class CheckYourAnswerHelperSpec extends SpecBase {
  val inputOptions = Seq(InputOption("GB", "United Kingdom"))
  val countryOptions: CountryOptions = new CountryOptions(inputOptions)
  val firstIndex = Index(0)

  def checkYourAnswerHelper(userAnswers: UserAnswers): CheckYourAnswersHelper = new CheckYourAnswersHelper(
    userAnswers, countryOptions
  )

  "address" must {
    "return the AnswerRows for the address" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now),
            AddressId.toString ->
              Address("address line 1", "address line 2", Some("address line 3"), None, Some("AB1 1AB"), "GB"))
        ))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.address(firstIndex) mustEqual Seq(AnswerRow("address.checkYourAnswersLabel", Seq("address line 1,", "address line 2,",
        "address line 3,", "AB1 1AB,", "United Kingdom"), false,
        controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, firstIndex).url))
    }

    "return the AnswerRows for uniqueTaxReference when answered Yes" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now),
            UniqueTaxReferenceId.toString ->
              Yes("1115676787"))
        ))
      val seqAnswers = Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq("Yes"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("uniqueTaxReference.utr.checkYourAnswersLabel", Seq("1115676787"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.uniqueTaxReference(firstIndex) mustEqual seqAnswers
    }

    "return the AnswerRows for uniqueTaxReference when answered No" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now),
            UniqueTaxReferenceId.toString ->
              No("Didn't receive"))
        ))
      val seqAnswers = Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq("No"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("uniqueTaxReference.reason.checkYourAnswersLabel", Seq("Didn't receive"), false,
          controllers.register.establishers.individual.routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.uniqueTaxReference(firstIndex) mustEqual seqAnswers
    }

    "return the AnswerRows for establisher Nino when answered Yes" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now),
            EstablisherNinoId.toString ->
              EstablisherNino.Yes("test Nino"))
        ))
      val seqAnswers = Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq("Yes"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherNino.nino.checkYourAnswersLabel", Seq("test Nino"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.establisherNino(firstIndex) mustEqual seqAnswers
    }

    "return the AnswerRows for establisher Nino when answered No" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now),
            EstablisherNinoId.toString ->
              EstablisherNino.No("Didn't receive"))
        ))
      val seqAnswers = Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq("No"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherNino.reason.checkYourAnswersLabel", Seq("Didn't receive"), false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.establisherNino(firstIndex) mustEqual seqAnswers
    }

    "return the AnswerRows for establisher details" in {
      val json = Json.obj(SchemeDetailsId.toString -> Json.toJson(
        SchemeDetails("value 1", SchemeType.SingleTrust)),
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherDetailsId.toString ->
              EstablisherDetails("test first name", "test last name", LocalDate.now))
        ))
      val seqAnswers = Seq(
        AnswerRow("establisherDetails.name.checkYourAnswersLabel", Seq("test first name test last name"), false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherDetails.dateOfBirth.checkYourAnswersLabel", Seq(s"${DateHelper.formatDate(LocalDate.now)}"), false,
          controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url))

      val userAnswers = new UserAnswers(json)
      val result = checkYourAnswerHelper(userAnswers)
      result.establisherDetails(firstIndex) mustEqual seqAnswers
    }
  }
}
