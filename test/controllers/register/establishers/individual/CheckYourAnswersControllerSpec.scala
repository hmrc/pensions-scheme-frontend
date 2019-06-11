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
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.trustees.individual.CheckYourAnswersControllerSpec.{fakeRequest, frontendAppConfig, messages, postUrl}
import identifiers.TypedIdentifier
import identifiers.register.establishers.individual._
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId, individual}
import models._
import models.address.Address
import models.person.PersonDetails
import models.requests.DataRequest
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.libs.json.JsResult
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.Ops._
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions:FakeCountryOptions = new FakeCountryOptions()
  implicit val request:FakeDataRequest = FakeDataRequest(individualAnswers)
  implicit val userAnswers:UserAnswers = request.userAnswers
  val firstIndex = Index(0)

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherHns,
                         allowChangeHelper:AllowChangeHelper = ach, toggle:Boolean = false): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      FakeUserAnswersService,
      countryOptions,
      new FakeNavigator(onwardRoute),
      allowChangeHelper,
      new FakeFeatureSwitchManagementService(toggle)
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

        val result = controller(individualAnswers.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, None)(request)
        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(Seq(individualDetails), NormalMode, None)
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }

      behave like changeableController(
        controller(individualAnswers.dataRetrievalAction, _:AllowChangeHelper)
        .onPageLoad(UpdateMode, firstIndex, None)(request))
    }

    "onSubmit" must {
      "mark the section as complete and redirect to the next page" in {
        val result = controller().onSubmit(NormalMode, firstIndex, None)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(desiredRoute.url)

        FakeUserAnswersService.verify(IsEstablisherCompleteId(firstIndex), true)
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends OptionValues {
  def postUrl(mode:Mode, srn:Option[String]): Call = routes.CheckYourAnswersController.onSubmit(mode, firstIndex, srn)

  def viewAsString(answerSections:Seq[AnswerSection], mode: Mode, srn:Option[String]): String = check_your_answers(
    frontendAppConfig,
    answerSections,
    postUrl(mode, srn),
    None,
    hideEditLinks = false,
    hideSaveAndContinueButton = false,
    srn = srn,
    mode = mode
  )(fakeRequest, messages).toString
  private val firstIndex = Index(0)
  private val desiredRoute:Call = controllers.routes.IndexController.onPageLoad()

  private val commonJsResultAnswers: JsResult[UserAnswers] = UserAnswers()
    .set(EstablisherDetailsId(firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .flatMap(_.set(EstablisherNinoId(firstIndex))(Nino.Yes("AB100100A")))
    .flatMap(_.set(UniqueTaxReferenceId(firstIndex))(UniqueTaxReference.Yes("1234567890")))
    .flatMap(_.set(AddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(AddressYearsId(firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(individual.PreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(individual.ContactDetailsId(firstIndex))(ContactDetails("test@test.com", "123456789")))

  private val individualAnswers:UserAnswers = commonJsResultAnswers.asOpt.value
  private val individualAnswersWithNewlyAddedEstablisher:UserAnswers = commonJsResultAnswers
    .flatMap(_.set(IsEstablisherNewId(firstIndex))(value = true))
    .asOpt.value
}
