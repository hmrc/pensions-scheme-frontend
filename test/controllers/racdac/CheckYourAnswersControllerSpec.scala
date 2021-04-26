/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.racdac

import connectors.PensionAdministratorConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.racdac.{RACDACContractOrPolicyNumberId, RACDACNameId}
import models.{CheckMode, Link}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.FakeCountryOptions
import viewmodels.{AnswerRow, AnswerSection, CYAViewModel, Message}
import views.html.racdac.checkYourAnswers

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends ControllerSpecBase {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" when {

    "onPageLoad() is called" must {
      "return OK and the correct view" in {
        when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
        val result = controller(racdacInfo).onPageLoad(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase {
  private val racDACName = "Test RACDAC Name"
  private val racDACContractNo = "Test RACDAC Contract No"
  private val view = injector.instanceOf[checkYourAnswers]
  private val mockPensionAdministratorConnector= mock[PensionAdministratorConnector]
  private def controller(dataRetrievalAction: DataRetrievalAction): CheckYourAnswersController =
    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeCountryOptions,
      controllerComponents,
      mockPensionAdministratorConnector,
      view
    )

  private val postUrl = controllers.racdac.routes.DeclarationController.onPageLoad()

  private val racdacInfo = new FakeDataRetrievalAction(
    Some(Json.obj(
      "racdac" -> Json.obj(
        RACDACNameId.toString -> racDACName,
        RACDACContractOrPolicyNumberId.toString -> racDACContractNo
      )
    ))
  )


  private val racdacSectionName = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("messages__racdac_name__title"),
        Seq(racDACName),
        answerIsMessageKey = false,
        Some(Link("site.change", controllers.racdac.routes.RACDACNameController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__racdac_name"))))
      )
    )
  )

  private val racdacSectionContractNo = AnswerSection(
    None,
    Seq(
      AnswerRow(
        messages("messages__racdac_contract_or_policy_number__title", racDACName),
        Seq(racDACContractNo),
        answerIsMessageKey = false,
        Some(Link("site.change", controllers.racdac.routes.RACDACContractOrPolicyNumberController.onPageLoad(CheckMode).url,
          Some(messages("messages__visuallyhidden__racdac_contract_or_policy_number", racDACName))))
      )
    )
  )

  val vm = CYAViewModel(
    answerSections = Seq(racdacSectionName, racdacSectionContractNo),
    href = postUrl,
    schemeName = None,
    returnOverview = true,
    hideEditLinks = false,
    srn = None,
    hideSaveAndContinueButton = false,
    title = Message("checkYourAnswers.hs.title"),
    h1 = Message("checkYourAnswers.hs.title")
  )

  private def viewAsString(): String = view(vm)(fakeRequest, messages).toString

}



