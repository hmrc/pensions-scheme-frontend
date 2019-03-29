/*
 * Copyright 2019 HM Revenue & Customs
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

import identifiers.{EstablishedCountryId, TypedIdentifier}
import models._
import models.address.Address
import models.person.PersonDetails
import models.register._
import models.requests.DataRequest
import org.joda.time.LocalDate
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

import scala.language.implicitConversions

class CheckYourAnswersSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues with Enumerable.Implicits {

  val onwardUrl = "onwardUrl"

  def testIdentifier[A]: TypedIdentifier[A] = new TypedIdentifier[A] {
    override def toString = "testId"
  }

  "CheckYourAnswers" must {

    "produce row of answers" when {

      "string" when {

        "id is EstablishedCountryId" in {
          implicit val countryOptions = new CountryOptions(Seq(InputOption("AU", "Australia"),
            InputOption("GB", "United Kingdom")))
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(EstablishedCountryId.toString -> "AU")), PsaId("A0000000"))
          implicit val userAnswers = request.userAnswers

          EstablishedCountryId.row(onwardUrl) must equal(Seq(
            AnswerRow("schemeEstablishedCountry.checkYourAnswersLabel", Seq("Australia"), false, Some(onwardUrl), "messages__visuallyhidden__schemeEstablishedCountry")))
        }

        "any id other than schemeEstablishedCountryId" in {
          implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> "value")), PsaId("A0000000"))

          testIdentifier[String].row(onwardUrl) must equal(Seq(AnswerRow("testId.checkYourAnswersLabel", Seq("value"), false, Some(onwardUrl), "messages__visuallyhidden__testId")))
        }
      }

      "boolean" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> true)), PsaId("A0000000"))

        testIdentifier[Boolean].row(onwardUrl) must equal(Seq(AnswerRow("testId.checkYourAnswersLabel", Seq("site.yes"), true, Some(onwardUrl), "messages__visuallyhidden__testId")))
      }

      "members" in {
        val membershipVal = Members.options.head.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> membershipVal)), PsaId("A0000000"))

        testIdentifier[Members].row(onwardUrl) must equal(Seq(AnswerRow(
          "testId.checkYourAnswersLabel", Seq(s"messages__members__$membershipVal"), true, Some(onwardUrl), "messages__visuallyhidden__testId")))
      }

      "companyDetails" when {

        "only name exists" in {

          val companyDetails = CompanyDetails("Company Name", None, None)

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )), PsaId("A0000000"))

          testIdentifier[CompanyDetails].row("onwardUrl") must equal(Seq(AnswerRow(
            "messages__common__cya__name",
            Seq(companyDetails.companyName),
            false,
            Some(onwardUrl),
            Message("messages__visuallyhidden__common__name", "Company Name")
          )))

        }
        "vat number exists" in {

          val companyDetails = CompanyDetails("Company Name", Some("VAT123"), None)

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )), PsaId("A0000000"))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              Some(onwardUrl),
              Message("messages__visuallyhidden__common__name", "Company Name")
            ),
            AnswerRow(
              "messages__common__cya__vat",
              Seq(s"${companyDetails.vatNumber.get}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__vat_number"
            )))

        }
        "paye ref exists" in {

          val companyDetails = CompanyDetails("Company Name", None, Some("PAYE/123"))

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )), PsaId("A0000000"))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              Some(onwardUrl),
              Message("messages__visuallyhidden__common__name", "Company Name")
            ),
            AnswerRow(
              "messages__common__cya__paye",
              Seq(s"${companyDetails.payeNumber.get}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__paye_number"
            )))

        }

        "all values exist" in {

          val companyDetails = CompanyDetails("Company Name", Some("VAT123"), Some("PAYE/123"))

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> companyDetails
          )), PsaId("A0000000"))

          testIdentifier[CompanyDetails].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__common__cya__name",
              Seq(s"${companyDetails.companyName}"),
              false,
              Some(onwardUrl),
              Message("messages__visuallyhidden__common__name", "Company Name")
            ),
            AnswerRow(
              "messages__common__cya__vat",
              Seq(s"${companyDetails.vatNumber.get}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__vat_number"
            ),
            AnswerRow(
              "messages__common__cya__paye",
              Seq(s"${companyDetails.payeNumber.get}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__paye_number"
            )))

        }
      }

      "CRN" when {

        "yes" in {

          val crn = CompanyRegistrationNumber.Yes("0987654")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> crn)), PsaId("A0000000"))

          testIdentifier[CompanyRegistrationNumber].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__company__cya__crn_yes_no",
              Seq(s"${CompanyRegistrationNumber.Yes}"),
              true,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__crn_yes_no"
            ),
            AnswerRow(
              "messages__common__crn",
              Seq(s"${crn.crn}"),
              true,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__crn"
            )))
        }
        "no" in {

          val crn = CompanyRegistrationNumber.No("Not sure")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> crn)), PsaId("A0000000"))

          testIdentifier[CompanyRegistrationNumber].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__company__cya__crn_yes_no",
              Seq(s"${CompanyRegistrationNumber.No}"),
              true,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__crn_yes_no"
            ),
            AnswerRow(
              "messages__company__cya__crn_no_reason",
              Seq(s"${crn.reason}"),
              true,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__crn_no"
            )
          ))

        }
      }

      "UTR" when {

        "yes" in {

          val utr = UniqueTaxReference.Yes("7654321244")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> utr)), PsaId("A0000000"))

          testIdentifier[UniqueTaxReference].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__establisher_individual_utr_question_cya_label",
              Seq(s"${UniqueTaxReference.Yes}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__utr_yes_no"
            ),
            AnswerRow(
              "messages__establisher_individual_utr_cya_label",
              Seq({
                utr.utr
              }),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__utr"
            )
          ))

        }

        "no" in {

          val utr = UniqueTaxReference.No("Not sure")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> utr)), PsaId("A0000000"))

          testIdentifier[UniqueTaxReference].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__establisher_individual_utr_question_cya_label",
              Seq(s"${UniqueTaxReference.No}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__utr_yes_no"
            ),
            AnswerRow(
              "messages__establisher_individual_utr_reason_cya_label",
              Seq(utr.reason),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__establisher__utr_no"
            )))

        }

      }

      "address" in {

        implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])

        val address = Address(
          "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
        )

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> address)), PsaId("A0000000"))

        def addressAnswer(address: Address): Seq[String] = {
          val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)

          Seq(
            Some(address.addressLine1),
            Some(address.addressLine2),
            address.addressLine3,
            address.addressLine4,
            address.postcode,
            Some(country)
          ).flatten
        }

        testIdentifier[Address].row(onwardUrl) must equal(Seq(
          AnswerRow(
            "messages__common__cya__address",
            addressAnswer(address),
            false,
            Some(onwardUrl),
            "messages__visuallyhidden__common__address"
          )))

      }

      "address years" in {

        val addressYears = AddressYears.values.head

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> addressYears)), PsaId("A0000000"))

        testIdentifier[AddressYears].row(onwardUrl) must equal(Seq(AnswerRow(
          "messages__establisher_address_years__title",
          Seq(s"messages__common__$addressYears"),
          true,
          Some(onwardUrl),
          "messages__visuallyhidden__common__address_years"
        )))

      }

      "contactDetails" in {

        val contactDetails = ContactDetails("e@mail.com", "0987654")

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> contactDetails)), PsaId("A0000000"))

        testIdentifier[ContactDetails].row(onwardUrl) must equal(Seq(
          AnswerRow(
            "messages__common__email",
            Seq(s"${contactDetails.emailAddress}"),
            false,
            Some(onwardUrl),
            "messages__visuallyhidden__common__email_address"
          ),
          AnswerRow(
            "messages__common__phone",
            Seq(s"${contactDetails.phoneNumber}"),
            false,
            Some(onwardUrl),
            "messages__visuallyhidden__common__phone_number"
          )))

      }

      "personDetails" in {
        val personDetails = PersonDetails("firstName", None, "last", LocalDate.now)
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> personDetails)), PsaId("A0000000"))

        testIdentifier[PersonDetails].row(onwardUrl) must equal(Seq(
          AnswerRow(
            "messages__common__cya__name",
            Seq(s"${personDetails.fullName}"),
            false,
            Some(onwardUrl),
            Message("messages__visuallyhidden__common__name", personDetails.fullName)
          ),
          AnswerRow(
            "messages__common__dob",
            Seq(s"${DateHelper.formatDate(personDetails.date)}"),
            false,
            Some(onwardUrl),
            Message("messages__visuallyhidden__common__dob", personDetails.fullName)
          )))
      }

      "Nino" when {
        "yes" in {
          val nino = Nino.Yes("AB700100A")
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> nino)), PsaId("A0000000"))

          testIdentifier[Nino].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__trusteeNino_question_cya_label",
              Seq(s"${Nino.Yes}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__trustee__nino_yes_no"
            ),
            AnswerRow(
              "messages__trusteeNino_nino_cya_label",
              Seq(nino.nino),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__trustee__nino"
            )))
        }

        "no" in {
          val nino = Nino.No("Not sure")
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> nino)), PsaId("A0000000"))

          testIdentifier[Nino].row(onwardUrl) must equal(Seq(
            AnswerRow(
              "messages__trusteeNino_question_cya_label",
              Seq(s"${Nino.No}"),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__trustee__nino_yes_no"
            ),
            AnswerRow(
              "messages__trusteeNino_reason_cya_label",
              Seq(nino.reason),
              false,
              Some(onwardUrl),
              "messages__visuallyhidden__trustee__nino_no"
            )))
        }
      }
    }

    "produce update row(row in UpdateMode) of answers" when {

      "members without change url" in {
        val membershipVal = Members.options.head.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> membershipVal)), PsaId("A0000000"))

        testIdentifier[Members].row(onwardUrl, UpdateMode) must equal(Seq(AnswerRow(
          "testId.checkYourAnswersLabel", Seq(s"messages__members__$membershipVal"), true, None, "messages__visuallyhidden__testId")))
      }
    }
  }
}
