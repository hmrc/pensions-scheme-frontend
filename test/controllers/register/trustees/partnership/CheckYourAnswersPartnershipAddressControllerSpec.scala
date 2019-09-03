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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerAllowChangeBehaviour
import models.Mode.checkMode
import models._
import models.address.Address
import play.api.mvc.Call
import play.api.test.Helpers._
import utils._
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

class CheckYourAnswersPartnershipAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {
  import CheckYourAnswersPartnershipAddressControllerSpec._

  "Check Your Answers Partnership Address Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result  = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, index, None)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressNormal)
      }

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, index, None)(FakeDataRequest(fullAnswers))
      )

    }

    "on Page load in UpdateMode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result  = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressUpdate, srn, postUrlUpdateMode)
      }

      "return OK and the correct view with partial answers" in {
        val request = FakeDataRequest(partialAnswers)
        val result  = controller(partialAnswers.dataRetrievalAction).onPageLoad(UpdateMode, index, srn)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(partnershipAddressUpdatePartial, srn, postUrlUpdateMode)
      }
    }
  }

}

object CheckYourAnswersPartnershipAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  private val index                                       = Index(0)
  private val testSchemeName                              = "Test Scheme Name"
  private val partnershipName                             = "Test partnership Name"
  private val srn                                         = Some("S123")

  private val address                = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val addressYearsOverAYear  = AddressYears.OverAYear
  private val previousAddress        = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()
  private def partnershipAddressRoute(mode: Mode, srn: Option[String]) =
    routes.PartnershipAddressController.onPageLoad(mode, Index(index), srn).url
  private def partnershipAddressYearsRoute(mode: Mode, srn: Option[String]) =
    routes.PartnershipAddressYearsController.onPageLoad(mode, Index(index), srn).url
  private def partnershipPreviousAddressRoute(mode: Mode, srn: Option[String]) =
    routes.PartnershipPreviousAddressController.onPageLoad(mode, Index(index), srn).url

  private val fullAnswers = emptyAnswers
    .trusteePartnershipDetails(0, PartnershipDetails(partnershipName))
    .trusteePartnershipAddress(0, address)
    .trusteePartnershipAddressYears(0, addressYearsUnderAYear)
    .trusteePartnershipTradingTime(0, true)
    .trusteesPartnershipPreviousAddress(0, previousAddress)

  private val partialAnswers = emptyAnswers
    .trusteePartnershipDetails(0, PartnershipDetails(partnershipName))
    .trusteePartnershipAddress(0, address)
    .trusteePartnershipAddressYears(0, addressYearsUnderAYear)

  private def postUrl: Call =
    controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private def postUrlUpdateMode: Call =
    controllers.routes.SchemeTaskListController.onPageLoad(UpdateMode, srn)

  private def addressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__addressFor", partnershipName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", partnershipAddressRoute(checkMode(mode), srn), Some(Message("messages__changeTrusteeAddress", partnershipName))))
  )

  private def addressYearsAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    Message("messages__hasBeen1Year", partnershipName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", partnershipAddressYearsRoute(checkMode(mode), srn), Some(Message("messages__changeHasBeen1Year", partnershipName))))
  )

  private def previousAddressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow =
    AnswerRow(
      Message("messages__previousAddressFor", partnershipName),
      UserAnswers().addressAnswer(previousAddress),
      answerIsMessageKey = false,
      Some(Link("site.change", partnershipPreviousAddressRoute(checkMode(mode), srn), Some(Message("messages__changeTrusteePreviousAddress", partnershipName))))
    )

  private def previousAddressAddLink(mode: Mode, srn: Option[String]): AnswerRow =
    AnswerRow(
      Message("messages__previousAddressFor", partnershipName),
      Seq("site.not_entered"),
      answerIsMessageKey = true,
      Some(Link("site.add", partnershipPreviousAddressRoute(checkMode(mode), srn), Some(Message("messages__changeTrusteePreviousAddress", partnershipName))))
    )

  private def partnershipAddressNormal: Seq[AnswerSection] =
    Seq(
      AnswerSection(None,
                    Seq(
                      addressAnswerRow(NormalMode, None),
                      addressYearsAnswerRow(NormalMode, None),
                      previousAddressAnswerRow(NormalMode, None)
                    )))

  private def partnershipAddressUpdate: Seq[AnswerSection] =
    Seq(AnswerSection(None, Seq(addressAnswerRow(UpdateMode, srn), previousAddressAnswerRow(UpdateMode, srn))))
  private def partnershipAddressUpdatePartial: Seq[AnswerSection] =
    Seq(AnswerSection(None, Seq(addressAnswerRow(UpdateMode, srn), previousAddressAddLink(UpdateMode, srn))))

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                         allowChangeHelper: AllowChangeHelper = ach,
                         isToggleOn: Boolean = true): CheckYourAnswersPartnershipAddressController =
    new CheckYourAnswersPartnershipAddressController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper
    )

  private def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = postUrl): String =
    check_your_answers(
      frontendAppConfig,
      answerSections,
      postUrl,
      None,
      hideEditLinks = false,
      srn = srn,
      hideSaveAndContinueButton = false
    )(fakeRequest, messages).toString

}
