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

package controllers

import base.{JsonFileReader, SpecBase}
import controllers.actions._
import models.NormalMode
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels._
import views.html.schemeDetailsTaskList

class SchemeTaskListControllerSpec extends ControllerSpecBase {

  import SchemeTaskListControllerSpec._

  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  "SchemeTaskList Controller" must {

    "return OK and the correct view" in {
      val result = controller(UserAnswers().dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe schemeDetailsTaskList(frontendAppConfig, schemeDetailsTL)(fakeRequest, messages).toString()
    }
  }
}

object SchemeTaskListControllerSpec extends SpecBase with JsonFileReader {
  private val userAnswersJson = readJsonFromFile("/payload.json")

  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))
  private lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  private lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  private lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  private lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")

  private val schemeDetailsTL = SchemeDetailsTaskList(
    SchemeDetailsTaskListSection(None, Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)),
    Seq(SchemeDetailsTaskListSection(None, Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url), None),
      SchemeDetailsTaskListSection(None, Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url), None),
      SchemeDetailsTaskListSection(None, Link(aboutBankDetailsLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url), None)), None,
    SchemeDetailsTaskListSection(None, Link(addEstablisherLinkText,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0).url), None),
    Seq.empty,
    Some(SchemeDetailsTaskListSection(None,
      Link(addTrusteesLinkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url),
      None
    )),
    Seq.empty,
    None,
    ""
  )

  private lazy val changeEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_change_link")
}
