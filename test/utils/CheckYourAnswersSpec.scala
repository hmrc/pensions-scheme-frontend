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

package utils

import base.SpecBase
import identifiers.{EstablishedCountryId, SchemeNameId, TypedIdentifier}
import models._
import models.address.Address
import models.register.DeclarationDormant
import models.requests.DataRequest
import org.scalatest.{MustMatchers, OptionValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Message}

class CheckYourAnswersSpec extends SpecBase with MustMatchers with ScalaCheckPropertyChecks with OptionValues with Enumerable.Implicits {

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
          implicit val request: DataRequest[AnyContent] =
            DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(EstablishedCountryId.toString -> "AU",
              SchemeNameId.toString -> "Test Scheme Name")), Some(PsaId("A0000000")))
          implicit val userAnswers = request.userAnswers

          EstablishedCountryId.row(onwardUrl) must equal(Seq(
            AnswerRow(Message("schemeEstablishedCountry.checkYourAnswersLabel", "Test Scheme Name"), Seq("Australia"), false,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__schemeEstablishedCountry", "Test Scheme Name")))))))
        }

        "any id other than schemeEstablishedCountryId" in {
          implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> "value")), Some(PsaId("A0000000")))

          testIdentifier[String].row(onwardUrl) must equal(Seq(AnswerRow(Message("testId.checkYourAnswersLabel"), Seq("value"),
            false, Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__testId")))))))
        }
      }

      "boolean" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> true)), Some(PsaId("A0000000")))

        testIdentifier[Boolean].row(onwardUrl) must equal(Seq(AnswerRow(Message("testId.checkYourAnswersLabel"), Seq("site.yes"),
          true, Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__testId")))))))
      }

      "members" in {
        val membershipVal = Members.options.head.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> membershipVal)), Some(PsaId("A0000000")))

        testIdentifier[Members].row(onwardUrl) must equal(Seq(AnswerRow(
          Message("testId.checkYourAnswersLabel"), Seq(s"messages__members__$membershipVal"), true,
          Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__testId")))))))
      }

      "companyDetails" in {

        val companyDetails = CompanyDetails("Company Name")

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
          "testId" -> companyDetails
        )), Some(PsaId("A0000000")))

        testIdentifier[CompanyDetails].row("onwardUrl") must equal(Seq(AnswerRow(
          Message("messages__common__cya__name"),
          Seq(companyDetails.companyName),
          false,
          Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name", "Company Name"))))
        )))
      }

      "address" in {

        implicit val countryOptions = new CountryOptions(Seq.empty[InputOption])

        val address = Address(
          "address1", "address2", Some("address3"), Some("address4"), Some("postcode"), "GB"
        )

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> address)), Some(PsaId("A0000000")))

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
            Message("messages__common__cya__address"),
            addressAnswer(address),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__address"))))
          )))
      }

      "address years" in {

        val addressYears = AddressYears.values.head

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> addressYears)), Some(PsaId("A0000000")))

        testIdentifier[AddressYears].row(onwardUrl) must equal(Seq(AnswerRow(
          Message("messages__establisher_address_years__title"),
          Seq(s"messages__common__$addressYears"),
          true,
          Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__address_years"))))
        )))

      }

      "is dormant" when {
        "yes" in {
          val declarationDormat = DeclarationDormant.Yes.toString
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> declarationDormat)), Some(PsaId("A0000000")))

          testIdentifier[DeclarationDormant].row(onwardUrl) must equal(Seq(
            AnswerRow(
              Message("messages__company__cya__dormant"),
              Seq("site.yes"),
              true,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__establisher__dormant"))))
            )
          ))
        }
        "no" in {
          val declarationDormat = DeclarationDormant.No.toString
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> declarationDormat)), Some(PsaId("A0000000")))

          testIdentifier[DeclarationDormant].row(onwardUrl) must equal(Seq(
            AnswerRow(
              Message("messages__company__cya__dormant"),
              Seq("site.no"),
              true,
              Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__establisher__dormant"))))
            )
          ))
        }
      }

      "partnershipDetails" in {
        val partnershipDetails = PartnershipDetails("partnership name")
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> partnershipDetails)), Some(PsaId("A0000000")))

        testIdentifier[PartnershipDetails].row(onwardUrl) must equal(Seq(
          AnswerRow(
            Message("messages__common__cya__name"),
            Seq(s"${partnershipDetails.name}"),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name", partnershipDetails.name)))
            ))))
      }

      "reference" in {

        val reference = ReferenceValue("reference")

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
          "testId" -> reference
        )), Some(PsaId("A0000000")))

        testIdentifier[ReferenceValue].row("onwardUrl") must equal(Seq(AnswerRow(
          Message("messages__common__cya__name"),
          Seq(reference.value),
          false,
          Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name"))))
        )))
      }
    }

    "produce update row(row in UpdateMode) of answers" when {

      "members without change url" in {
        val membershipVal = Members.options.head.value
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> membershipVal)), Some(PsaId("A0000000")))

        testIdentifier[Members].row(onwardUrl, UpdateMode) must equal(Seq(AnswerRow(
          Message("testId.checkYourAnswersLabel"), Seq(s"messages__members__$membershipVal"), true, None)))
      }

      "partnershipDetails without change url" in {
        val partnershipDetails = PartnershipDetails("partnership name")
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> partnershipDetails)), Some(PsaId("A0000000")))

        testIdentifier[PartnershipDetails].row(onwardUrl, UpdateMode) must equal(Seq(
          AnswerRow(
            Message("messages__common__cya__name"),
            Seq(s"${partnershipDetails.name}"),
            false,
            None
          )))
      }

      "no dormant question" in {

        val dormant = DeclarationDormant.Yes.toString

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> dormant)), Some(PsaId("A0000000")))

        testIdentifier[DeclarationDormant].row(onwardUrl, UpdateMode) must equal(Nil)
      }

      "no address years" in {

        val addressYears = AddressYears.values.head

        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> addressYears)), Some(PsaId("A0000000")))

        testIdentifier[AddressYears].row(onwardUrl, UpdateMode) must equal(Nil)
      }

      "boolean without change url" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj("testId" -> true)), Some(PsaId("A0000000")))

        testIdentifier[Boolean].row(onwardUrl, UpdateMode) must equal(Seq(AnswerRow(
          Message("testId.checkYourAnswersLabel"), Seq("site.yes"), true, None)))
      }

      "reference" must {
        "not be editable " in {
          val reference = ReferenceValue("reference")

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> reference
          )), Some(PsaId("A0000000")))

          testIdentifier[ReferenceValue].row("onwardUrl", UpdateMode) must equal(Seq(AnswerRow(
            Message("messages__common__cya__name"),
            Seq(reference.value),
            false,
            None
          )))
        }

        "be editable" in {
          val reference = ReferenceValue("reference", true)

          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", UserAnswers(Json.obj(
            "testId" -> reference
          )), Some(PsaId("A0000000")))

          testIdentifier[ReferenceValue].row("onwardUrl", UpdateMode) must equal(Seq(AnswerRow(
            Message("messages__common__cya__name"),
            Seq(reference.value),
            false,
            Some(Link("site.change", onwardUrl, Some(Message("messages__visuallyhidden__common__name"))))
          )))
        }

        "not have any value" in {
          implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
            UserAnswers(Json.obj()), Some(PsaId("A0000000")))

          testIdentifier[ReferenceValue].row("onwardUrl", UpdateMode) must equal(Seq(AnswerRow(
            Message("messages__common__cya__name"),
            Seq("site.not_entered"),
            true,
            Some(Link("site.add", onwardUrl, Some(Message("messages__visuallyhidden__common__name"))))
          )))
        }
      }
    }
  }
}