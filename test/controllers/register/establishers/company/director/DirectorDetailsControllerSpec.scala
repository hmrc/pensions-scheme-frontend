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

package controllers.register.establishers.company.director

import services.UserAnswersService
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.PersonDetailsFormProvider
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorDetailsId, IsDirectorCompleteId, IsNewDirectorId}
import identifiers.register.establishers.{EstablishersId, IsEstablisherCompleteId}
import models.person.PersonDetails
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
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      mockSectionComplete)

  private val postCall = routes.DirectorDetailsController.onSubmit _

  def viewAsString(form: Form[_] = form): String = directorDetails(
    frontendAppConfig,
    form,
    NormalMode,
    firstEstablisherIndex,
    firstDirectorIndex,
    None,
    postCall(NormalMode, firstEstablisherIndex, firstDirectorIndex, None))(fakeRequest, messages).toString

  private val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"),
    ("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))

  "DirectorDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(companyName),
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

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(PersonDetails("First Name", Some("Middle Name"), "Last Name", new LocalDate(year, month, day))))
    }

    "redirect to the next page when valid data is submitted" in {
      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails("test company name")
          )
        )
      )

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "save the isNewDirector flag and set the establisher as not complete when the new director is being added" in {
      reset(mockSectionComplete, mockUserAnswersService)
      val validData = UserAnswers().set(CompanyDetailsId(firstEstablisherIndex))(CompanyDetails("test company name")).flatMap(
        _.set(DirectorDetailsId(firstEstablisherIndex, firstDirectorIndex))(
          PersonDetails("testFirstName", None, "testLastName", new LocalDate(year, month, day))).flatMap(
          _.set(IsNewDirectorId(firstEstablisherIndex, firstDirectorIndex))(true)
        )
      ).asOpt.value.json

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val userAnswers = UserAnswers(validData)
      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))
      when(mockUserAnswersService.setCompleteFlag(any(), any(), eqTo(IsEstablisherCompleteId(0)),
        eqTo(userAnswers), eqTo(false))(any(), any(), any(), any())).thenReturn(Future.successful(userAnswers))

      val result = controller(getRelevantData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(postRequest)
      status(result) mustBe SEE_OTHER
      verify(mockUserAnswersService, times(1)).upsert(any(), any(), eqTo(validData))(any(), any(), any())
      verify(mockUserAnswersService, times(1))
        .setCompleteFlag(any(), any(), eqTo(IsEstablisherCompleteId(0)), eqTo(userAnswers), eqTo(false))(any(), any(), any(), any())
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
  val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  val mockSectionComplete: SectionComplete = mock[SectionComplete]

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear - 20
}
