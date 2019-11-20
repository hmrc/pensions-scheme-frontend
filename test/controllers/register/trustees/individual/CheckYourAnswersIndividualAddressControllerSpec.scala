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
import models.Mode.checkMode
import models._
import models.address.Address
import models.person.PersonName
import navigators.Navigator
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.Helpers._
import utils._
import utils.annotations.NoSuspendedCheck
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class CheckYourAnswersIndividualAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersIndividualAddressControllerSpec._

  "Check Your Answers Individual Address Controller " when {
    "on Page load" must {
      "return OK and the correct view with full answers" when {
        "Normal MOde" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualAddressController]
          val result = controller.onPageLoad(NormalMode, index, None)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection())
          app.stop()
        }

        "Update Mode" in {

          running(_.overrides(
            bind[Navigator].toInstance(FakeNavigator),
            bind[AuthAction].toInstance(FakeAuthAction),
            bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
            bind[DataRetrievalAction].to(fullAnswers.dataRetrievalAction),
            bind[AllowChangeHelper].toInstance(allowChangeHelper(saveAndContinueButton = true)),
            bind[AllowAccessActionProvider].qualifiedWith(classOf[NoSuspendedCheck]).to(FakeAllowAccessProvider())
          )) {
            app =>

              val controller = app.injector.instanceOf[CheckYourAnswersIndividualAddressController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

              status(result) mustBe OK
              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn, submitUrl(UpdateMode, srn), hideButton = true)
              app.stop()
          }
        }
      }
    }
  }

}

object CheckYourAnswersIndividualAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index = Index(0)
  val srn = Some("test-srn")
  val trusteeName = "First Last"

  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private def trusteeAddressRoute(mode: Mode, srn: Option[String]): String =
    routes.TrusteeAddressController.onPageLoad(mode, index, srn).url

  private def trusteeAddressYearsRoute(mode: Mode, srn: Option[String]): String =
    routes.TrusteeAddressYearsController.onPageLoad(mode, index, srn).url

  private def trusteePreviousAddressRoute(mode: Mode, srn: Option[String]): String =
    routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn).url

  private val fullAnswers = UserAnswers().
    trusteeName(index, PersonName("First", "Last")).
    trusteesAddress(index, address).
    trusteesIndividualAddressYears(index, addressYearsUnderAYear).
    trusteesPreviousAddress(index, previousAddress)

  def submitUrl(mode: Mode = NormalMode, srn: Option[String] = None): Call = controllers.routes.SchemeTaskListController.onPageLoad(mode, srn)

  def addressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__trusteeAddress", trusteeName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", trusteeAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_address", trusteeName)))
    ))

  def addressYearsAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__trusteeAddressYears__heading", trusteeName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", trusteeAddressYearsRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_addressYears", trusteeName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__trusteePreviousAddress", trusteeName),
    UserAnswers().addressAnswer(previousAddress),
    answerIsMessageKey = false,
    Some(Link("site.change", trusteePreviousAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_previousAddress", trusteeName))))
  )

  def answerSection(mode: Mode = NormalMode, srn: Option[String] = None): Seq[AnswerSection] = Seq(AnswerSection(None,
    if (mode == NormalMode) Seq(addressAnswerRow(mode, srn), addressYearsAnswerRow(mode, srn), previousAddressAnswerRow(mode, srn))
    else Seq(addressAnswerRow(mode, srn), previousAddressAnswerRow(mode, srn))))

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = submitUrl(), hideButton: Boolean = false): String =
    checkYourAnswers(frontendAppConfig, CYAViewModel(
      answerSections = answerSections,
      href = postUrl,
      schemeName = None,
      returnOverview = false,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = hideButton,
      title = Message("checkYourAnswers.hs.title"),
      h1 = Message("checkYourAnswers.hs.heading")
    )
    )(fakeRequest, messages).toString

}
