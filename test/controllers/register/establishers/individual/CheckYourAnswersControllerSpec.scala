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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import identifiers.register.establishers.individual._
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId, individual}
import models._
import models.Mode._
import models.address.Address
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import utils._
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase {
 import CheckYourAnswersControllerSpec._

  implicit val countryOptions = new FakeCountryOptions()
  implicit val request = FakeDataRequest(individualAnswers)
  val firstIndex = Index(0)

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherHns): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeSectionComplete,
      countryOptions,
      new FakeNavigator(onwardRoute)
    )

  "CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {
        val individualDetails = AnswerSection(
          None,
          EstablisherDetailsId(firstIndex).row(
            controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(CheckMode, firstIndex, None).url) ++
            EstablisherNinoId(firstIndex).row(
              controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex, None).url) ++
              UniqueTaxReferenceId(firstIndex).row(
                routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex, None).url) ++
              AddressId(firstIndex).row(
                controllers.register.establishers.individual.routes.AddressController.onPageLoad(CheckMode, firstIndex, None).url) ++
              AddressYearsId(firstIndex).row(
                controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(CheckMode, firstIndex, None).url) ++
              PreviousAddressId(firstIndex).row(
                controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(CheckMode, firstIndex, None).url
              ) ++
              ContactDetailsId(firstIndex).row(
                controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(CheckMode, firstIndex, None).url
              )
          )

        val viewAsString = check_your_answers(
          frontendAppConfig,
          Seq(individualDetails),
          routes.CheckYourAnswersController.onSubmit(NormalMode, firstIndex, None),
          None,
          viewOnly = false
        )(fakeRequest, messages).toString

        val result = controller(individualAnswers.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }

      "return OK and display all the answers with change link when new" in {
        val individualDetails = AnswerSection(
          None,
          EstablisherDetailsId(firstIndex).row(
            controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url) ++
            EstablisherNinoId(firstIndex).row(
              controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url) ++
            UniqueTaxReferenceId(firstIndex).row(
              routes.UniqueTaxReferenceController.onPageLoad(CheckMode, firstIndex, None).url) ++
            AddressId(firstIndex).row(
              controllers.register.establishers.individual.routes.AddressController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url) ++
            AddressYearsId(firstIndex).row(
              controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url) ++
            PreviousAddressId(firstIndex).row(
              controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url
            ) ++
            ContactDetailsId(firstIndex).row(
              controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(checkMode(UpdateMode), firstIndex, None).url
            )
        )

        val individualAnswersNew = individualAnswers.set(IsEstablisherNewId(firstIndex))(true).asOpt.value
        val viewAsString = check_your_answers(
          frontendAppConfig,
          Seq(individualDetails),
          routes.CheckYourAnswersController.onSubmit(UpdateMode, firstIndex, None),
          None,
          viewOnly = false
        )(fakeRequest, messages).toString

        val result = controller(individualAnswersNew.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, None)(request)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(NormalMode, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeSectionComplete.verify(IsEstablisherCompleteId(firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends OptionValues {
  val firstIndex = Index(0)
  val desiredRoute = controllers.routes.IndexController.onPageLoad()
  val individualAnswers = UserAnswers()
    .set(EstablisherDetailsId(firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .flatMap(_.set(EstablisherNinoId(firstIndex))(Nino.Yes("AB100100A")))
    .flatMap(_.set(UniqueTaxReferenceId(firstIndex))(UniqueTaxReference.Yes("1234567890")))
    .flatMap(_.set(AddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(AddressYearsId(firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(individual.PreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(individual.ContactDetailsId(firstIndex))(ContactDetails("test@test.com", "123456789"))).asOpt.value
}
