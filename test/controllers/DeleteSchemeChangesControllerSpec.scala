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

import connectors.{MinimalPsaConnector, PensionSchemeVarianceLockConnector, UpdateSchemeCacheConnector}
import controllers.actions._
import forms.DeleteSchemeChangesFormProvider
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.mvc.Results.Ok
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import views.html.deleteSchemeChanges

import scala.concurrent.Future

class DeleteSchemeChangesControllerSpec extends ControllerSpecBase with MockitoSugar with BeforeAndAfterEach{

  val formProvider = new DeleteSchemeChangesFormProvider()
  val form: Form[Boolean] = formProvider()
  val srn = "S123"
  val schemeName = "Test Scheme Name"
  val psaName = "Test Psa Name"
  val fakeCacheConnector: UpdateSchemeCacheConnector = mock[UpdateSchemeCacheConnector]
  val fakeLockConnector: PensionSchemeVarianceLockConnector = mock[PensionSchemeVarianceLockConnector]
  val fakeMinPsaConnector: MinimalPsaConnector = mock[MinimalPsaConnector]
  def postCall: Call = controllers.routes.DeleteSchemeChangesController.onSubmit(srn)

  val view: deleteSchemeChanges = app.injector.instanceOf[deleteSchemeChanges]

  def controller(): DeleteSchemeChangesController =
    new DeleteSchemeChangesController(
      frontendAppConfig,
      messagesApi,
      fakeCacheConnector,
      fakeLockConnector,
      fakeMinPsaConnector,
      FakeAuthAction,
      getEmptyData,
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString(form: Form[_] = form): String = view(form, schemeName, postCall, psaName)(fakeRequest, messages).toString

  override def beforeEach(): Unit = {
    reset(fakeCacheConnector, fakeMinPsaConnector)
    when(fakeCacheConnector.fetch(eqTo(srn))(any(), any())).thenReturn(Future.successful(Some(Json.obj(
      "schemeName" -> schemeName))))
    when(fakeMinPsaConnector.getPsaNameFromPsaID(any())(any(), any())).thenReturn(Future.successful(Some(psaName)))
    super.beforeEach()
  }

  "DeleteScheme Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "remove all is called to delete user answers when user answers Yes" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      when(fakeCacheConnector.removeAll(any())(any(), any())).thenReturn(Future.successful(Ok))
      when(fakeLockConnector.releaseLock(any(), any())(any(), any())).thenReturn(Future.successful((): Unit))

      val result = controller().onSubmit(srn)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.managePensionsSchemeOverviewUrl.url)
      verify(fakeCacheConnector, times(1)).removeAll(any())(any(), any())
      verify(fakeLockConnector, times(1)).releaseLock(any(),any())(any(), any())
    }

    "redirect to the overview page when user answers No" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller().onSubmit(srn)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.managePensionsSchemeOverviewUrl.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(srn)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }
  }
}
