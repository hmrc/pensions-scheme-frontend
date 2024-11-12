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

package controllers.register.trustees.company

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.register.trustees.routes._
import controllers.routes.PsaSchemeTaskListController
import identifiers.register.trustees.company.CompanyConfirmPreviousAddressId
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.address.Address
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils._
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach{

  import CheckYourAnswersCompanyAddressControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggle)
    when(mockFeatureToggle.get(any())(any(),any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "Check Your Answers Company Address Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {

        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(companyAddressNormal,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "on Page load in UpdateMode" must {

      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(
          companyAddressUpdate, srn, postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__theCompany").resolve),
          h1 = Message("messages__addressFor", companyName)
        )
      }

      "return OK and the correct view with partial answers" in {
        val request = FakeDataRequest(partialAnswersForAddLink)
        val result = controller(partialAnswersForAddLink.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyAddressSectionWithAddLink, srn, postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__theCompany").resolve),
          h1 = Message("messages__addressFor", companyName))
      }
    }

    "rendering submit button_link" must {

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, index, None)(FakeDataRequest(fullAnswers))
      )
    }
  }
}

object CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, None)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index: Index = Index(0)
  val companyName = "Test company Name"
  val srn: OptionalSchemeReferenceNumber = Some(SchemeReferenceNumber("S123"))

  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")
  private val emptyAnswers = UserAnswers()
  private val mockFeatureToggle = mock[FeatureToggleService]

  private def companyAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.CompanyAddressController.onPageLoad(mode, index, srn).url

  private def companyAddressYearsRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.CompanyAddressYearsController.onPageLoad(mode, index, srn).url
  private def companyTradingTimeRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.HasBeenTradingCompanyController.onPageLoad(mode, index, srn).url

  private def companyPreviousAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.CompanyPreviousAddressController.onPageLoad(mode, index, srn).url

  private val fullAnswers = emptyAnswers.
    trusteesCompanyDetails(index, CompanyDetails(companyName)).
    trusteesCompanyAddress(index, address).
    trusteesCompanyAddressYears(index, addressYearsUnderAYear).
    trusteeCompanyTradingTime(index, hasBeenTrading = true).
    trusteesCompanyPreviousAddress(index, previousAddress)

  private val partialAnswersForAddLink = emptyAnswers.
    trusteesCompanyDetails(index, CompanyDetails(companyName)).
    trusteesCompanyAddress(index, address).set(CompanyConfirmPreviousAddressId(index))(value = false).asOpt.value

  def postUrl: Call = PsaSchemeTaskListRegistrationTrusteeController.onPageLoad(index)

  def postUrlUpdateMode: Call = PsaSchemeTaskListController.onPageLoad(UpdateMode, srn)

  def addressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteeAddress", companyName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", companyAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_address", companyName))))
  )

  def addressYearsAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteeAddressYears__heading", companyName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", companyAddressYearsRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_addressYears", companyName))))
  )

  def tradingTimeAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__hasBeenTrading__h1", companyName),
    Seq("site.yes"),
    answerIsMessageKey = true,
    Some(Link("site.change", companyTradingTimeRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic__hasBeenTrading", companyName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__trusteePreviousAddress", companyName),
    UserAnswers().addressAnswer(previousAddress),
    answerIsMessageKey = false,
    Some(Link("site.change", companyPreviousAddressRoute(checkMode(mode), srn),
      Some(Message("messages__visuallyhidden__dynamic_previousAddress", companyName))))
  )

  def previousAddressAddLink(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__trusteePreviousAddress", companyName),
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", companyPreviousAddressRoute(checkMode(mode), srn),
        Some(Message("messages__visuallyhidden__dynamic_previousAddress", companyName)))))

  def companyAddressNormal: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(NormalMode, None), addressYearsAnswerRow(NormalMode, None),
    tradingTimeAnswerRow(NormalMode, None), previousAddressAnswerRow(NormalMode, None)
  )))

  def companyAddressUpdate: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, srn), previousAddressAnswerRow(UpdateMode, srn))))

  def companyAddressSectionWithAddLink: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, srn), previousAddressAddLink(UpdateMode, srn))))

  private val view = injector.instanceOf[checkYourAnswers]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersCompanyAddressController =
    new CheckYourAnswersCompanyAddressController(frontendAppConfig, messagesApi, FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl,
      fakeCountryOptions, allowChangeHelper, controllerComponents, view, mockFeatureToggle)

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = postUrl, title:Message, h1:Message): String =
    view(CYAViewModel(
      answerSections = answerSections,
      href = postUrl,
      schemeName = None,
      returnOverview = false,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false,
      title = title,
      h1 = h1
    ))(fakeRequest, messages).toString

}



