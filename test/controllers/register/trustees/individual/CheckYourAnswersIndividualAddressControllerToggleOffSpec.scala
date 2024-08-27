/*
 * Copyright 2024 HM Revenue & Customs
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
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.address.Address
import models.person.PersonName
import navigators.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils._
import utils.annotations.NoSuspendedCheck
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersIndividualAddressControllerToggleOffSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach{

  import CheckYourAnswersIndividualAddressControllerToggleOffSpec._

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, false)))
  }

  "Check Your Answers Individual Address Controller " when {
    "on Page load" must {
      "return OK and the correct view with full answers" when {
        "Normal MOde" in {
          val app = applicationBuilder(fullAnswers.dataRetrievalAction).overrides(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService)).build()

          val controller = app.injector.instanceOf[CheckYourAnswersIndividualAddressController]
          val result = controller.onPageLoad(NormalMode, index, srn)(fakeRequest)

          status(result) mustBe OK
          contentAsString(result) mustBe viewAsString(answerSection(NormalMode, srn),
            title = Message("checkYourAnswers.hs.heading"),
            h1 = Message("checkYourAnswers.hs.heading"),
            srn = srn)
          app.stop()
        }

        "Update Mode" in {

          val ftBinding: Seq[GuiceableModule] = Seq(
            bind[FeatureToggleService].toInstance(mockFeatureToggleService),
            bind[Navigator].toInstance(FakeNavigator),
            bind[AuthAction].toInstance(FakeAuthAction),
            bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider(srn)),
            bind[DataRetrievalAction].to(fullAnswers.dataRetrievalAction),
            bind[AllowChangeHelper].toInstance(allowChangeHelper(saveAndContinueButton = true)),
            bind[AllowAccessActionProvider].qualifiedWith(classOf[NoSuspendedCheck]).to(FakeAllowAccessProvider(srn))
          )
          running(_.overrides(ftBinding: _*)) {
            app =>

              val controller = app.injector.instanceOf[CheckYourAnswersIndividualAddressController]
              val result = controller.onPageLoad(UpdateMode, index, srn)(fakeRequest)

              status(result) mustBe OK
              contentAsString(result) mustBe viewAsString(answerSection(UpdateMode, srn), srn,
                submitUrl(UpdateMode, srn), hideButton = true,
                title = Message("messages__addressFor", Message("messages__thePerson")),
                h1 = Message("messages__addressFor", trusteeName))
              app.stop()
          }
        }
      }
    }
  }
}

object CheckYourAnswersIndividualAddressControllerToggleOffSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, srn)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index = Index(0)
  val trusteeName = "First Last"

  private val mockFeatureToggleService = mock[FeatureToggleService]
  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private def trusteeAddressRoute(mode: Mode, srn: SchemeReferenceNumber): String =
    routes.TrusteeAddressController.onPageLoad(mode, index, srn).url

  private def trusteeAddressYearsRoute(mode: Mode, srn: SchemeReferenceNumber): String =
    routes.TrusteeAddressYearsController.onPageLoad(mode, index, srn).url

  private def trusteePreviousAddressRoute(mode: Mode, srn: SchemeReferenceNumber): String =
    routes.TrusteePreviousAddressController.onPageLoad(mode, index, srn).url

  private val fullAnswers = UserAnswers().
    trusteeName(index, PersonName("First", "Last")).
    trusteesAddress(index, address).
    trusteesIndividualAddressYears(index, addressYearsUnderAYear).
    trusteesPreviousAddress(index, previousAddress)

  def submitUrl(mode: Mode = NormalMode, srn: SchemeReferenceNumber): Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(mode, srn)

  def addressAnswerRow(mode: Mode, srn: SchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteeAddress", trusteeName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", trusteeAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_address", trusteeName)))
    ))

  def addressYearsAnswerRow(mode: Mode, srn: SchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteeAddressYears__heading", trusteeName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", trusteeAddressYearsRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_addressYears", trusteeName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: SchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteePreviousAddress", trusteeName),
    UserAnswers().addressAnswer(previousAddress),
    answerIsMessageKey = false,
    Some(Link("site.change", trusteePreviousAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_previousAddress", trusteeName))))
  )

  def answerSection(mode: Mode = NormalMode, srn: SchemeReferenceNumber): Seq[AnswerSection] = Seq(AnswerSection(None,
    if (mode == NormalMode) Seq(addressAnswerRow(mode, srn), addressYearsAnswerRow(mode, srn), previousAddressAnswerRow(mode, srn))
    else Seq(addressAnswerRow(mode, srn), previousAddressAnswerRow(mode, srn))))

  private val view = injector.instanceOf[checkYourAnswers]

  def viewAsString(answerSections: Seq[AnswerSection], srn: SchemeReferenceNumber,
                   postUrl: Call = submitUrl(NormalMode, srn), hideButton: Boolean = false,
                   title:Message, h1:Message): String =
    view(CYAViewModel(
      answerSections = answerSections,
      href = postUrl,
      schemeName = None,
      returnOverview = false,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = hideButton,
      title = title,
      h1 = h1
    )
    )(fakeRequest, messages).toString

}



