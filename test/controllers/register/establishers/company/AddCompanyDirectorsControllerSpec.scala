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

package controllers.register.establishers.company

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.company.AddCompanyDirectorsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.director.DirectorNameId
import identifiers.register.establishers.company.{AddCompanyDirectorsId, CompanyDetailsId}
import models.person.PersonName
import models.register.DirectorEntity
import models.{CompanyDetails, Index, NormalMode}
import navigators.Navigator
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._

import utils.{FakeNavigator, UserAnswers}
import views.html.register.establishers.company.addCompanyDirectors

class AddCompanyDirectorsControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddCompanyDirectorsFormProvider()
  private val form         = formProvider()
  private val postCall     = routes.AddCompanyDirectorsController.onSubmit _

  private def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  val firstIndex = Index(0)

  private val view = injector.instanceOf[addCompanyDirectors]

  private def controller(
      dataRetrievalAction: DataRetrievalAction,
      navigator: Navigator = fakeNavigator()
  ) =
    new AddCompanyDirectorsController(
      frontendAppConfig,
      messagesApi,
      navigator,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      view,
      controllerComponents
    )

  private def viewAsString(form: Form[_] = form, directors: Seq[DirectorEntity] = Nil, enableSubmission: Boolean = false) =
    view(
      form,
      directors,
      None,
      postCall(NormalMode, None, establisherIndex),
      false,
      NormalMode,
      None
    )(fakeRequest, messages).toString

  private val establisherIndex = 0
  private val companyName      = "MyCo Ltd"

  // scalastyle:off magic.number
  private val johnDoe   = PersonName("John", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")

  // scalastyle:on magic.number

  private val maxDirectors = frontendAppConfig.maxDirectors

  private def validData(directors: PersonName*) = {
    Json.obj(
      EstablishersId.toString -> Json.arr(
        Json.obj(
          CompanyDetailsId.toString -> CompanyDetails(companyName),
          "director"                -> directors.map(d => Json.obj(DirectorNameId.toString -> Json.toJson(d)))
        )
      )
    )
  }


  "AddCompanyDirectors Controller" must {

    "return OK and the correct view for a GET" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result          = controller(getRelevantData).onPageLoad(NormalMode, None, establisherIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      UserAnswers(validData(johnDoe))
        .set(AddCompanyDirectorsId(firstIndex))(true)
        .map { userAnswers =>
          val getRelevantData = new FakeDataRetrievalAction(Some(userAnswers.json))
          val result          = controller(getRelevantData).onPageLoad(NormalMode, None, establisherIndex)(fakeRequest)

          contentAsString(result) mustBe viewAsString(
            form,
            Seq(DirectorEntity(DirectorNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 1)),
            enableSubmission = true)
        }
    }

    "populate the view with directors when they exist" in {
      val directors = Seq(johnDoe, joeBloggs)
      val directorsViewModel = Seq(
        DirectorEntity(DirectorNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 2),
        DirectorEntity(DirectorNameId(0, 1), joeBloggs.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 3)
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(directors: _*)))
      val result          = controller(getRelevantData).onPageLoad(NormalMode, None, establisherIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form, directorsViewModel, enableSubmission = true)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, None, 0)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when no directors exist and the user submits" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData()))
      val result          = controller(getRelevantData).onSubmit(NormalMode, None, establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum directors exist and valid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoe)))
      val postRequest     = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result          = controller(getRelevantData).onSubmit(NormalMode, None, establisherIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum directors exist and invalid data is submitted" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(johnDoe)))
      val postRequest     = fakeRequest.withFormUrlEncodedBody(("value", "meh"))
      val boundForm       = form.bind(Map("value" -> "meh"))
      val result          = controller(getRelevantData).onSubmit(NormalMode, None, establisherIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(
        boundForm,
        Seq(DirectorEntity(DirectorNameId(0, 0), johnDoe.fullName, isDeleted = false, isCompleted = false, isNewEntity = false, 0)))
    }

    "redirect to the next page when maximum directors exist and the user submits" in {
      val directors       = Seq.fill(maxDirectors)(johnDoe)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData(directors: _*)))
      val result          = controller(getRelevantData).onSubmit(NormalMode, None, establisherIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result      = controller(dontGetAnyData).onSubmit(NormalMode, None, 0)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}
