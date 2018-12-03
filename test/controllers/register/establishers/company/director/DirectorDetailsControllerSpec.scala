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

import connectors.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.PersonDetailsFormProvider
import identifiers.register.SchemeDetailsId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.DirectorDetailsId
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import models.person.PersonDetails
import models.register.{SchemeDetails, SchemeType}
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, SectionComplete, UserAnswers}
import views.html.register.establishers.company.director.directorDetails

import scala.concurrent.Future

//scalastyle:off magic.number

class DirectorDetailsControllerSpec extends ControllerSpecBase {

  import DirectorDetailsControllerSpec._

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): DirectorDetailsController =
    new DirectorDetailsController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      mockSectionComplete)

  def viewAsString(form: Form[_] = form): String = directorDetails(
    frontendAppConfig,
    form,
    NormalMode,
    firstEstablisherIndex,
    firstDirectorIndex,
    companyName)(fakeRequest, messages).toString

  private val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"),
    ("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))

  "DirectorDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex)(fakeRequest)

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
                  PersonDetails("First Name", Some("Middle Name"), "Last Name", new LocalDate(year, month, day))
              )
            )
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(PersonDetails("First Name", Some("Middle Name"), "Last Name", new LocalDate(year, month, day))))
    }

    "redirect to the next page when valid data is submitted" in {
      val validData = Json.obj(
        SchemeDetailsId.toString ->
          SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails("test company name", Some("123456"), Some("abcd"))
          )
        )
      )

      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a GET when the index is invalid for establisher" in {
      val result = controller().onPageLoad(NormalMode, invalidIndex, firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the index is invalid for establisher" in {
      val result = controller().onSubmit(NormalMode, invalidIndex, firstDirectorIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a GET when the index is invalid for director" ignore {
      val result = controller().onPageLoad(NormalMode, firstEstablisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to session expired from a POST when the index is invalid for director" ignore {
      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, invalidIndex)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "set the establisher as not complete when the new director is being added" in {
      reset(mockSectionComplete)
      val validData =
        Json.obj(
          SchemeDetailsId.toString ->
            SchemeDetails("Test Scheme Name", SchemeType.SingleTrust),
          EstablishersId.toString -> Json.arr(
            Json.obj(
              CompanyDetailsId.toString ->
                CompanyDetails("test company name", Some("123456"), Some("abcd")),
              "director" -> Json.arr(
                Json.obj(
                  DirectorDetailsId.toString ->
                    PersonDetails("First Name", Some("Middle Name"), "Last Name", new LocalDate(year, month, day))
                )
              )
            )
          )
        )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val userAnswers = UserAnswers(validData)
      when(mockUserAnswersCacheConnector.save(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
      when(mockSectionComplete.setCompleteFlag(any(), eqTo(IsEstablisherCompleteId(0)),
        eqTo(userAnswers), eqTo(false))(any(), any())).thenReturn(Future.successful(userAnswers))

      val result = controller(getRelevantData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex)(postRequest)
      status(result) mustBe SEE_OTHER
      verify(mockSectionComplete, times(1)).setCompleteFlag(any(), eqTo(IsEstablisherCompleteId(0)), eqTo(userAnswers), eqTo(false))(any(), any())
    }
  }
}

object DirectorDetailsControllerSpec extends MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider: PersonDetailsFormProvider = new PersonDetailsFormProvider()
  val form: Form[PersonDetails] = formProvider()

  val firstEstablisherIndex: Index = Index(0)
  val firstDirectorIndex: Index = Index(0)
  val invalidIndex: Index = Index(10)

  val companyName: String = "test company name"
  val mockUserAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  val mockSectionComplete: SectionComplete = mock[SectionComplete]

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear - 20
}
