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

package controllers

import connectors._
import controllers.actions._
import identifiers.{AdviserAddressId, AdviserEmailId, AdviserNameId}
import models.address.Address
import models.{CheckMode, Link, NormalMode}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.domain.PsaId

import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.checkYourAnswers

class AdviserCheckYourAnswersControllerSpec extends ControllerSpecBase with ScalaFutures {

  import AdviserCheckYourAnswersControllerSpec._

  "AdviserCheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {

      val result = controller(dataRetrievalAction = getMandatoryAdviser).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}


object AdviserCheckYourAnswersControllerSpec extends ControllerSpecBase with MockitoSugar {
  val schemeName = "Test Scheme Name"

  val psaId = PsaId("A0000000")

  private val psaName = Json.obj("psaName" -> "Test", "psaEmail" -> "email@test.com")

  lazy val adviserNameRoute: String = controllers.routes.AdviserNameController.onPageLoad(CheckMode, srn).url
  lazy val adviserEmailRoute: String = controllers.routes.AdviserEmailAddressController.onPageLoad(CheckMode, srn).url
  lazy val adviserPhoneRoute: String = controllers.routes.AdviserPhoneController.onPageLoad(CheckMode, srn).url
  lazy val postUrl: Call = routes.PsaSchemeTaskListController.onPageLoad(NormalMode, srn)

  val adviserName = "Xyx"
  val adviserEmail = "x@x.c"
  val adviserPhone = "0000"
  val adviserAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")

  val getMandatoryAdviser = new FakeDataRetrievalAction(Some(UserAnswers()
    .set(AdviserNameId)(adviserName)
    .asOpt
    .value
    .set(AdviserEmailId)(adviserEmail)
    .asOpt
    .value
    .set(AdviserAddressId)(adviserAddress)
    .asOpt
    .value
    .workingKnowledgePersonPhone(adviserPhone)
    .json
  ))

  val adviserSection = AnswerSection(None,
    Seq(
      AnswerRow("adviserName.checkYourAnswersLabel", Seq(adviserName), answerIsMessageKey = false,
        Some(Link("site.change", routes.AdviserNameController.onPageLoad(CheckMode, srn).url,
          Some(messages("messages__visuallyhidden__adviserName", adviserName))))),
      AnswerRow(Messages("adviserEmail.checkYourAnswersLabel", adviserName), Seq(adviserEmail), answerIsMessageKey = false,
        Some(Link("site.change", routes.AdviserEmailAddressController.onPageLoad(CheckMode, srn).url,
          Some(messages("messages__visuallyhidden__adviserEmail", adviserName))))),
      AnswerRow(Messages("adviserPhone.checkYourAnswersLabel", adviserName), Seq(adviserPhone), answerIsMessageKey = false,
        Some(Link("site.change", routes.AdviserPhoneController.onPageLoad(CheckMode, srn).url,
          Some(messages("messages__visuallyhidden__adviserPhone", adviserName))))),
      AnswerRow(Messages("adviserAddress.checkYourAnswersLabel", adviserName),
        Seq(
          adviserAddress.addressLine1,
          adviserAddress.addressLine2,
          adviserAddress.addressLine3.get,
          adviserAddress.addressLine4.get,
          adviserAddress.postcode.get,
          "Country of GB"),
        answerIsMessageKey = false,
        Some(Link("site.change", routes.AdviserAddressController.onPageLoad(CheckMode, srn).url,
          Some(messages("messages__visuallyhidden__adviser__address", adviserName)))))
    )
  )

  val vm = CYAViewModel(
    answerSections = Seq(adviserSection),
    href = postUrl,
    schemeName = None,
    returnOverview = false,
    hideEditLinks = false,
    srn = srn,
    hideSaveAndContinueButton = false,
    title = Message("checkYourAnswers.hs.title"),
    h1 = Message("checkYourAnswers.hs.title")
  )
  private val view = injector.instanceOf[checkYourAnswers]
  val viewAsString: String = view(vm)(fakeRequest, messages).toString

  private val onwardRoute = controllers.routes.IndexController.onPageLoad

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 psaName: JsValue = psaName
                ): AdviserCheckYourAnswersController =

    new AdviserCheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute),
      new FakeCountryOptions,
      controllerComponents,
      view
    )

}
