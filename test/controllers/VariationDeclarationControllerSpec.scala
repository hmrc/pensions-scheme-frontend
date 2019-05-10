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

import connectors.{PensionSchemeVarianceLockConnector, PensionsSchemeConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.{PstrId, SchemeNameId}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncWordSpec, MustMatchers}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.variationDeclaration

import scala.concurrent.Future

class VariationDeclarationControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  val schemeName = "Test Scheme Name"
  val srnNumber = "S12345"
  val srn = Some("S12345")
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  def postCall: Call = routes.VariationDeclarationController.onSubmit(srn)

  def validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
    SchemeNameId.toString -> schemeName,
    PstrId.toString -> "pstr")))

  val pensionsSchemeConnector: PensionsSchemeConnector = mock[PensionsSchemeConnector]
  val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  val updateSchemeCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = validData): VariationDeclarationController =
    new VariationDeclarationController(frontendAppConfig, messagesApi, new FakeNavigator(onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider, pensionsSchemeConnector, lockConnector, updateSchemeCacheConnector)

  private def viewAsString() = variationDeclaration(frontendAppConfig, form, Some(schemeName), postCall, srn)(fakeRequest, messages).toString

  "VariationDeclarationController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

      "redirect to the next page for a POST" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("agree", "agreed"))
        when(pensionsSchemeConnector.updateSchemeDetails(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful((): Unit))
        when(updateSchemeCacheConnector.removeAll(any())(any(), any()))
          .thenReturn(Future.successful(Ok))
        when(lockConnector.releaseLock(any(), any())(any(), any()))
          .thenReturn(Future.successful((): Unit))

        val result = controller().onSubmit(srn)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}




