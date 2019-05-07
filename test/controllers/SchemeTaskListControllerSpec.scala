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
import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, SchemeDetailsConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import handlers.ErrorHandler
import models.details.transformation.{SchemeDetailsMasterSection, SchemeDetailsStubData}
import models.{Link, NormalMode, SchemeLock, UpdateMode, VarianceLock}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsNull, Json}
import play.api.test.Helpers._
import utils.UserAnswers
import viewmodels._
import views.html.{psa_scheme_details, schemeDetailsTaskList}

import scala.concurrent.Future

class SchemeTaskListControllerSpec extends ControllerSpecBase {

  import SchemeTaskListControllerSpec._

  "SchemeTaskList Controller" when {

    "accessed in NormalMode with srn as None" must {

      "return OK and the correct view" in {
        val result = controller(UserAnswers().dataRetrievalAction).onPageLoad(NormalMode, None)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe schemeDetailsTaskList(frontendAppConfig, schemeDetailsTL, isVariations = false)(fakeRequest, messages).toString()
      }
    }


    "when isVariationsEnabled toggle switched off in UpdateMode" must {

      "return OK and the correct view for a GET" in {

        reset(fakeSchemeDetailsConnector)
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

      "return OK and the correct view for a GET" in {

        reset(fakeSchemeDetailsConnector)
        fs.change("is-variations-enabled", true)
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any()))
          .thenReturn(Future.successful(false))
        when(fakeSchemeTransformer.transformMasterSection(any())).thenReturn(masterSections)
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(VarianceLock)))
        when(fakeUpdateCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))

        val result = controller().onPageLoad(UpdateMode, srn)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result).contains(messages("messages__scheme_details__title")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe true
        contentAsString(result).contains(messages("messages__schemeTaskList__sectionTrustees_no_trustees")) mustBe false
      }

      "return OK and correct view when viewOnly flag set to true" in {

        val userAnswersResponse = UserAnswers(Json.obj(
          "test attribute" -> "test value"
        ))

        when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(userAnswersResponse))
        when(fakeSchemeTransformer.transformMasterSection(any())).thenReturn(masterSections)
        when(fakeLockConnector.isLockByPsaIdOrSchemeId(any(), any())(any(), any())).thenReturn(Future.successful(Some(SchemeLock)))
        when(fakeMinimalPsaConnector.isPsaSuspended(any())(any(), any())).thenReturn(Future.successful(false))
        when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(any(), any())(any(), any()))
          .thenReturn(Future.successful(JsNull))

        val result = controller(dataRetrievalAction = dontGetAnyDataViewOnly)
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

  private val userAnswers = new FakeDataRetrievalAction(Some(userAnswersJson))
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
    Some(SchemeDetailsTaskListDeclarationSection(None)),
    messages("messages__schemeTaskList__heading"),
    messages("messages__schemeTaskList__before_you_start_header"),
    None,
    messages("messages__schemeTaskList__title")
  )
}
