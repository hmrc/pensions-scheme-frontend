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

import base.JsonFileReader
import config.{FeatureSwitchManagementService, FeatureSwitchManagementServiceTestImpl}
import connectors._
import controllers.actions._
import handlers.ErrorHandler
import identifiers.{IsPsaSuspendedId, SchemeNameId, SchemeSrnId, SchemeStatusId}
import models._
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, when, _}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.JsNull
import play.api.test.Helpers._
import utils.{FakeFeatureSwitchManagementService, UserAnswers}
import viewmodels._
import views.html.{schemeDetailsTaskList, schemeDetailsTaskListNonHns}

import scala.concurrent.Future

class SchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import SchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(fakeSchemeDetailsConnector, fakeMinimalPsaConnector, fakeLockConnector, fakeUpdateCacheConnector)
  }

  "SchemeTaskList Controller" when {

    "accessed in NormalMode with srn as None" must {

      "return OK and the correct view" in {
        val result = controller(UserAnswers().set(SchemeNameId)("test scheme").asOpt.value.dataRetrievalAction, isHnsEnabled = true)
          .onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe schemeDetailsTaskList(frontendAppConfig, schemeDetailsTL)(fakeRequest, messages).toString()

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

        val result = controller(dataRetrievalAction = getEmptyData)
          .onPageLoad(UpdateMode, srn)(fakeRequest)

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

  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers, isHnsEnabled: Boolean = false): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      fakeSchemeDetailsConnector,
      new ErrorHandler(frontendAppConfig, messagesApi),
      new FakeFeatureSwitchManagementService(isHnsEnabled),
      fakeLockConnector,
      fakeSchemeDetailsReadOnlyCacheConnector,
      fakeUpdateCacheConnector,
      fakeMinimalPsaConnector
    )

  val fakeSchemeDetailsConnector: SchemeDetailsConnector                           = mock[SchemeDetailsConnector]
  val fakeSchemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  val fakeUpdateCacheConnector: UpdateSchemeCacheConnector                         = mock[UpdateSchemeCacheConnector]
  val fakeLockConnector: PensionSchemeVarianceLockConnector                        = mock[PensionSchemeVarianceLockConnector]
  val fakeMinimalPsaConnector: MinimalPsaConnector                                 = mock[MinimalPsaConnector]
  val config                                                                       = injector.instanceOf[Configuration]

  val srnValue = "S1000000456"
  val srn      = Some(srnValue)
  val schemeName = "test scheme"

  private val userAnswersJson                           = readJsonFromFile("/payload.json")
  private val userAnswersJsonRejected                   = readJsonFromFile("/payloadRejected.json")
  private val userAnswers                               = new FakeDataRetrievalAction(Some(userAnswersJson))
  private val userAnswersRejected                       = new FakeDataRetrievalAction(Some(userAnswersJsonRejected))
  private lazy val beforeYouStartLinkText               = messages("messages__schemeTaskList__before_you_start_link_text", schemeName)
  private lazy val addEstablisherLinkText               = messages("messages__schemeTaskList__sectionEstablishers_add_link", schemeName)
  private lazy val aboutMembersAddLinkText              = messages("messages__schemeTaskList__about_members_link_text_add", schemeName)
  private lazy val aboutBenefitsAndInsuranceAddLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text_add", schemeName)
  private lazy val aboutBankDetailsAddLinkText          = messages("messages__schemeTaskList__about_bank_details_link_text_add", schemeName)
  private lazy val addTrusteesLinkText                  = messages("messages__schemeTaskList__sectionTrustees_add_link", schemeName)

  private val schemeDetailsTL = SchemeDetailsTaskList(
    SchemeDetailsTaskListSection(None, Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)),
    messages("messages__schemeTaskList__about_scheme_header", "test scheme"),
    Seq(
      SchemeDetailsTaskListSection(None, Link(aboutMembersAddLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url), None),
      SchemeDetailsTaskListSection(None,
                                   Link(aboutBenefitsAndInsuranceAddLinkText, controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url),
                                   None),
      SchemeDetailsTaskListSection(None, Link(aboutBankDetailsAddLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url), None)
    ),
    None,
    Some(
      SchemeDetailsTaskListHeader(
        None,
        Some(Link(addEstablisherLinkText, controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url)),
        None)),
    Seq.empty,
    Some(
      SchemeDetailsTaskListHeader(
        None,
        Some(Link(addTrusteesLinkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url)),
        None)),
    Seq.empty,
    Some(
      SchemeDetailsTaskListDeclarationSection("messages__schemeTaskList__sectionDeclaration_header",
                                              None,
                                              incompleteDeclarationText = "messages__schemeTaskList__sectionDeclaration_incomplete")),
    "test scheme",
    messages("messages__scheme_details__title"),
    Some(messages("messages__schemeTaskList__before_you_start_header")),
    messages("messages__schemeTaskList__title"),
    None
  )
}
