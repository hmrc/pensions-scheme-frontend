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

package controllers.register.establishers.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.NinoNewFormProvider
import identifiers.SchemeNameId
import identifiers.register.establishers.individual.{EstablisherNameId, EstablisherNewNinoId}
import models._
import models.person.PersonName
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import viewmodels.NinoViewModel
import views.html.nino

class EstablisherEnterNinoControllerSpec extends ControllerSpecBase {

  import EstablisherEnterNinoControllerSpec._

  "EstablisherNinoNew Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val result = controller().onPageLoad(UpdateMode, firstIndex, srn)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, UpdateMode, firstIndex, srn)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(alreadySubmittedData))
      val result          = controller(getRelevantData).onPageLoad(UpdateMode, firstIndex, srn)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(ReferenceValue("CS700100A")), UpdateMode, firstIndex, srn)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("nino" -> "CS700100A")
      val result      = controller().onSubmit(UpdateMode, firstIndex, srn)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("nino" -> "invalid value")
      val boundForm   = form.bind(Map("nino" -> "invalid value"))
      val result      = controller().onSubmit(UpdateMode, firstIndex, srn)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, UpdateMode, firstIndex, srn)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", Nino.options.head.value))
      val result      = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex, None)(postRequest)
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
}

object EstablisherEnterNinoControllerSpec extends ControllerSpecBase {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val establisherName = "test first name test last name"

  private val srn                       = Some("srn")
  private val schemeName                = "Test Scheme Name"
  private val formProvider              = new NinoNewFormProvider()
  private val form                      = formProvider(establisherName)
  private val firstIndex                = Index(0)
  private val establisherIndividualName = PersonName("test first name", "test last name", false)

  private val alreadySubmittedData: JsObject = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        EstablisherNameId.toString -> establisherIndividualName,
        EstablisherNewNinoId.toString -> Json.obj(
          "value" -> "CS700100A"
        )
      )
    ),
    SchemeNameId.toString -> schemeName
  )

  private val basicData: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(
      Json.obj(
        "establishers" -> Json.arr(
          Json.obj(
            EstablisherNameId.toString -> establisherIndividualName
          )
        ),
        SchemeNameId.toString -> schemeName
      )))

  private def controller(dataRetrievalAction: DataRetrievalAction = basicData): EstablisherEnterNinoController =
    new EstablisherEnterNinoController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewAsString(form: Form[_], mode: Mode, index: Index, srn: Option[String]): String = {
    val vm = NinoViewModel(
      postCall = controllers.register.establishers.individual.routes.EstablisherEnterNinoController.onSubmit(mode, index, srn),
      title = messages("messages__common_nino__title"),
      heading = messages("messages__common_nino__h1", establisherName),
      hint = messages("messages__common__nino_hint"),
      srn = srn
    )

    nino(frontendAppConfig, form, vm, Some(schemeName))(fakeRequest, messages).toString
  }
}
