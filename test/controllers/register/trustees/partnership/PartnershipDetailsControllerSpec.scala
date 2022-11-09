/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.trustees.partnership

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.PartnershipDetailsFormProvider
import identifiers.register.trustees.TrusteesId
import identifiers.register.trustees.partnership.PartnershipDetailsId
import models.FeatureToggleName.SchemeRegistration
import models.{FeatureToggle, Index, NormalMode, PartnershipDetails}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{FakeUserAnswersService, FeatureToggleService}
import utils.FakeNavigator
import views.html.register.trustees.partnership.partnershipDetails

import scala.concurrent.Future

class PartnershipDetailsControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar {
  appRunning()

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider = new PartnershipDetailsFormProvider()
  private val form: Form[PartnershipDetails] = formProvider()
  private val firstIndex: Index = Index(0)
  val invalidIndex: Index = Index(3)
  val schemeName = "Test Scheme Name"
  private val mockFeatureToggleService = mock[FeatureToggleService]

  private val view = injector.instanceOf[partnershipDetails]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData): PartnershipDetailsController =
    new PartnershipDetailsController(frontendAppConfig, messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider,
      controllerComponents,
      view,
      mockFeatureToggleService
    )

  val submitUrl: Call = controllers.register.trustees.partnership.routes.PartnershipDetailsController.onSubmit(NormalMode, firstIndex, None)

  def viewAsString(form: Form[_] = form): String = view(
    form,
    NormalMode,
    firstIndex,
    None,
    submitUrl,
    None
  )(fakeRequest, messages).toString

  val validData: JsObject = Json.obj(
    TrusteesId.toString -> Json.arr(
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

  "TrusteeDetails Controller" must {

    "return OK and the correct view for a GET when scheme name is present" in {
      val result = controller().onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(PartnershipDetails("test partnership name")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("partnershipName", "test partnership name"))

      val result = controller().onSubmit(NormalMode, firstIndex, None)(postRequest)

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
