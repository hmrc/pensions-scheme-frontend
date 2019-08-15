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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.trustees.individual.{TrusteeEmailId, TrusteeNameId, TrusteePhoneId}
import models.Mode.checkMode
import models._
import models.person.PersonName
import models.requests.DataRequest
import play.api.mvc.{AnyContent, Call}
import play.api.test.Helpers._
import utils.checkyouranswers.CheckYourAnswers.StringCYA
import utils.{AllowChangeHelper, CountryOptions, FakeCountryOptions, FakeDataRequest, UserAnswers}
import viewmodels.AnswerSection
import views.html.check_your_answers

class CheckYourAnswersIndividualContactDetailsControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  private val index = Index(0)
  private val srn = Some("test-srn")
  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions

  private def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call =
    routes.CheckYourAnswersIndividualContactDetailsController.onSubmit(mode, index, srn)

  private def answerSection(mode: Mode, srn: Option[String] = None)(implicit request: DataRequest[AnyContent]): Seq[AnswerSection] = {
    val userAnswers = request.userAnswers
    val trusteeName = userAnswers.get(TrusteeNameId(index)).map(_.fullName).value

    Seq(AnswerSection(None,
      StringCYA[TrusteeEmailId](
        Some(messages("messages__common_email__heading", trusteeName)),
        Some(messages("messages__common_email__visually_hidden_change_label", trusteeName))
      )().row(TrusteeEmailId(index))(
        routes.TrusteeEmailController.onPageLoad(checkMode(mode), Index(index), srn).url, userAnswers) ++

        StringCYA[TrusteePhoneId](
         Some(messages("messages__common_phone__heading", trusteeName)),
          Some(messages("messages__common_phone__visually_hidden_change_label", trusteeName))
        )().row(TrusteePhoneId(index))(
          routes.TrusteePhoneController.onPageLoad(checkMode(mode), Index(index), srn).url, userAnswers)
    ))
  }

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersIndividualContactDetailsController =
    new CheckYourAnswersIndividualContactDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl()): String =
    check_your_answers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false
    )(fakeRequest, messages).toString

  private val fullAnswers = UserAnswers().set(TrusteeEmailId(0))("test@test.com").flatMap(_.set(TrusteePhoneId(0))("12345"))
    .flatMap(_.set(TrusteeNameId(0))(PersonName("test", "name"))).asOpt.value

  "CheckYourAnswersIndividualContactDetailsController" when {

    "on a GET" must {
      "return OK and the correct view with full answers" when {
        "Normal Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe viewAsString(answerSection(NormalMode))
        }

        "Update Mode" in {
          implicit val request: DataRequest[AnyContent] = FakeDataRequest(fullAnswers)
          val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), postUrl = submitUrl(UpdateMode, srn), srn = srn)
        }
      }
    }

    "on a POST" must {
      "redirect to task list page" in {
        val result = controller().onSubmit(NormalMode, Index(1), None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url)
      }
    }
  }
}

