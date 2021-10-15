/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.NINOFormProvider
import identifiers.SchemeNameId
import identifiers.register.trustees.individual.{TrusteeEnterNINOId, TrusteeNameId}
import models._
import models.person.PersonName
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService

import utils.{FakeNavigator, UserAnswers}
import viewmodels.{Message, NinoViewModel}
import views.html.nino

class TrusteeEnterNINOControllerSpec extends ControllerSpecBase {

  import TrusteeEnterNINOControllerSpec._

  "TrusteeNino Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val result = controller().onPageLoad(UpdateMode, index, srn)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, UpdateMode, index, srn)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = UserAnswers()
        .set(TrusteeNameId(0))(PersonName("Test", "Name"))
        .flatMap(_.set(TrusteeEnterNINOId(0))(ReferenceValue(ninoData)))
        .flatMap(_.set(SchemeNameId)(schemeName))
        .asOpt
        .value
        .dataRetrievalAction
      val result = controller(getRelevantData).onPageLoad(UpdateMode, index, srn)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(ReferenceValue("CS700100A")), UpdateMode, index, srn)
    }
  }

  "redirect to the next page when valid data is submitted" in {
    val postRequest = fakeRequest.withFormUrlEncodedBody("nino" -> "CS700100A")
    val result      = controller().onSubmit(UpdateMode, index, srn)(postRequest)
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(onwardRoute.url)
  }

  "return a Bad Request and errors when invalid data is submitted" in {
    val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "invalid value")
    val boundForm   = form.bind(Map("value" -> "invalid value"))
    val result      = controller().onSubmit(UpdateMode, index, srn)(postRequest)
    status(result) mustBe BAD_REQUEST
    contentAsString(result) mustBe viewAsString(boundForm, UpdateMode, index, srn)
  }

  "redirect to Session Expired for a GET if no existing data is found" in {
    val result = controller(dontGetAnyData).onPageLoad(NormalMode, index, None)(fakeRequest)
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
  }

  "redirect to Session Expired for a POST if no existing data is found" in {
    val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "CS700100A"))
    val result      = controller(dontGetAnyData).onSubmit(NormalMode, index, None)(postRequest)
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
  }

  "redirect to Session Expired page when the index is not valid" in {
    val getRelevantData = new FakeDataRetrievalAction(Some(alreadySubmittedData))
    val result          = controller(getRelevantData).onPageLoad(NormalMode, Index(2), None)(fakeRequest)
    status(result) mustBe SEE_OTHER
    redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
  }
}

object TrusteeEnterNINOControllerSpec extends ControllerSpecBase {
  val formProvider       = new NINOFormProvider()
  private val srn        = Some("srn")
  val form: Form[ReferenceValue] = formProvider("First Name Last Name")
  private val index      = Index(0)
  private val ninoData   = "CS700100A"
  val trusteeFullName    = "Test Name"
  private val schemeName = "pension scheme details"

  private val alreadySubmittedData: JsObject = Json.obj(
    "trustees" -> Json.arr(
      Json.obj(
        TrusteeNameId.toString -> PersonName("Test", "Name"),
        TrusteeEnterNINOId.toString -> Json.obj(
          "value" -> ninoData
        )
      )
    ),
    SchemeNameId.toString -> schemeName
  )

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val view = injector.instanceOf[nino]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryTrustee): TrusteeEnterNINOController =
    new TrusteeEnterNINOController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_], mode: Mode, index: Index, srn: Option[String], trusteeName: String =trusteeFullName): String = {

    val vm = NinoViewModel(
      postCall = controllers.register.trustees.individual.routes.TrusteeEnterNINOController.onSubmit(mode, index, srn),
      title = Message("messages__enterNINO", Message("messages__theIndividual").resolve),
      heading = Message("messages__enterNINO", trusteeName),
      hint = "messages__common__nino_hint",
      srn = srn
    )

    view(form, vm, Some(schemeName))(fakeRequest, messages).toString
  }
}
