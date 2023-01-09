/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register.establishers.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.FeatureToggleName.SchemeRegistration
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, FeatureToggleService}
import utils.FakeNavigator
import views.html.register.establishers.partnership.partnershipDetails

import scala.concurrent.Future

class PartnershipDetailsControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar{

  def onwardRoute: Call = controllers.register.establishers.routes.PsaSchemeTaskListRegistrationEstablisherController.onPageLoad(firstIndex)
  def onwardRouteToggleOff: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new PartnershipDetailsFormProvider()
  private val form = formProvider()
  private val firstIndex = Index(0)
  private val postCall = routes.PartnershipDetailsController.onSubmit _
  private def navigator = new FakeNavigator(desiredRoute = onwardRoute)
  private def oldNavigator = new FakeNavigator(desiredRoute = onwardRouteToggleOff)

  private val view = injector.instanceOf[partnershipDetails]
  private val mockFeatureToggleService = mock[FeatureToggleService]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): PartnershipDetailsController =
    new PartnershipDetailsController(frontendAppConfig, messagesApi, FakeUserAnswersService, navigator, oldNavigator,
      FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider, controllerComponents, view, mockFeatureToggleService)

  def viewAsString(form: Form[_] = form): String = view(form, NormalMode, firstIndex, None,
    postCall(NormalMode, 0, None), None)(fakeRequest, messages).toString

  private val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString ->
          PartnershipDetails("test partnership name")
      )
    )
  )

  override protected def beforeEach(): Unit = {
    reset(mockFeatureToggleService)
    when(mockFeatureToggleService.get(any())(any(), any()))
      .thenReturn(Future.successful(FeatureToggle(SchemeRegistration, true)))
  }

  "PartnershipDetails Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode,firstIndex, None)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(PartnershipDetails("test partnership name")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("partnershipName", "test partnership name"), ("vatNumber", "GB123456789"), ("payeNumber", "1234567824"))
      val result = controller().onSubmit(NormalMode,firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))
      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
