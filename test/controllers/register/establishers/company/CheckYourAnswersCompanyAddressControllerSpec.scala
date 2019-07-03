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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import controllers.behaviours.ControllerAllowChangeBehaviour
import identifiers.register.establishers.company._
import models._
import models.Mode.checkMode
import models.address.Address
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.{CountryOptions, FakeCountryOptions, FakeNavigator, UserAnswers, _}
import viewmodels.{AnswerRow, AnswerSection}
import views.html.check_your_answers

class CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour {

  import CheckYourAnswersCompanyAddressControllerSpec._

  "Check Your Answers Company Address Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, None, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyAddressNormal)
      }
    }

    "on Page load in UpdateMode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyAddressUpdate, srn, postUrlUpdateMode)
      }

      "return OK and the correct view with partial answers" in {
        val request = FakeDataRequest(partialAnswers)
        val result = controller(partialAnswers.dataRetrievalAction).onPageLoad(UpdateMode, srn, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyAddressUpdatePartial, srn, postUrlUpdateMode)
      }
    }

    "on Submit" must {
      "redirect to next page " in {
        val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "mark company address as complete" in {
        val result = controller().onSubmit(NormalMode, None, index)(fakeRequest)
        status(result) mustBe SEE_OTHER
        FakeUserAnswersService.verify(IsAddressCompleteId(index), true)
      }

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, None, index)(FakeDataRequest(fullAnswers))
      )
    }
  }

}

object CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index = Index(0)
  val testSchemeName = "Test Scheme Name"
  val companyName = "Test company Name"
  val srn = Some("S123")

  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val addressYearsOverAYear = AddressYears.OverAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()
  private def companyAddressRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyAddressController.onPageLoad(mode, srn, Index(index)).url
  private def companyAddressYearsRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyAddressYearsController.onPageLoad(mode, srn, Index(index)).url
  private def companyPreviousAddressRoute(mode: Mode, srn: Option[String]) =
    routes.CompanyPreviousAddressController.onPageLoad(mode, srn, Index(index)).url

  private val fullAnswers = emptyAnswers.
    establisherCompanyDetails(0, CompanyDetails(companyName)).
    establishersCompanyAddress(0, address).
    establisherCompanyAddressYears(0, addressYearsUnderAYear).
    establisherCompanyTradingTime(0, true).
    establishersCompanyPreviousAddress(0, previousAddress)

  private val partialAnswers = emptyAnswers.
    establisherCompanyDetails(0, CompanyDetails(companyName)).
    establishersCompanyAddress(0, address).
    establisherCompanyAddressYears(0, addressYearsUnderAYear)

  def postUrl: Call = routes.CheckYourAnswersCompanyAddressController.onSubmit(NormalMode, None, index)

  def postUrlUpdateMode: Call = routes.CheckYourAnswersCompanyAddressController.onSubmit(UpdateMode, srn, index)

  def addressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    "messages__common__cya__address",
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", companyAddressRoute(checkMode(mode), srn),
      Some("messages__visuallyhidden__establisher__address")))
  )
  def addressYearsAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    "companyAddressYears.checkYourAnswersLabel",
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", companyAddressYearsRoute(checkMode(mode), srn),
      Some("messages__visuallyhidden__establisher__address_years")))
  )
  def previousAddressAnswerRow(mode: Mode, srn: Option[String]): AnswerRow = AnswerRow(
    "messages__common__cya__previous_address",
    UserAnswers().addressAnswer(previousAddress),
    answerIsMessageKey = false,
    Some(Link("site.change", companyPreviousAddressRoute(checkMode(mode), srn),
      Some("messages__visuallyhidden__establisher__previous_address")))
  )

  def previousAddressAddLink(mode: Mode, srn: Option[String]): AnswerRow =
    AnswerRow("messages__common__cya__previous_address",
    Seq("site.not_entered"),
    answerIsMessageKey = true,
    Some(Link("site.add", companyPreviousAddressRoute(checkMode(mode), srn), Some("messages__visuallyhidden__establisher__previous_address_add"))))

  def companyAddressNormal: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(NormalMode, None), addressYearsAnswerRow(NormalMode, None),
    previousAddressAnswerRow(NormalMode, None)
  )))

  def companyAddressUpdate: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, srn), previousAddressAnswerRow(UpdateMode, srn))))
  def companyAddressUpdatePartial: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, srn), previousAddressAddLink(UpdateMode, srn))))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach,
                 isToggleOn: Boolean = false): CheckYourAnswersCompanyAddressController =
    new CheckYourAnswersCompanyAddressController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      new FakeNavigator(onwardRoute),
      FakeUserAnswersService,
      allowChangeHelper,
      new FakeFeatureSwitchManagementService(isToggleOn)
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: Option[String] = None, postUrl: Call = postUrl): String =
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



