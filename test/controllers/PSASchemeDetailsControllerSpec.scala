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

import config.FeatureSwitchManagementService
import connectors.{PensionSchemeVarianceLockConnector, _}
import controllers.actions.{DataRetrievalAction, _}
import handlers.ErrorHandler
import identifiers.MinimalPsaDetailsId
import models.details.transformation.{SchemeDetailsMasterSection, SchemeDetailsStubData}
import models.details.{Name, PsaDetails}
import models.{IndividualDetails, Lock, MinimalPSA, VarianceLock}
import org.mockito.Matchers
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}
import play.api.test.Helpers.{contentAsString, _}
import utils.UserAnswers
import views.html.psa_scheme_details

import scala.concurrent.Future

class PSASchemeDetailsControllerSpec extends ControllerSpecBase {

  import PSASchemeDetailsControllerSpec._

  "SchemeDetailsController when isVariationsEnabled toggle switched off" must {
    "return OK and the correct view for a GET" in {
      val psaDetails3 = PsaDetails("A0000000", Some("org name test zero"), Some(Name(Some("Minnie"), Some("m"), Some("Mouse"))))
      val psaSchemeDetailsSampleAdministeredByLoggedInUser = psaSchemeDetailsSample copy (
        psaDetails = List(psaDetails1, psaDetails2, psaDetails3)
        )

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetails(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(psaSchemeDetailsSampleAdministeredByLoggedInUser))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)

      prepareLockAndCacheMocks()

      val result = controller(isVariationsEnabled = false).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()

      verifyLockAndCacheMocks()
    }
  }

  "SchemeDetailsController when isVariationsEnabled toggle switched on" must {
    "where no scheme is currently locked return OK, the correct view for a GET and verify " +
      "that lock is checked and view cache only is updated with correct json" in {
      resetLockAndCacheMocks()
      val userAnswersResponse = UserAnswers(Json.obj(
        "test attribute" -> "test value"
      ))

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(userAnswersResponse))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)
      when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(JsNull))

      prepareLockAndCacheMocks()

      val expectedSavedJson = Json.obj(
        MinimalPsaDetailsId.toString -> Json.toJson(minimalPSA)
      ) ++ userAnswersResponse.json.as[JsObject]

      val result = controller(isVariationsEnabled = true).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK

      val content = contentAsString(result)
      content.contains(messages("messages__scheme_details__title")) mustBe true
      content.contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe true

      verifyLockAndCacheMocks(lockTimes = 1, viewTimes = 1, minimalPsaTimes = 1, expectedSavedJson = Some(expectedSavedJson))
    }

    "where the scheme is currently locked verify that lock is checked and update cache only is updated with correct json" in {
      resetLockAndCacheMocks()
      val userAnswersResponse = UserAnswers(Json.obj(
        "test attribute" -> "test value"
      ))

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(userAnswersResponse))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)
      when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(JsNull))

      prepareLockAndCacheMocks(lock = Some(VarianceLock))

      val expectedSavedJson = Json.obj(
        MinimalPsaDetailsId.toString -> Json.toJson(minimalPSA)
      ) ++ userAnswersResponse.json.as[JsObject]

      val result = controller(isVariationsEnabled = true).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK

      verifyLockAndCacheMocks(lockTimes = 1, updateTimes = 1, minimalPsaTimes = 1, expectedSavedJson = Some(expectedSavedJson))
    }

    "return OK and no declaration section where request has viewOnly flag set to true" in {
      resetLockAndCacheMocks()
      val userAnswersResponse = UserAnswers(Json.obj(
        "test attribute" -> "test value"
      ))

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(userAnswersResponse))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)
      when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(JsNull))

      prepareLockAndCacheMocks()

      val result = controller(isVariationsEnabled = true, dataRetrievalAction = dontGetAnyDataViewOnly)
        .onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK

      val content = contentAsString(result)
      content.contains(messages("messages__schemeTaskList__sectionDeclaration_header")) mustBe false
    }
  }
}

private object PSASchemeDetailsControllerSpec extends ControllerSpecBase with MockitoSugar with SchemeDetailsStubData {

  val fakeSchemeDetailsConnector: SchemeDetailsConnector = mock[SchemeDetailsConnector]
  val fakeSchemeDetailsReadOnlyCacheConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]
  val fakeSchemeTransformer: SchemeDetailsMasterSection = mock[SchemeDetailsMasterSection]

  def featureSwitchManagementService(isVariationsEnabled: Boolean): FeatureSwitchManagementService = new FeatureSwitchManagementService {
    override def change(name: String, newValue: Boolean): Boolean = ???

    override def get(name: String): Boolean = isVariationsEnabled

    override def reset(name: String): Unit = ???
  }

  val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  val updateConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]

  val minimalPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = dontGetAnyData, isVariationsEnabled: Boolean): PSASchemeDetailsController =
    new PSASchemeDetailsController(frontendAppConfig,
      messagesApi,
      fakeSchemeDetailsConnector,
      fakeSchemeTransformer,
      FakeAuthAction,
      dataRetrievalAction,
      new ErrorHandler(frontendAppConfig, messagesApi),
      featureSwitchManagementService(isVariationsEnabled),
      fakeSchemeDetailsReadOnlyCacheConnector,
      lockConnector,
      updateConnector,
      minimalPsaConnector
    )

  val masterSections = Seq(individualMasterSection)
  val srn = "S1000000456"

  def viewAsString(): String =
    psa_scheme_details(
      frontendAppConfig, masterSections, psaSchemeDetailsSample.schemeDetails.name, srn
    )(fakeRequest, messages).toString()

  val individualDetails = IndividualDetails(firstName = "Aaa", middleName = None, lastName = "Bbb")
  val minimalPSA = MinimalPSA(email = "",
    isPsaSuspended = false,
    organisationName = Some("org"),
    individualDetails = Some(individualDetails)
  )

  def prepareLockAndCacheMocks(lock: Option[Lock] = None, viewJsValue: JsValue = JsNull,
                               updateJsValue: JsValue = JsNull, minimalPSA: MinimalPSA = minimalPSA): Unit = {
    when(lockConnector.isLockByPsaIdOrSchemeId(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(lock))
    when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(viewJsValue))
    when(updateConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(updateJsValue))
    when(minimalPsaConnector.getMinimalPsaDetails(Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(minimalPSA))
  }

  def verifyLockAndCacheMocks(lockTimes: Int = 0, viewTimes: Int = 0,
                              updateTimes: Int = 0, minimalPsaTimes: Int = 0,
                              expectedSavedJson: Option[JsValue] = None): Unit = {
    verify(lockConnector, times(lockTimes)).isLockByPsaIdOrSchemeId(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())

    verify(fakeSchemeDetailsReadOnlyCacheConnector, times(viewTimes)).upsert(Matchers.any(),
      expectedSavedJson match {
        case None => Matchers.any()
        case Some(jsonValue) =>
          Matchers.eq(jsonValue)
      })(Matchers.any(), Matchers.any())

    verify(updateConnector, times(updateTimes)).upsert(Matchers.any(),
      expectedSavedJson match {
        case None => Matchers.any()
        case Some(jsonValue) =>
          Matchers.eq(jsonValue)
      })(Matchers.any(), Matchers.any())

    verify(minimalPsaConnector, times(minimalPsaTimes)).getMinimalPsaDetails(Matchers.any())(Matchers.any(), Matchers.any())
  }

  def resetLockAndCacheMocks(): Unit = {
    reset(lockConnector)
    reset(fakeSchemeDetailsReadOnlyCacheConnector)
    reset(updateConnector)
    reset(minimalPsaConnector)
  }
}
