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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions.*
import controllers.behaviours.ControllerAllowChangeBehaviour
import controllers.routes.PsaSchemeTaskListController
import identifiers.register.establishers.company.CompanyConfirmPreviousAddressId
import models.*
import models.Mode.checkMode
import models.address.Address
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.{AllowChangeHelper, CountryOptions, Enumerable, FakeCountryOptions, FakeDataRequest, UserAnswerOps, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers


class CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with ControllerAllowChangeBehaviour with BeforeAndAfterEach {

  import CheckYourAnswersCompanyAddressControllerSpec.*

  "Check Your Answers Company Address Controller " when {
    "on Page load in Normal Mode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(request)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(companyAddressNormal,
          title = Message("checkYourAnswers.hs.heading"),
          h1 = Message("checkYourAnswers.hs.heading"))
      }
    }

    "on Page load in UpdateMode" must {
      "return OK and the correct view with full answers" in {
        val request = FakeDataRequest(fullAnswers)
        val result = controller(fullAnswers.dataRetrievalAction).onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn), index)(request)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(companyAddressUpdate, OptionalSchemeReferenceNumber(srn), postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__theCompany")),
          h1 = Message("messages__addressFor", companyName))
      }

      "return OK and the correct view with partial answers" in {
        val request = FakeDataRequest(partialAnswers)
        val result = controller(partialAnswers.dataRetrievalAction).onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn), index)(request)

        status(result) mustBe OK

        contentAsString(result) mustBe viewAsString(companyAddressUpdatePartial, OptionalSchemeReferenceNumber(srn), postUrlUpdateMode,
          title = Message("messages__addressFor", Message("messages__theCompany")),
          h1 = Message("messages__addressFor", companyName))
      }
    }

    "rendering submit button_link" must {

      behave like changeableController(
        controller(fullAnswers.dataRetrievalAction, _: AllowChangeHelper)
          .onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber, index)(FakeDataRequest(fullAnswers))
      )
    }
  }

}

object CheckYourAnswersCompanyAddressControllerSpec extends ControllerSpecBase with Enumerable.Implicits with ControllerAllowChangeBehaviour {

  def onwardRoute: Call = controllers.routes.PsaSchemeTaskListController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber)

  private implicit val fakeCountryOptions: CountryOptions = new FakeCountryOptions
  val index: Index = Index(0)
  val testSchemeName = "Test Scheme Name"
  val companyName = "Test company Name"
  val srn: OptionalSchemeReferenceNumber = OptionalSchemeReferenceNumber(Some(SchemeReferenceNumber("S123")))

  private val address = Address("address-1-line-1", "address-1-line-2", None, None, Some("post-code-1"), "country-1")
  private val addressYearsUnderAYear = AddressYears.UnderAYear
  private val previousAddress = Address("address-2-line-1", "address-2-line-2", None, None, Some("post-code-2"), "country-2")

  private val emptyAnswers = UserAnswers()
  private def companyAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.CompanyAddressController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), Index(index)).url
  private def companyAddressYearsRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.CompanyAddressYearsController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), Index(index)).url
  private def companyPreviousAddressRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String =
    routes.CompanyPreviousAddressController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), Index(index)).url
  private def companyTradingTimeRoute(mode: Mode, srn: OptionalSchemeReferenceNumber): String = routes.HasBeenTradingCompanyController.onPageLoad(mode, OptionalSchemeReferenceNumber(srn), index).url

  private val fullAnswers = emptyAnswers.
    establisherCompanyDetails(0, CompanyDetails(companyName)).
    establishersCompanyAddress(0, address).
    establisherCompanyAddressYears(0, addressYearsUnderAYear).
    establisherCompanyTradingTime(0, hasBeenTrading = true).
    establishersCompanyPreviousAddress(0, previousAddress)

  private val partialAnswers = emptyAnswers.
    establisherCompanyDetails(0, CompanyDetails(companyName)).
    establishersCompanyAddress(0, address).set(CompanyConfirmPreviousAddressId(index))(value = false).asOpt.value

  def postUrl: Call = controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(index)

  def postUrlUpdateMode: Call = PsaSchemeTaskListController.onPageLoad(UpdateMode, OptionalSchemeReferenceNumber(srn))

  def addressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__address__cya", companyName),
    UserAnswers().addressAnswer(address),
    answerIsMessageKey = false,
    Some(Link("site.change", companyAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(messages("messages__visuallyhidden__dynamic_address", companyName))))
  )
  def addressYearsAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__company_address_years__h1", companyName),
    Seq(s"messages__common__$addressYearsUnderAYear"),
    answerIsMessageKey = true,
    Some(Link("site.change", companyAddressYearsRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(messages("messages__visuallyhidden__dynamic_addressYears", companyName))))
  )

  def tradingTimeAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__hasBeenTrading__h1", companyName),
    Seq("site.yes"),
    answerIsMessageKey = true,
    Some(Link("site.change", companyTradingTimeRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(Message("messages__visuallyhidden__dynamic__hasBeenTrading", companyName))))
  )

  def previousAddressAnswerRow(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow = AnswerRow(
    Message("messages__previousAddress__cya", companyName),
    UserAnswers().addressAnswer(previousAddress),
    answerIsMessageKey = false,
    Some(Link("site.change", companyPreviousAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)),
      Some(messages("messages__visuallyhidden__dynamic_previousAddress", companyName))))
  )

  def previousAddressAddLink(mode: Mode, srn: OptionalSchemeReferenceNumber): AnswerRow =
    AnswerRow(Message("messages__previousAddress__cya", companyName),
    Seq("site.not_entered"),
    answerIsMessageKey = true,
    Some(Link("site.add", companyPreviousAddressRoute(checkMode(mode), OptionalSchemeReferenceNumber(srn)), Some(messages("messages__visuallyhidden__dynamic_previousAddress", companyName)))))

  def companyAddressNormal: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber), addressYearsAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber), tradingTimeAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber),
    previousAddressAnswerRow(NormalMode, EmptyOptionalSchemeReferenceNumber)
  )))

  def companyAddressUpdate: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)), previousAddressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)))))

  def companyAddressUpdatePartial: Seq[AnswerSection] = Seq(AnswerSection(None, Seq(
    addressAnswerRow(UpdateMode, OptionalSchemeReferenceNumber(srn)), previousAddressAddLink(UpdateMode, OptionalSchemeReferenceNumber(srn)))))

  private val view = injector.instanceOf[checkYourAnswers]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 allowChangeHelper: AllowChangeHelper = ach): CheckYourAnswersCompanyAddressController =
    new CheckYourAnswersCompanyAddressController(
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      fakeCountryOptions,
      allowChangeHelper,
      controllerComponents,
      view
    )

  def viewAsString(answerSections: Seq[AnswerSection], srn: OptionalSchemeReferenceNumber = EmptyOptionalSchemeReferenceNumber, postUrl: Call = postUrl,
                   title:Message, h1:Message): String =
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



