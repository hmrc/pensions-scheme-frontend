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
import identifiers.SchemeStatusId
import models._
import models.details.transformation.{SchemeDetailsMasterSection, SchemeDetailsStubData}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.JsNull
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels._
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.Future

class SchemeTaskListControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import SchemeTaskListControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(fakeSchemeDetailsConnector, fakeSchemeTransformer, fakeMinimalPsaConnector, fakeLockConnector, fakeUpdateCacheConnector)
  }

  "SchemeTaskList Controller" when {

    "accessed in NormalMode with srn as None" must {

      "return OK and the correct view" in {
        val result = controller(UserAnswers().dataRetrievalAction).onPageLoad(NormalMode, None)(fakeRequest)

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


    "when isVariationsEnabled toggle switched off in UpdateMode" must {

      "return OK and the correct view for a GET" in {
        fs.change("is-variations-enabled", false)
        when(fakeSchemeDetailsConnector.getSchemeDetails(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(psaSchemeDetailsSample))
        when(fakeSchemeTransformer.transformMasterSection(any())).thenReturn(masterSections)

        val result = controller().onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsStringVariationsToggleOff()
      }
    }

    "isVariationsEnabled toggle switched on in UpdateMode and user holds the lock" must {

      "return OK and the correct view for a GET where scheme status is open" in {
        fs.change("is-variations-enabled", true)
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

      "return OK and correct view when viewOnly flag set to true if the scheme is locked by another psa, have user answers and cannot be edited" in {
        val answers = UserAnswers().set(SchemeStatusId)("Open").asOpt.value
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(SchemeLock)))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
        when(fakeUpdateCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))

        val result = controller(dataRetrievalAction = answers.dataRetrievalAction)
          .onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK

        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe false
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe true
      }

      "return OK and correct view when viewOnly flag set to true if the scheme is locked by another psa, don't have user answers and cannot be edited" in {
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

object SchemeTaskListControllerSpec extends ControllerSpecBase with MockitoSugar with SchemeDetailsStubData with JsonFileReader {


  def controller(dataRetrievalAction: DataRetrievalAction = userAnswers): SchemeTaskListController =
    new SchemeTaskListController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      fakeSchemeDetailsConnector,
      fakeSchemeTransformer,
      new ErrorHandler(frontendAppConfig, messagesApi),
      fs,
      fakeLockConnector,
      fakeSchemeDetailsReadOnlyCacheConnector,
      fakeUpdateCacheConnector,
      fakeMinimalPsaConnector
    )


  val fakeSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]
  val fakeSchemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  val fakeUpdateCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  val fakeSchemeTransformer: SchemeDetailsMasterSection = mock[SchemeDetailsMasterSection]
  val fakeLockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  val fakeMinimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  val config = injector.instanceOf[Configuration]

  val fs: FeatureSwitchManagementService = new FeatureSwitchManagementServiceTestImpl(config, environment)

  val masterSections = Seq(individualMasterSection)
  val srnValue = "S1000000456"
  val srn = Some(srnValue)

  def viewAsStringVariationsToggleOff(): String =
    psa_scheme_details(
      frontendAppConfig, masterSections, psaSchemeDetailsSample.schemeDetails.name, srnValue
    )(fakeRequest, messages).toString()

  private val userAnswersJson = readJsonFromFile("/payload.json")
  private val userAnswersJsonRejected = readJsonFromFile("/payloadRejected.json")
  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))
  private val userAnswersRejected = new FakeDataRetrievalAction(Some(userAnswersJsonRejected))
  private lazy val beforeYouStartLinkText = messages("messages__schemeTaskList__before_you_start_link_text")
  private lazy val addEstablisherLinkText = messages("messages__schemeTaskList__sectionEstablishers_add_link")
  private lazy val aboutMembersLinkText = messages("messages__schemeTaskList__about_members_link_text")
  private lazy val aboutBenefitsAndInsuranceLinkText = messages("messages__schemeTaskList__about_benefits_and_insurance_link_text")
  private lazy val aboutBankDetailsLinkText = messages("messages__schemeTaskList__about_bank_details_link_text")
  private lazy val addTrusteesLinkText = messages("messages__schemeTaskList__sectionTrustees_add_link")

  private val schemeDetailsTL = SchemeDetailsTaskList(
    SchemeDetailsTaskListSection(None, Link(beforeYouStartLinkText, controllers.routes.SchemeNameController.onPageLoad(NormalMode).url)),
    messages("messages__schemeTaskList__about_header"),
    Seq(SchemeDetailsTaskListSection(None, Link(aboutMembersLinkText, controllers.routes.WhatYouWillNeedMembersController.onPageLoad.url), None),
      SchemeDetailsTaskListSection(None, Link(aboutBenefitsAndInsuranceLinkText,
        controllers.routes.WhatYouWillNeedBenefitsInsuranceController.onPageLoad.url), None),
      SchemeDetailsTaskListSection(None, Link(aboutBankDetailsLinkText, controllers.routes.WhatYouWillNeedBankDetailsController.onPageLoad.url), None)), None,
    Some(SchemeDetailsTaskListHeader(None, Some(Link(addEstablisherLinkText,
      controllers.register.establishers.routes.EstablisherKindController.onPageLoad(NormalMode, 0, None).url)), None)),
    Seq.empty,
    Some(SchemeDetailsTaskListHeader(None,
      Some(Link(addTrusteesLinkText, controllers.register.trustees.routes.TrusteeKindController.onPageLoad(NormalMode, 0, None).url)),
      None
    )),
    Seq.empty,
    Some(SchemeDetailsTaskListDeclarationSection("messages__schemeTaskList__sectionDeclaration_header", None,
      incompleteDeclarationText="messages__schemeTaskList__sectionDeclaration_incomplete")),
    messages("messages__schemeTaskList__heading"),
    messages("messages__schemeTaskList__before_you_start_header"),
    None,
    messages("messages__schemeTaskList__title"),
    None
  )
}
