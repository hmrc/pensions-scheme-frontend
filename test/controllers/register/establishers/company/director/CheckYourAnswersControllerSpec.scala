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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.establishers.company.director._
import models._
import models.address.Address
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.test.Helpers._
import utils.checkyouranswers.Ops._
import utils.{FakeCountryOptions, FakeDataRequest, FakeNavigator, FakeSectionComplete, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(directorAnswers)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      new FakeNavigator(desiredRoute),
      countryOptions
    )

  "CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {
        val directorDetails = AnswerSection(
          Some("messages__director__cya__details_heading"),
          Seq(
            DirectorDetailsId(firstIndex, firstIndex).
              row(routes.DirectorDetailsController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url),
            DirectorNinoId(firstIndex, firstIndex).
              row(routes.DirectorNinoController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url),
            DirectorUniqueTaxReferenceId(firstIndex, firstIndex).
              row(routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url)
          ).flatten
        )

        val directorContactDetails = AnswerSection(
          Some("messages__director__cya__contact__details_heading"),
          Seq(
            DirectorAddressId(firstIndex, firstIndex).
              row(routes.DirectorAddressController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url),
            DirectorAddressYearsId(firstIndex, firstIndex).
              row(routes.DirectorAddressYearsController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url),
            DirectorPreviousAddressId(firstIndex, firstIndex).
              row(routes.DirectorPreviousAddressController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url),
            DirectorContactDetailsId(firstIndex, firstIndex).
              row(routes.DirectorContactDetailsController.onPageLoad(CheckMode, firstIndex, firstIndex, None).url)
          ).flatten
        )

        val viewAsString = check_your_answers(
          frontendAppConfig,
          Seq(directorDetails, directorContactDetails),
          routes.CheckYourAnswersController.onSubmit(firstIndex, firstIndex, NormalMode, None),
          None,
          viewOnly = false
        )(fakeRequest, messages).toString

        val result = controller(directorAnswers.dataRetrievalAction).onPageLoad(firstIndex, firstIndex, NormalMode, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(firstIndex, firstIndex, NormalMode, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeSectionComplete.verify(IsDirectorCompleteId(firstIndex, firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends OptionValues {
  val firstIndex = Index(0)
  val schemeName = "test scheme name"
  val desiredRoute = controllers.routes.IndexController.onPageLoad()

  val directorAnswers = UserAnswers()
    .set(DirectorDetailsId(firstIndex, firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .flatMap(_.set(DirectorNinoId(firstIndex, firstIndex))(Nino.Yes("AB100100A")))
    .flatMap(_.set(DirectorUniqueTaxReferenceId(firstIndex, firstIndex))(UniqueTaxReference.Yes("1234567890")))
    .flatMap(_.set(DirectorAddressId(firstIndex, firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorAddressYearsId(firstIndex, firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(DirectorPreviousAddressId(firstIndex, firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(DirectorContactDetailsId(firstIndex, firstIndex))(ContactDetails("test@test.com", "123456789")))
    .asOpt.value
}
