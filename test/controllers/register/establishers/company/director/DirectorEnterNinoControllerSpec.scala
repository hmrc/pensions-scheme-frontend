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

package controllers.register.establishers.company.director

import controllers.ControllerSpecBase
import controllers.actions._
import forms.NINOFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorEnterNINOId, DirectorNameId}
import models._
import models.person.PersonName
import play.api.data.Form
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.FakeUserAnswersService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{Message, NinoViewModel}
import views.html.nino

//scalastyle:off magic.number

class DirectorEnterNinoControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val companyName = "test company name"
  val formProvider = new NINOFormProvider()
  val form = formProvider("First Name Last Name")
  val establisherIndex = Index(0)
  val directorIndex = Index(0)
  val invalidIndex = Index(11)
  val directorName = "First Name Last Name"

  val validData: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString ->
              PersonName("First Name", "Last Name"),
              DirectorEnterNINOId.toString -> Json.obj( "value" -> "CS700100A")
          )
        )
      )
    )
  )

  val validDataNoPreviousAnswer: JsObject = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString ->
              PersonName("First Name", "Last Name")
          )
        )
      )
    )
  )

  private val view = injector.instanceOf[nino]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisher): DirectorEnterNINOController =
    new DirectorEnterNINOController(frontendAppConfig, messagesApi, FakeUserAnswersService, new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction, dataRetrievalAction, FakeAllowAccessProvider(), new DataRequiredActionImpl, formProvider, stubMessagesControllerComponents(), view)

  def viewAsString(form: Form[_] = form): String = {
    val viewmodel = NinoViewModel(
      postCall = controllers.register.establishers.company.director.routes.DirectorEnterNINOController.onSubmit(NormalMode, establisherIndex, directorIndex, None),
      title = Message("messages__enterNINO", Message("messages__theDirector")),
      heading = Message("messages__enterNINO", directorName),
      hint = Message("messages__common__nino_hint"),
      srn = None
    )

    view(form, viewmodel, None)(fakeRequest, messages).toString
  }

  "DirectorNino Controller" must {

    "return OK and the correct view for a GET when establisher name is present" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))

      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to session expired from a GET when the establisher  index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, invalidIndex, establisherIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
    "redirect to session expired from a GET when the director index is invalid" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, invalidIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form.fill(ReferenceValue("CS700100A")))
    }

    "redirect to the next page when valid data is submitted" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "CS700100A"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validDataNoPreviousAnswer))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "invalid value"))
      val boundForm = form.bind(Map("nino" -> "invalid value"))
      val result = controller(getRelevantData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no existing director details data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, establisherIndex, directorIndex, None)(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "CS700100A"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing director details data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("nino", "CS700100A"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, establisherIndex, directorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
