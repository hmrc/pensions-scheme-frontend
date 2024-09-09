/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.register.establishers.company.director.ConfirmDeleteDirectorFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorNameId
import models.person.PersonName
import models.{CompanyDetails, Index, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FakeUserAnswersService
import utils.FakeNavigator
import views.html.register.establishers.company.director.confirmDeleteDirector

class ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  import ConfirmDeleteDirectorControllerSpec._

  "ConfirmDeleteDirector Controller" must {
    "return OK and the correct view for a GET" in {
      val data   = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onPageLoad(establisherIndex, directorIndex, NormalMode, srn)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to already deleted view for a GET if the director was already deleted" in {
      val data   = new FakeDataRetrievalAction(Some(testData(directorDeleted)))
      val result = controller(data).onPageLoad(establisherIndex, directorIndex, NormalMode, srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AlreadyDeletedController.onPageLoad(establisherIndex, directorIndex, srn).url)
    }

    "return a Bad Request on POST and errors when invalid data is submitted" in {
      val data        = new FakeDataRetrievalAction(Some(testData()))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, srn)(postRequest)
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "delete the director on a POST" in {
      val data   = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, srn)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verify(DirectorNameId(establisherIndex, directorIndex), directorDetails.copy(isDeleted = true))
    }

    "dont delete the director on a POST if No selected" in {
      FakeUserAnswersService.reset()
      val data   = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, srn)(postRequestForCancel)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(DirectorNameId(establisherIndex, directorIndex))
    }

    "never delete the director on a POST if selected No in UpdateMode" in {
      FakeUserAnswersService.reset()
      val data   = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, UpdateMode, srn)(postRequestForCancel)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersService.verifyNot(DirectorNameId(establisherIndex, directorIndex))
    }

    "redirect to the next page on a successful POST" in {
      val data   = new FakeDataRetrievalAction(Some(testData()))
      val result = controller(data).onSubmit(establisherIndex, directorIndex, NormalMode, srn)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(establisherIndex, directorIndex, NormalMode, srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(establisherIndex, directorIndex, NormalMode, srn)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}

object ConfirmDeleteDirectorControllerSpec extends ControllerSpecBase {

  private val establisherIndex = Index(0)
  private val directorIndex    = Index(0)
  private val companyName      = "MyCo Ltd"
  private val directorName     = "John Doe"
  private lazy val postCall    = routes.ConfirmDeleteDirectorController.onSubmit(establisherIndex, directorIndex, NormalMode, srn)
  private val directorDetails  = PersonName("John", "Doe")
  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))
  private val postRequestForCancel: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "false"))

  private def testData(directors: PersonName = directorDetails) = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        "director" -> Json.arr(
          Json.obj(
            DirectorNameId.toString ->
              directors
          )
        )
      )
    )
  )

  val directorDeleted: PersonName = directorDetails.copy(isDeleted = true)

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new ConfirmDeleteDirectorFormProvider()
  private val form         = formProvider.apply(directorName)

  private val view = injector.instanceOf[confirmDeleteDirector]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new ConfirmDeleteDirectorController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(srn),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      directorName,
      postCall,None,
      srn
    )(fakeRequest, messages).toString

}
