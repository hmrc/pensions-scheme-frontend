/*
 * Copyright 2018 HM Revenue & Customs
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

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.establishers.company.director.DirectorDetailsFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import models.register.establishers.company.director.DirectorDetails
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, FakeSectionComplete}
import views.html.register.establishers.company.director.directorDetails

//scalastyle:off magic.number

class DirectorDetailsControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DirectorDetailsFormProvider()
  val form = formProvider()

  val firstEstablisherIndex = Index(0)
  val firstDirectorIndex=Index(0)
  val invalidIndex=Index(10)

  val companyName ="test company name"

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): DirectorDetailsController =
    new DirectorDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      FakeSectionComplete)

  def viewAsString(form: Form[_] = form): String = directorDetails(
    frontendAppConfig,
    form,
    NormalMode,
    firstEstablisherIndex,
    firstDirectorIndex,
    companyName)(fakeRequest, messages).toString

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear - 20

  "DirectorDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstEstablisherIndex,firstDirectorIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(companyName, Some("123456"), Some("abcd")),
            "director" -> Json.arr(
              Json.obj(
                DirectorDetailsId.toString ->
                  DirectorDetails("First Name",Some("Middle Name"), "Last Name", new LocalDate(year, month, day))
              )
            )
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstEstablisherIndex,firstDirectorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(DirectorDetails("First Name",Some("Middle Name"), "Last Name", new LocalDate(year, month, day))))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"),
        ("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex,firstDirectorIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex,firstDirectorIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstEstablisherIndex,firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to session expired from a GET when the index is invalid for establisher" ignore {

      val result = controller().onPageLoad(NormalMode, invalidIndex,firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a GET when the index is invalid for director" ignore {

      val result = controller().onPageLoad(NormalMode,firstEstablisherIndex ,invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the index is invalid for establisher" ignore {

      val result = controller().onSubmit(NormalMode, invalidIndex,firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the index is invalid for director" ignore {

      val result = controller().onSubmit(NormalMode,firstEstablisherIndex ,invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }


    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"),
        ("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstEstablisherIndex,firstDirectorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
