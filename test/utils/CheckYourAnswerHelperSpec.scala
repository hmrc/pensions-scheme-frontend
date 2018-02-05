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
import identifiers.register.establishers.individual._
import models.{CheckMode, EstablisherNino, Index}
import models.addresslookup.Address
import models.register.{CountryOptions, SchemeDetails, SchemeType}
import models.register.establishers.individual.{AddressYears, ContactDetails, EstablisherDetails}
import models.register.establishers.individual.UniqueTaxReference.{No, Yes}
import org.joda.time.LocalDate
import play.api.libs.json._
import viewmodels.AnswerRow
import controllers.register.establishers.individual.routes._

class CheckYourAnswerHelperSpec extends SpecBase {
  val inputOptions = Seq(InputOption("GB", "United Kingdom"))
  val countryOptions: CountryOptions = new CountryOptions(inputOptions)
  val firstIndex = Index(0)

  def checkYourAnswerHelper(userAnswers: UserAnswers): CheckYourAnswersHelper = new CheckYourAnswersHelper(
    userAnswers, countryOptions
  )

  "check your answer helper" must {
    "return the AnswerRows for the address" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              AddressId.toString ->
                Address("address line 1", "address line 2", Some("address line 3"), None, Some("AB1 1AB"), "GB"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("address.checkYourAnswersLabel", Seq("address line 1,", "address line 2,",
          "address line 3,", "AB1 1AB,", "United Kingdom"), answerIsMessageKey = false, AddressController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).address(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for the previous address" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              PreviousAddressId.toString ->
                Address("address line 1", "address line 2", None, Some("address line 4"), Some("AB1 1AB"), "GB"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("previousAddress.checkYourAnswersLabel", Seq("address line 1,", "address line 2,",
          "address line 4,", "AB1 1AB,", "United Kingdom"), answerIsMessageKey = false, PreviousAddressController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).previousAddress(firstIndex) mustEqual expectedOutput
    }


    "return the AnswerRows for uniqueTaxReference when answered Yes" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              UniqueTaxReferenceId.toString ->
                Yes("1115676787"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq("Yes"), false, UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("uniqueTaxReference.utr.checkYourAnswersLabel", Seq("1115676787"), false, UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).uniqueTaxReference(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for uniqueTaxReference when answered No" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              UniqueTaxReferenceId.toString ->
                No("No utr"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("uniqueTaxReference.checkYourAnswersLabel", Seq("No"), false, UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("uniqueTaxReference.reason.checkYourAnswersLabel", Seq("No utr"), false, UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).uniqueTaxReference(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for establisher Nino when answered Yes" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              EstablisherNinoId.toString ->
                EstablisherNino.Yes("test Nino"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq("Yes"), answerIsMessageKey = false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherNino.nino.checkYourAnswersLabel", Seq("test Nino"), answerIsMessageKey = false,
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).establisherNino(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for establisher Nino when answered No" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now),
              EstablisherNinoId.toString ->
                EstablisherNino.No("No nino"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("establisherNino.checkYourAnswersLabel", Seq("No"), false, EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherNino.reason.checkYourAnswersLabel", Seq("No nino"), false, EstablisherNinoController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).establisherNino(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for contact details" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              ContactDetailsId.toString ->
                ContactDetails("test@test.com", "0111111111"))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("test@test.com"), false, ContactDetailsController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("contactDetails.phoneNumber.checkYourAnswersLabel", Seq("0111111111"), false, ContactDetailsController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).contactDetails(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for establisher details" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              EstablisherDetailsId.toString ->
                EstablisherDetails("test first name", "test last name", LocalDate.now))
          ))
      )
      val expectedOutput = Seq(
        AnswerRow("establisherDetails.name.checkYourAnswersLabel", Seq("test first name test last name"), answerIsMessageKey = false,
          EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url),
        AnswerRow("establisherDetails.dateOfBirth.checkYourAnswersLabel", Seq(s"${DateHelper.formatDate(LocalDate.now)}"), answerIsMessageKey = false,
          EstablisherDetailsController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).establisherDetails(firstIndex) mustEqual expectedOutput
    }

    "return the AnswerRows for address years" in {
      val userAnswers = new UserAnswers(
        Json.obj(SchemeDetailsId.toString -> Json.toJson(
          SchemeDetails("value 1", SchemeType.SingleTrust)),
          "establishers" -> Json.arr(
            Json.obj(
              AddressYearsId.toString -> AddressYears.UnderAYear.toString)))
      )
      val expectedOutput = Seq(
        AnswerRow("addressYears.checkYourAnswersLabel", Seq(s"messages__common__${AddressYears.UnderAYear.toString}"), true,
          AddressYearsController.onPageLoad(CheckMode, firstIndex).url)
      )
      checkYourAnswerHelper(userAnswers).addressYears(firstIndex) mustEqual expectedOutput
    }
  }
}
