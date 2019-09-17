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
import controllers.register.establishers.individual.CheckYourAnswersControllerSpec.firstIndex
import controllers.register.trustees.individual.CheckYourAnswersControllerSpec.{fakeRequest, frontendAppConfig, messages}
import identifiers.register.establishers.individual._
import identifiers.register.establishers.{IsEstablisherCompleteId, IsEstablisherNewId, individual}
import models._
import models.address.Address
import models.person.PersonDetails
import org.joda.time.LocalDate
import org.scalatest.OptionValues
import play.api.libs.json.JsResult
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import services.FakeUserAnswersService
import utils._
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, AnswerSection}
import views.html.{checkYourAnswers, check_your_answers_old}

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersControllerSpec._

  implicit val countryOptions: FakeCountryOptions = new FakeCountryOptions()
  implicit val request: FakeDataRequest = FakeDataRequest(individualAnswers)
  implicit val userAnswers: UserAnswers = request.userAnswers
  val firstIndex = Index(0)

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher,
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersController =
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
      allowChangeHelper
    )

  "CheckYourAnswersController" when {
    "onPageLoad" must {
      "return OK and display all the answers" in {
        val result = controller(individualAnswers.dataRetrievalAction).onPageLoad(NormalMode, firstIndex, None)(request)
        status(result) mustBe OK

        val ninoRow = EstablisherNinoId(firstIndex).row(
          controllers.register.establishers.individual.routes.EstablisherNinoController.onPageLoad(CheckMode, firstIndex, None).url)

        contentAsString(result) mustBe viewAsString(Seq(individualDetails(ninoRow, CheckMode, None)), NormalMode, None)
        assertRenderedById(asDocument(contentAsString(result)), "submit")
      }

      behave like changeableController(
        controller(individualAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(UpdateMode, firstIndex, None)(request))

      "return OK and display Add link for UpdateMode pointing to new Nino page where no nino retrieved from ETMP" in {

        val expectedNinoRow = {
          implicit val request: FakeDataRequest = FakeDataRequest(individualAnswersWithNoNino)
          EstablisherNewNinoId(firstIndex).row(
            controllers.register.establishers.individual.routes.EstablisherNinoNewController.onPageLoad(CheckUpdateMode, firstIndex, Some("srn")).url, UpdateMode)
        }

        val result = controller(individualAnswersWithNoNino.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, Some("srn"))(fakeRequest)
        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(Seq(individualDetails(expectedNinoRow, CheckUpdateMode, Some("srn"))), UpdateMode, Some("srn"))
      }

      "return OK and display no add/change link but do display new nino for UpdateMode where a new nino has already been entered" in {
        val expectedNinoRow = {
          implicit val request: FakeDataRequest = FakeDataRequest(individualAnswersWithNewNino)
          EstablisherNewNinoId(firstIndex).row(
            controllers.register.establishers.individual.routes.EstablisherNinoNewController.onPageLoad(CheckUpdateMode, firstIndex, Some("srn")).url, UpdateMode)
        }

        val result = controller(individualAnswersWithNewNino.dataRetrievalAction).onPageLoad(UpdateMode, firstIndex, Some("srn"))(fakeRequest)
        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(Seq(individualDetails(expectedNinoRow, CheckUpdateMode, Some("srn"))), UpdateMode, Some("srn"))
      }
    }

    def individualDetails(ninoRow: Seq[AnswerRow], mode: Mode, srn: Option[String]) = AnswerSection(
      None,
      EstablisherDetailsId(firstIndex).row(
        controllers.register.establishers.individual.routes.EstablisherDetailsController.onPageLoad(mode, firstIndex, srn).url, mode) ++
        ninoRow ++
        UniqueTaxReferenceId(firstIndex).row(
          routes.UniqueTaxReferenceController.onPageLoad(mode, firstIndex, srn).url, mode) ++
        AddressId(firstIndex).row(
          controllers.register.establishers.individual.routes.AddressController.onPageLoad(mode, firstIndex, srn).url, mode) ++
        AddressYearsId(firstIndex).row(
          controllers.register.establishers.individual.routes.AddressYearsController.onPageLoad(mode, firstIndex, srn).url, mode) ++
        PreviousAddressId(firstIndex).row(
          controllers.register.establishers.individual.routes.PreviousAddressController.onPageLoad(mode, firstIndex, srn).url, mode
        ) ++
        ContactDetailsId(firstIndex).row(
          controllers.register.establishers.individual.routes.ContactDetailsController.onPageLoad(mode, firstIndex, srn).url, mode
        )
    )

  }
}

object CheckYourAnswersControllerSpec extends OptionValues {
  def postUrl(mode: Mode, srn: Option[String]): Call = controllers.register.establishers.routes.AddEstablisherController.onPageLoad(mode, srn)

  def viewAsString(answerSections: Seq[AnswerSection], mode: Mode, srn: Option[String]): String = checkYourAnswers(
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

  private def commonJsResultAnswers(f: UserAnswers => JsResult[UserAnswers]): JsResult[UserAnswers] = UserAnswers()
    .set(EstablisherDetailsId(firstIndex))(PersonDetails("first name", None, "last name", LocalDate.now(), false))
    .flatMap(dd => f(dd))
    .flatMap(_.set(UniqueTaxReferenceId(firstIndex))(UniqueTaxReference.Yes("1234567890")))
    .flatMap(_.set(AddressId(firstIndex))(Address("Address 1", "Address 2", None, None, None, "GB")))
    .flatMap(_.set(AddressYearsId(firstIndex))(AddressYears.UnderAYear))
    .flatMap(_.set(individual.PreviousAddressId(firstIndex))(Address("Previous Address 1", "Previous Address 2", None, None, None, "GB")))
    .flatMap(_.set(individual.ContactDetailsId(firstIndex))(ContactDetails("test@test.com", "123456789")))

  private val individualAnswers: UserAnswers = commonJsResultAnswers(_.set(EstablisherNinoId(firstIndex))(Nino.Yes("AB100100A"))).asOpt.value
  private val individualAnswersWithNoNino: UserAnswers = commonJsResultAnswers(
    _.set(EstablisherNinoId(firstIndex))(Nino.No("reason"))
  ).asOpt.value

  private val individualAnswersWithNewNino: UserAnswers = commonJsResultAnswers(
    _.set(EstablisherNinoId(firstIndex))(Nino.Yes("CS121212C"))
  ).asOpt.value

}
