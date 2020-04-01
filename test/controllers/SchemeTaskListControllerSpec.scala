/*
 * Copyright 2020 HM Revenue & Customs
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
import connectors._
import controllers.actions._
import handlers.ErrorHandler
import identifiers.{IsPsaSuspendedId, SchemeNameId, SchemeSrnId, SchemeStatusId}
import models._
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsNull
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers
import utils.hstasklisthelper.{HsTaskListHelperRegistration, HsTaskListHelperVariations}
import viewmodels._
import views.html.{error_template, error_template_page_not_found, schemeDetailsTaskList}

import scala.concurrent.Future

class SchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import SchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(fakeSchemeDetailsConnector, fakeMinimalPsaConnector, fakeLockConnector, fakeUpdateCacheConnector, fakeHsTaskListHelperRegistration)
  }

  "SchemeTaskList Controller" when {

    "accessed in NormalMode with srn as None" must {

      "return OK and the correct view" in {
        when(fakeHsTaskListHelperRegistration.taskList(any(), any(), any())).thenReturn(schemeDetailsTL)
        val result = controller(UserAnswers().set(SchemeNameId)("test scheme").asOpt.value.dataRetrievalAction)
          .onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe view(schemeDetailsTL)(fakeRequest, messages).toString()
      }
    }

    "accessed in NormalMode with no user answers and srn as None" must {
      "return OK and the correct view" in {
        val result = controller(new FakeDataRetrievalAction(None)).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(frontendAppConfig.managePensionsSchemeOverviewUrl.url)
      }
    }

    "In UpdateMode when user holds the lock" must {

      "return OK and the correct view for a GET where scheme status is open" in {
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any()))
          .thenReturn(Future.successful(false))
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(VarianceLock)))
        when(fakeUpdateCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(declaration =
          Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
          "messages__schemeTaskList__sectionDeclaration_incomplete_v1", "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))))

        val result = controller().onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result).contains(messages("messages__scheme_details__title")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false

        val updatedUserAnswers =
          UserAnswers(userAnswersJson).set(IsPsaSuspendedId)(false).flatMap(_.set(SchemeSrnId)(srnValue)).asOpt.getOrElse(UserAnswers(userAnswersJson))

        verify(fakeUpdateCacheConnector, times(1)).upsert(any(), eqTo(updatedUserAnswers.json))(any(), any())

      }

      "return OK and the correct view for a GET where scheme status is rejected" in {
        when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(UserAnswers(userAnswersJsonRejected)))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any()))
          .thenReturn(Future.successful(false))
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(None))
        when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL)

        val result = controller(dataRetrievalAction = userAnswersRejected).onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result).contains(messages("messages__scheme_details__title")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe false
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false
      }

      "return OK and correct view when viewOnly flag set to true if the scheme is locked by another psa and cannot be edited" in {
        val answers = UserAnswers().set(SchemeStatusId)("Open").asOpt.value
        when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(answers))
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(SchemeLock)))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
        when(fakeUpdateCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(addTrusteeHeader = Some(
          SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionTrustees_no_trustees"))
        )))

        val result = controller(dataRetrievalAction = getEmptyData).onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK

        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe false
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe true
      }

      "return OK and correct view when viewOnly flag set to false if scheme is locked by same psa and can be edited" in {
        val answers = UserAnswers().set(SchemeStatusId)("Open").asOpt.value
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(VarianceLock)))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
        when(fakeUpdateCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(declaration =
          Some(SchemeDetailsTaskListEntitySection(None, Nil, Some("messages__schemeTaskList__sectionDeclaration_header"),
            "messages__schemeTaskList__sectionDeclaration_incomplete_v1", "messages__schemeTaskList__sectionDeclaration_incomplete_v2"))))

        val result = controller(dataRetrievalAction = answers.dataRetrievalAction)
          .onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK

        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false
      }

      "return OK and correct view when viewOnly flag set to true if the scheme is not locked but the scheme status is rejected" in {
        val answers = UserAnswers().set(SchemeStatusId)("Rejected").asOpt.value
        when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(answers))
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(None))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
        when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))
        when(fakeHsTaskListHelperVariation.taskList(any(), any(), any())).thenReturn(schemeDetailsTL.copy(addTrusteeHeader = Some(
          SchemeDetailsTaskListEntitySection(None, Nil, None, Message("messages__schemeTaskList__sectionTrustees_no_trustees"))
        )))

        val result = controller(dataRetrievalAction = answers.dataRetrievalAction)
          .onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK

        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe false
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe true
      }

    }
  }
}

object SchemeTaskListControllerSpec extends ControllerSpecBase with MockitoSugar with JsonFileReader {

  private val view = injector.instanceOf[schemeDetailsTaskList]
  private val fakeSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]
  private val fakeSchemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  private val fakeUpdateCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  private val fakeLockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  private val fakeMinimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  private val fakeHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]
  private val fakeHsTaskListHelperVariation = mock[HsTaskListHelperVariations]

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
      fakeSchemeDetailsConnector,
      fakeLockConnector,
      fakeSchemeDetailsReadOnlyCacheConnector,
      fakeUpdateCacheConnector,
      fakeMinimalPsaConnector,
      stubMessagesControllerComponents(),
      view,
      fakeHsTaskListHelperRegistration,
      fakeHsTaskListHelperVariation
    )

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswersJsonRejected = readJsonFromFile("/payloadRejected.json")
  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))
  private val userAnswersRejected = new FakeDataRetrievalAction(Some(userAnswersJsonRejected))
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
