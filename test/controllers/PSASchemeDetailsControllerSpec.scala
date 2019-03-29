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
import connectors._
import controllers.actions.{DataRetrievalAction, _}
import handlers.ErrorHandler
import identifiers.PsaDetailsId
import models.details.transformation.{SchemeDetailsMasterSection, SchemeDetailsStubData}
import models.details.{Name, PsaDetails}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsNull, Json}
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

      val result = controller(isVariationsEnabled = false).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "return NOT_FOUND for a GET where logged in PSA is not administrator of scheme" in {
      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetails(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(psaSchemeDetailsSample))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)

      val result = controller(isVariationsEnabled = false).onPageLoad(srn)(fakeRequest)

      status(result) mustBe NOT_FOUND
      contentAsString(result).contains(messages("messages__pageNotFound404__heading")) mustBe true
    }

  }

  "SchemeDetailsController when isVariationsEnabled toggle switched on" must {

    "return OK and the correct view for a GET" in {

      val userAnswersResponse = UserAnswers(Json.obj(
        PsaDetailsId.toString -> Seq("A0000000")
      ))

      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(userAnswersResponse))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)
      when(fakeSchemeDetailsReadOnlyCacheConnector.upsert(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(JsNull))

      val result = controller(isVariationsEnabled = true).onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      verify(fakeSchemeDetailsReadOnlyCacheConnector, times(1)).upsert(any(), Matchers.eq(userAnswersResponse.json))(any(), any())

      contentAsString(result).contains(messages("messages__schemeTaskList__title")) mustBe true
    }

    "return NOT_FOUND for a GET where logged in PSA is not administrator of scheme" in {
      reset(fakeSchemeDetailsConnector)
      when(fakeSchemeDetailsConnector.getSchemeDetailsVariations(Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(UserAnswers(Json.obj(
          PsaDetailsId.toString -> Seq("A0000099")
        ))))
      when(fakeSchemeTransformer.transformMasterSection(Matchers.any())).thenReturn(masterSections)

      val result = controller(isVariationsEnabled = true).onPageLoad(srn)(fakeRequest)

      status(result) mustBe NOT_FOUND
      contentAsString(result).contains(messages("messages__pageNotFound404__heading")) mustBe true
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

  def controller(dataRetrievalAction: DataRetrievalAction = dontGetAnyData, isVariationsEnabled: Boolean): PSASchemeDetailsController =
    new PSASchemeDetailsController(frontendAppConfig,
      messagesApi,
      fakeSchemeDetailsConnector,
      fakeSchemeTransformer,
      FakeAuthAction,
      new ErrorHandler(frontendAppConfig, messagesApi),
      featureSwitchManagementService(isVariationsEnabled),
      fakeSchemeDetailsReadOnlyCacheConnector)

  val masterSections = Seq(individualMasterSection)
  val srn = "S1000000456"

  def viewAsString(): String =
    psa_scheme_details(
      frontendAppConfig, masterSections, psaSchemeDetailsSample.schemeDetails.name, srn
    )(fakeRequest, messages).toString()
}
