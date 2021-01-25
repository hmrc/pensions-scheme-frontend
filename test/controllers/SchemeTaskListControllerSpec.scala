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

package controllers

import base.JsonFileReader
import controllers.actions._
import identifiers.SchemeNameId
import models.FeatureToggle.Enabled
import models.FeatureToggleName.TCMP
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import services.FeatureToggleService

import utils.UserAnswers
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import viewmodels._
import views.html.schemeDetailsTaskList

import scala.concurrent.Future

class SchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import SchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(fakeHsTaskListHelperRegistration)
    when(featureToggleService.get(any())(any(), any())).thenReturn(Future.successful(Enabled(TCMP)))
  }

  "SchemeTaskList Controller" when {

    "srn is None and there is user answers" must {
      "return OK and the correct view" in {
        when(fakeHsTaskListHelperRegistration.taskList(any(), any(), any())).thenReturn(schemeDetailsTL)
        val result = controller(UserAnswers().set(SchemeNameId)("test scheme").asOpt.value.dataRetrievalAction)
          .onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe view(schemeDetailsTL)(fakeRequest, messages).toString()
      }
    }

    "srn as None and no user answers" must {
      "return REDIRECT to manage" in {
        val result = controller(new FakeDataRetrievalAction(None)).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.managePensionsSchemeOverviewUrl.url)
      }
    }

    "srn is some value and there is user answers" must {
      "return OK and the correct view" in {
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(declaration =
          Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
            "messages__schemeTaskList__sectionDeclaration_incomplete_v1", "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))))

        val result = controller().onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result).contains(messages("messages__scheme_details__title")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false
      }
    }

    "srn is some value and there is no user answers" must {
      "return REDIRECT to session expired page" in {
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(declaration =
          Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
            "messages__schemeTaskList__sectionDeclaration_incomplete_v1", "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))))

        val result = controller(new FakeDataRetrievalAction(None)).onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}

object SchemeTaskListControllerSpec extends ControllerSpecBase with MockitoSugar with JsonFileReader {
  private val view = injector.instanceOf[schemeDetailsTaskList]
  private val fakeHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]
  private val fakeHsTaskListHelperVariation = mock[HsTaskListHelperVariations]
  private val featureToggleService: FeatureToggleService = mock[FeatureToggleService]

  private val srnValue = "S1000000456"
  private val srn = Some(srnValue)
  private val schemeName = "test scheme"

  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      controllerComponents,
      view,
      fakeHsTaskListHelperRegistration,
      fakeHsTaskListHelperVariation,
      featureToggleService
    )

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))
  private val beforeYouStartLinkText = Message("messages__schemeTaskList__before_you_start_link_text", schemeName)
  private val expectedBeforeYouStartSpoke = Seq(EntitySpoke(TaskListLink(beforeYouStartLinkText,
    controllers.routes.SchemeNameController.onPageLoad(NormalMode).url), Some(false)))
  private val whatYouWillNeedMemberPage = controllers.routes.WhatYouWillNeedMembersController.onPageLoad().url
  private val addMembersLinkText = Message("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private val expectedAboutSpoke = Seq(EntitySpoke(TaskListLink(addMembersLinkText, whatYouWillNeedMemberPage), None))
  private val aboutHeader = Some(Message("messages__schemeTaskList__about_scheme_header", schemeName))

  private val beforeYouStartHeader = Some(Message("messages__schemeTaskList__before_you_start_header"))

  private val schemeDetailsTL = SchemeDetailsTaskList(
    schemeName, None,
    beforeYouStart = SchemeDetailsTaskListEntitySection(None, expectedBeforeYouStartSpoke, beforeYouStartHeader),
    about = SchemeDetailsTaskListEntitySection(None, expectedAboutSpoke, aboutHeader),
    workingKnowledge = None,
    addEstablisherHeader = None,
    establishers = Nil,
    addTrusteeHeader = None,
    trustees = Nil,
    declaration = None,
    isAllSectionsComplete = None
  )
}
