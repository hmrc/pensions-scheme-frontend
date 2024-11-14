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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.partnership.PartnershipConfirmPreviousAddressId
import models.FeatureToggleName.SchemeRegistration
import models.Mode.checkMode
import models._
import models.address.Address
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FeatureToggleService
import utils._
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers
import org.mockito.Mockito._

import scala.concurrent.Future

class CheckYourAnswersPartnershipAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach{
  import CheckYourAnswersPartnershipAddressControllerSpec._

  override def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "Check Your Answers Partnership Address Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result  = controller(fullAnswers.dataRetrievalAction).onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressNormal,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad( NormalMode, index, EmptyOptionalSchemeReferenceNumber)(FakeDataRequest(fullAnswers))
      )

    }

    "on Page load in UpdateMode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result  = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn))(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressUpdate, OptionalSchemeReferenceNumber(srn), postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__thePartnership").resolve),
          h1 = Message("messages__addressFor", partnershipName))
      }

      "return OK and the correct view with partial answers" in {
        val request = FakeDataRequest(partialAnswers)
        val result  = controller(partialAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, OptionalSchemeReferenceNumber(srn))(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressUpdatePartial, OptionalSchemeReferenceNumber(srn), postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__thePartnership").resolve),
          h1 = Message("messages__addressFor", partnershipName))
      }
    }
  }

}

object CheckYourAnswersPartnershipAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  private val index                                       = Index(0)
  private val partnershipName                             = "Test partnership Name"
  private val srn                                         = Some(SchemeReferenceNumber("S123"))

  private val address                = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val previousAddress        = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()
  private def partnershipAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.PartnershipAddressController.onPageLoad(mode, Index(index), OptionalSchemeReferenceNumber(srn)).url
  private def partnershipAddressYearsRoute(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.PartnershipAddressYearsController.onPageLoad(mode, Index(index), OptionalSchemeReferenceNumber(srn)).url
  private def partnershipPreviousAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.PartnershipPreviousAddressController.onPageLoad(mode, Index(index), OptionalSchemeReferenceNumber(srn)).url
  private def partnershipTradingTimeRoute(mode: Mode, srn: OptionalSchemeReferenceNumber) =
    routes.PartnershipHasBeenTradingController.onPageLoad(mode, Index(index), OptionalSchemeReferenceNumber(srn)).url

  private val fullAnswers = emptyAnswers
    .establisherPartnershipDetails(index, PartnershipDetails(partnershipName))
    .establisherPartnershipAddress(index, address)
    .establisherPartnershipAddressYears(index, addressYearsUnderAYear)
    .establisherPartnershipTradingTime(index, hasBeenTrading = true)
    .establishersPartnershipPreviousAddress(index, previousAddress)

  private val partialAnswers = emptyAnswers
    .establisherPartnershipDetails(index, PartnershipDetails(partnershipName))
    .establisherPartnershipAddress(index, address)
    .set(PartnershipConfirmPreviousAddressId(index))(value = false).asOpt.value

  private def postUrl: Call =
    controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)

  private def postUrlUpdateMode: Call =
    controllers.routes.PsaSchemeTaskListController.onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn))

  private def addressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__address__cya", partnershipName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", partnershipAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)), Some(Message("messages__visuallyhidden__dynamic_address", partnershipName))))
  )

  private def addressYearsAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__partnershipAddressYears__heading", partnershipName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", partnershipAddressYearsRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(Message("messages__visuallyhidden__dynamic_addressYears", partnershipName))))
  )

  def tradingTimeAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__hasBeenTrading__h1", partnershipName),
    Seq("site.yes"),
    answerIsMessageKey = true,
    Some(Link("site.change", partnershipTradingTimeRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(Message("messages__visuallyhidden__dynamic__hasBeenTrading", partnershipName))))
  )

  private def previousAddressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(
      Message("messages__previousAddress__cya", partnershipName),
      UserAnswers().addressAnswer(previousAddress),
      answerIsMessageKey = false,
      Some(Link("site.change", partnershipPreviousAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
        Some(Message("messages__visuallyhidden__dynamic_previousAddress", partnershipName))))
    )

  private def previousAddressAddLink(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(
      Message("messages__previousAddress__cya", partnershipName),
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", partnershipPreviousAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
        Some(Message("messages__visuallyhidden__dynamic_previousAddress", partnershipName))))
    )

  private def partnershipAddressNormal: Seq[AnswerSection] =
    Seq(
      AnswerSection(None,
        Seq(
          addressAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber),
          addressYearsAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber),
          tradingTimeAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber),
          previousAddressAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber)
        )))

  private def partnershipAddressUpdate: Seq[AnswerSection] =
    Seq(AnswerSection(None, Seq(addressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)), previousAddressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)))))
  private def partnershipAddressUpdatePartial: Seq[AnswerSection] =
    Seq(AnswerSection(None, Seq(addressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)), previousAddressAddLink(UpdateMode, OptionalSchemeReferenceNumber(srn)))))

  private val view = injector.instanceOf[checkYourAnswers]
  private val mockFeatureToggleService = mock[FeatureToggleService]
  private def controller(dataRetrievalAction: DataRetrievalAction,
                         allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersPartnershipAddressController =
    new CheckYourAnswersPartnershipAddressController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  private def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber,
                           postUrl: Call = postUrl, title:Message, h1:Message): String =
    view(
      CYAViewModel(
        answerSections = answerSections,
        href = postUrl,
        schemeName = None,
        returnOverview = false,
        hideEditLinks = false,
        srn = srn,
        hideSaveAndContinueButton = false,
        title = title,
        h1 = h1
      )
    )(fakeRequest, messages).toString

}
