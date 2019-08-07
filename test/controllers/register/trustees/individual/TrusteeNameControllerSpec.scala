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

package controllers.register.trustees.individual

import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.PersonNameFormProvider
import identifiers.register.trustees.individual.TrusteeNameId
import identifiers.register.trustees.{IsTrusteeNewId, TrusteesId}
import models.person.PersonName
import models.{Index, NormalMode}
import org.mockito.Matchers.any
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import services.UserAnswersService
import utils.{FakeNavigator, SectionComplete, UserAnswers}
import views.html.register.trustees.individual.trusteeName

import scala.concurrent.Future

class TrusteeNameControllerSpec extends ControllerSpecBase {

  import TrusteeNameControllerSpec._

  def controller(dataRetrievalAction: DataRetrievalAction = getMandatoryEstablisherCompany): TrusteeNameController =
    new TrusteeNameController(
      frontendAppConfig,
      messagesApi,
      mockUserAnswersService,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      FakeAllowAccessProvider(),
      new DataRequiredActionImpl,
      formProvider
    )

  private val postCall = routes.TrusteeNameController.onSubmit _

  def viewAsString(form: Form[_] = form): String = trusteeName(
    frontendAppConfig,
    form,
    NormalMode,
    firstTrusteeIndex,
    None,
    postCall(NormalMode, firstTrusteeIndex, None),
    None
  )(fakeRequest, messages).toString

  private val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "testFirstName"), ("lastName", "testLastName"))

  "TrusteeNameController" must {
    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, firstTrusteeIndex, None)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = UserAnswers().set(TrusteeNameId(0))(personName).asOpt.value.dataRetrievalAction

      val result = controller(getRelevantData).onPageLoad(NormalMode, firstTrusteeIndex, None)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(PersonName("John", "Doe")))
    }

    "redirect to the next page when valid data is submitted" in {
      val validData = Json.obj(
        TrusteesId.toString -> Json.arr(
          Json.obj("trustee" -> Json.obj(
            TrusteeNameId.toString -> personName
          ))
        )
      )

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val result = controller().onSubmit(NormalMode, firstTrusteeIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("firstName", "01"), ("lastName", "?&^%$£"))

      val result = controller().onSubmit(NormalMode, firstTrusteeIndex, None)(postRequest)

      status(result) mustBe BAD_REQUEST
    }

    "return a Bad Request and errors when no data is submitted" in {
      val result = controller().onSubmit(NormalMode, firstTrusteeIndex, None)(fakeRequest)

      status(result) mustBe BAD_REQUEST
    }

    "save the IsTrusteeNewId flag and when the new trustee is being added" in {
      reset(mockSectionComplete, mockUserAnswersService)

      val validData =
        UserAnswers().set(TrusteeNameId(firstTrusteeIndex))(PersonName("testFirstName", "testLastName")).flatMap(
          _.set(IsTrusteeNewId(firstTrusteeIndex))(true)).asOpt.value.json

      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(validData))

      val result = controller(getRelevantData).onSubmit(NormalMode, firstTrusteeIndex, None)(postRequest)

      status(result) mustBe SEE_OTHER

      verify(mockUserAnswersService, times(1)).upsert(eqTo(NormalMode), eqTo(None), eqTo(validData))(any(), any(), any())
    }
  }
}

object TrusteeNameControllerSpec extends ControllerSpecBase with MockitoSugar {
  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider: PersonNameFormProvider = new PersonNameFormProvider()
  private val form: Form[PersonName] = formProvider()

  private val firstTrusteeIndex: Index = Index(0)
  private val personName = PersonName("John", "Doe")
  private val mockUserAnswersService: UserAnswersService = mock[UserAnswersService]
  private val mockSectionComplete: SectionComplete = mock[SectionComplete]
}
