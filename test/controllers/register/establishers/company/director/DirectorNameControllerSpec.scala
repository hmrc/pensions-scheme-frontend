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
import forms.register.PersonNameFormProvider
import identifiers.register.establishers.EstablishersId
import identifiers.register.establishers.company.CompanyDetailsId
import identifiers.register.establishers.company.director.{DirectorNameId, IsNewDirectorId}
import models.person.PersonName
import models.{CompanyDetails, EmptyOptionalSchemeReferenceNumber, Index, NormalMode}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.personName

import scala.concurrent.Future

//scalastyle:off magic.number

class DirectorNameControllerSpec extends ControllerSpecBase {

  import DirectorNameControllerSpec._

  private val viewmodel = CommonFormWithHintViewModel(
    routes.DirectorNameController.onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber),
    title = Message("messages__directorName__title"),
    heading = Message("messages__directorName__heading"))

  private val view = injector.instanceOf[personName]

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): DirectorNameController =
    new DirectorNameController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )


  def viewAsString(form: Form[_] = form): String = view(
    form,
    viewmodel,
    None)(fakeRequest, messages).toString

  private val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"))

  "DirectorName Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

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
                "directorDetails" ->
                  PersonName("First Name", "Last Name")
              )
            )
          )
        )
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(PersonName("First Name", "Last Name")))
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

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when no data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "returns a Bad Request and errors when invalid data is submitted" in {
      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails("test company name")
          )
        )
      )

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "01"), ("lastName", "?&^%$£"))

      val boundForm = form.bind(Map("firstName" -> "01", "lastName" -> "?&^%$£"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "returns a Bad Request and errors when max length has been exceeded" in {
      val validData = Json.obj(
        EstablishersId.toString -> Json.arr(
          Json.obj(
            CompanyDetailsId.toString ->
              CompanyDetails("test company name")
          )
        )
      )

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "tencharacter" * 3), ("lastName", "tencharacter" * 3))

      val boundForm = form.bind(Map("firstName" -> "tencharactertencharactertencharacter", "lastName" -> "tencharactertencharactertencharacter"))

      val result = controller().onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe BAD_REQUEST

      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val result = controller(dontGetAnyData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "save the isNewDirector flag when the new director is being added" in {
      reset(mockUserAnswersService)
      val validData = UserAnswers().set(CompanyDetailsId(firstEstablisherIndex))(CompanyDetails("test company name")).flatMap(
        _.set(DirectorNameId(firstEstablisherIndex, firstDirectorIndex))(
          PersonName("testFirstName", "testLastName")).flatMap(
          _.set(IsNewDirectorId(firstEstablisherIndex, firstDirectorIndex))(true)
        )
      ).asOpt.value.json

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val result = controller(getRelevantData).onSubmit(NormalMode, firstEstablisherIndex, firstDirectorIndex, EmptyOptionalSchemeReferenceNumber)(postRequest)
      status(result) mustBe SEE_OTHER
      verify(mockUserAnswersService, times(1)).upsert(eqTo(NormalMode), eqTo(EmptyOptionalSchemeReferenceNumber), eqTo(validData))(any(), any(), any())
    }
  }
}

object DirectorNameControllerSpec extends ControllerSpecBase with MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private val formProvider: PersonNameFormProvider = new PersonNameFormProvider()
  private val form: Form[PersonName] = formProvider("messages__error__director")

  private val firstEstablisherIndex: Index = Index(0)
  private val firstDirectorIndex: Index = Index(0)

  private val companyName: String = "test company name"
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
}
