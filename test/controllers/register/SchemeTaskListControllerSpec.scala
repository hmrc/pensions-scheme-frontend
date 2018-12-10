/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register

import base.{JsonFileReader, SpecBase}
import controllers.ControllerSpecBase
import controllers.actions._
import models.NormalMode
import play.api.test.Helpers._
import viewmodels._
import views.html.schemeTaskList

class SchemeTaskListControllerSpec extends ControllerSpecBase {

  import SchemeTaskListControllerSpec._

  private val journeyTL = JourneyTaskList(expectedAboutSection, Seq(expectedEstablishersCompany, expectedEstablishersIndividual),
    Seq(expectedTrustees), expectedWorkingKnowledgeSection, expectedDeclarationLink, expectedChangeTrusteeHeader)

  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))

  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction
    )

  def viewAsString(): String =
    schemeTaskList(
      frontendAppConfig, journeyTL
    )(fakeRequest, messages).toString()

  "SchemeTaskList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}

object SchemeTaskListControllerSpec extends SpecBase with JsonFileReader {
  private val userAnswersJson = readJsonFromFile("/payload.json")

  private val expectedAboutSection = JourneyTaskListSection(
    Some(true),
    Link(messages("messages__schemeTaskList__about_link_text"),
      controllers.register.routes.CheckYourAnswersController.onPageLoad.url),
    None)

  private val expectedEstablishersCompany = JourneyTaskListSection(
    Some(true),
    Link(messages(messages("messages__schemeTaskList__company_link")),
      controllers.register.establishers.company.routes.CompanyReviewController.onPageLoad(0).url),
    Some("Test company name"))

  private val expectedEstablishersIndividual = JourneyTaskListSection(
    Some(true),
    Link(messages(messages("messages__schemeTaskList__individual_link")),
      controllers.register.establishers.individual.routes.CheckYourAnswersController.onPageLoad(1).url),
    Some("Test individual name"))

  private val expectedTrustees = JourneyTaskListSection(
    Some(true),
    Link(messages("messages__schemeTaskList__partnership_link"),
      controllers.register.trustees.partnership.routes.CheckYourAnswersController.onPageLoad(0).url),
    Some("Test partnership name"))

  private val expectedWorkingKnowledgeSection = JourneyTaskListSection(
    Some(true),
    Link(messages("messages__schemeTaskList__working_knowledge_change_link"),
      controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad().url),
    None)

  private val expectedDeclarationLink = Some(Link(messages("messages__schemeTaskList__declaration_link"),
    controllers.register.routes.DeclarationController.onPageLoad().url))

  private val expectedChangeTrusteeHeader = JourneyTaskListSection(
    None,
    Link(messages("messages__schemeTaskList__sectionTrustees_change_link"),
      controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode).url),
    None
  )
}
