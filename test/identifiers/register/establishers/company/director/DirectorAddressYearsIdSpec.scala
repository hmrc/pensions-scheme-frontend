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

package identifiers.register.establishers.company.director

import base.SpecBase
import identifiers.register.establishers.IsEstablisherCompleteId
import models.AddressYears.UnderAYear
import models.address.{Address, TolerantAddress}
import models.person.PersonDetails
import models.requests.DataRequest
import models.{AddressYears, Link, NormalMode, UpdateMode}
import org.joda.time.LocalDate
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.PsaId
import utils.checkyouranswers.Ops._
import utils.{Enumerable, FakeFeatureSwitchManagementService, UserAnswers}
import viewmodels.AnswerRow

class DirectorAddressYearsIdSpec extends SpecBase with Enumerable.Implicits {

  "Cleanup" must {



    val answers = UserAnswers(Json.obj())
      .set(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear)
      .flatMap(_.set(DirectorPreviousAddressPostcodeLookupId(0, 0))(Seq.empty))
      .flatMap(_.set(DirectorPreviousAddressId(0, 0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(DirectorPreviousAddressListId(0, 0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .flatMap(_.set(IsDirectorCompleteId(0, 0))(true))
      .flatMap(_.set(IsEstablisherCompleteId(0))(true))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" when {

      val result: UserAnswers = answers.set(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostcodeLookupId(0, 0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0, 0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0, 0)) mustNot be(defined)
      }

      "do not change the value of IsDirectorCompleteId and IsEstablisherCompleteId" in {
        result.get(IsDirectorCompleteId(0, 0)).value mustBe true
        result.get(IsEstablisherCompleteId(0)).value mustBe true
      }
    }

    "`AddressYears` is set to `UnderAYear`" when {

      val result: UserAnswers = UserAnswers(Json.obj())
        .set(DirectorAddressYearsId(0, 0))(AddressYears.OverAYear)
        .flatMap(_.set(DirectorPreviousAddressPostcodeLookupId(0, 0))(Seq.empty))
        .flatMap(_.set(DirectorPreviousAddressId(0, 0))(Address("foo", "bar", None, None, None, "GB")))
        .flatMap(_.set(DirectorPreviousAddressListId(0, 0))(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
        .flatMap(_.set(IsDirectorCompleteId(0, 0))(true))
        .flatMap(_.set(IsEstablisherCompleteId(0))(true)).asOpt.value.
        set(DirectorAddressYearsId(0, 0))(AddressYears.UnderAYear).asOpt.value

      "set the value of IsDirectorCompleteId and IsEstablisherCompleteId to false" in {
        result.get(IsDirectorCompleteId(0, 0)).value mustBe false
        result.get(IsEstablisherCompleteId(0)).value mustBe false
      }

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostcodeLookupId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0, 0)) mustBe defined
      }
    }

    "`AddressYears` is removed" when {

      val result: UserAnswers = answers.remove(DirectorAddressYearsId(0, 0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostcodeLookupId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0, 0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0, 0)) mustBe defined
      }
    }
  }

  "cya" when {

    val onwardUrl = "onwardUrl"

    val directorDetails = PersonDetails("John", None, "One", LocalDate.now())
    implicit val featureSwitchManagementService = new FakeFeatureSwitchManagementService(false)

    def answers = UserAnswers()
      .set(DirectorAddressYearsId(0, 0))(UnderAYear).asOpt.value
      .set(DirectorDetailsId(0, 0))(directorDetails).asOpt.value

    "in normal mode" must {

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__address_years", directorDetails.firstAndLastName),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl, Some("messages__visuallyhidden__director__address_years")))
          ))

        DirectorAddressYearsId(0, 0).row(onwardUrl, NormalMode) must equal(expectedResult)
      }
    }

    "in update mode for new trustee - company paye" must {

      def answersNew: UserAnswers = answers.set(IsNewDirectorId(0, 0))(true).asOpt.value

      "return answers rows with change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answersNew, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        val expectedResult = Seq(
          AnswerRow(
            messages("messages__director__cya__address_years", directorDetails.firstAndLastName),
            Seq(s"messages__common__under_a_year"),
            answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl,Some("messages__visuallyhidden__director__address_years")))
          ))

        DirectorAddressYearsId(0, 0).row(onwardUrl, UpdateMode) must equal(expectedResult)
      }
    }

    "in update mode for existing trustee - company paye" must {

      "return answers rows without change links" in {
        implicit val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id", answers, PsaId("A0000000"))
        implicit val userAnswers = request.userAnswers

        DirectorAddressYearsId(0, 0).row(onwardUrl, UpdateMode) must equal(Nil)
      }
    }
  }
}
