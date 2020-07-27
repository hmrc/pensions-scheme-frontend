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

import connectors.{PensionSchemeVarianceLockConnector, PensionsSchemeConnector, SchemeDetailsReadOnlyCacheConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import identifiers.{PstrId, SchemeNameId}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Call
import play.api.mvc.Results.Ok
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import views.html.variationDeclaration

import scala.concurrent.Future

class VariationDeclarationControllerSpec extends ControllerSpecBase with MockitoSugar {
  val schemeName = "Test Scheme Name"
  val srnNumber = "S12345"
  val srn: Option[String] = Some(srnNumber)
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  def postCall: Call = routes.VariationDeclarationController.onClickAgree(srn)

  def validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj(
    SchemeNameId.toString -> schemeName,
    PstrId.toString -> "pstr")))

  val pensionsSchemeConnector: PensionsSchemeConnector = mock[PensionsSchemeConnector]
  val lockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  val updateSchemeCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  val viewConnector: SchemeDetailsReadOnlyCacheConnector = mock[SchemeDetailsReadOnlyCacheConnector]

  def controller(dataRetrievalAction: DataRetrievalAction = validData): VariationDeclarationController =
    new VariationDeclarationController(messagesApi, new FakeNavigator(onwardRoute), FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl,
      pensionsSchemeConnector, lockConnector, updateSchemeCacheConnector, viewConnector,
      stubMessagesControllerComponents(),
      view
    )

  private val view = injector.instanceOf[variationDeclaration]
  private def viewAsString(): String = view(Some(schemeName), srn, postCall)(fakeRequest, messages).toString

  "VariationDeclarationController" must {

    "return OK and the correct view for a GET when update cache has srn" in {

      when(updateSchemeCacheConnector.fetch(Matchers.eq(srnNumber))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Some(JsNull)))

      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to tasklist and the correct view for a GET when update cache does not have srn" in {

      when(updateSchemeCacheConnector.fetch(Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
    }

    "redirect to the next page on clicking agree and continue" in {
      when(pensionsSchemeConnector.updateSchemeDetails(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(updateSchemeCacheConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(viewConnector.removeAll(any())(any(), any()))
        .thenReturn(Future.successful(Ok))
      when(lockConnector.releaseLock(any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      val result = controller().onClickAgree(srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}




