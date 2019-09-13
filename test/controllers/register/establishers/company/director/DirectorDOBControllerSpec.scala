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

import controllers.ControllerSpecBase
import controllers.actions._
import forms.DOBFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import models.{CompanyDetails, Index, NormalMode}
import org.joda.time.LocalDate
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.{FakeNavigator, SectionComplete}
import viewmodels.Message
import views.html.register.DOB

import scala.concurrent.Future

//scalastyle:off magic.number

class DirectorDOBControllerSpec extends ControllerSpecBase {

  import DirectorDOBControllerSpec._

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompanyDirectorWithDirectorName): DirectorDOBController =
    new DirectorDOBController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider)

  private val postCall = routes.DirectorDOBController.onSubmit _

  def viewAsString(form: Form[_] = form): String = DOB(
    frontendAppConfig,
    form,
    NormalMode,
    None,
    postCall(NormalMode, firstEstablisherIndex, firstDirectorIndex, None),
    None,
    "first last",
    Message("messages__theDirector").resolve
  )(fakeRequest, messages).toString

  private val postRequest = fakeRequest
    .withFormUrlEncodedBody(("date.day", day.toString), ("date.month", month.toString), ("date.year", year.toString))

  "DirectorDOB Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller()
        .onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      val result = controller(getRelevantData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(new LocalDate(year, month, day)))
    }

    "redirect to the next page when valid data is submitted" in {

      when(mockUserAnswersService.save(any(), any(), any(), any())(any(), any(), any(), any())).thenReturn(Future.successful(validData))
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
  }
}

object DirectorDOBControllerSpec extends MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider: DOBFormProvider = new DOBFormProvider()
  val form: Form[LocalDate] = formProvider()

  val firstEstablisherIndex: Index = Index(0)
  val firstDirectorIndex: Index = Index(0)
  val invalidIndex: Index = Index(10)

  val companyName: String = "test company name"
  val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  val mockSectionComplete: SectionComplete = mock[SectionComplete]

  val day: Int = LocalDate.now().getDayOfMonth
  val month: Int = LocalDate.now().getMonthOfYear
  val year: Int = LocalDate.now().getYear - 20

  val validData = Json.obj(
    EstablishersId.toString -> Json.arr(
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(companyName),
        "director" -> Json.arr(
          Json.obj(
            "directorDetails" -> Json.obj(
              "firstName" -> "first",
              "lastName" -> "last"
            ),
            "dateOfBirth" -> s"$year-$month-$day"
          )
        )
      )
    )
  )
}


