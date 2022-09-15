
/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.dataPrefill.DataPrefillRadioFormProvider
import identifiers.SchemeNameId
import identifiers.register.establishers.company.CompanyDetailsId
import models.prefill.{IndividualDetails => DataPrefillIndividualDetails}
import models.{CompanyDetails, DataPrefillRadio}
import navigators.{EstablishersCompanyDirectorNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.never
import org.mockito.MockitoSugar.{atLeastOnce, mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.{DataPrefillService, UserAnswersService}
import utils.UserAnswers
import views.html.dataPrefillRadio

import scala.concurrent.Future

class TrusteesAlsoDirectorsControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {
  private val onwardRoute: Call = Call("GET", "/dummy")
  private val establisherIndex = 0
  private val companyDetails = CompanyDetails(companyName = "Wibble Inc")
  private val schemeName = "aa"

  private val data =
    Some(
      UserAnswers(Json.obj())
        .setOrException(CompanyDetailsId(establisherIndex))(companyDetails)
        .setOrException(SchemeNameId)(schemeName)
        .json
    )

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  private val mockDataPrefillService = mock[DataPrefillService]
  private val mockNavigator = mock[EstablishersCompanyDirectorNavigator]
  private val mockUserAnswersService = mock[UserAnswersService]

  private val pageHeading = Messages("messages__directors__prefill__title")
  private val titleMessage = Messages("messages__directors__prefill__heading", companyDetails.companyName)
  private val postCall = controllers.register.establishers.company.director.routes.TrusteesAlsoDirectorsController.onSubmit(establisherIndex)

  private val seqTrustee = Seq(
    DataPrefillIndividualDetails(
      firstName = "John", lastName = "Smith", index = 3, isDeleted = false, nino = None, dob = None, isComplete = true
    ),
    DataPrefillIndividualDetails(
      firstName = "Jane", lastName = "Anderson", index = 4, isDeleted = false, nino = None, dob = None, isComplete = true
    )
  )

  private val extraModules: Seq[GuiceableModule] = Seq(
    bind[DataPrefillService].toInstance(mockDataPrefillService),
    bind[Navigator].toInstance(mockNavigator),
    bind[EstablishersCompanyDirectorNavigator].toInstance(mockNavigator),
    bind[UserAnswersService].toInstance(mockUserAnswersService)
  )

  override def beforeEach: Unit = {
    reset(mockDataPrefillService, mockUserAnswersService, mockNavigator)

    when(mockDataPrefillService.getListOfTrusteesToBeCopied(any())(any()))
      .thenReturn(seqTrustee)

    when(mockNavigator.nextPage(any(), any(), any(), any())(any(), any(), any())).thenReturn(onwardRoute)

  }

  "onPageLoad" must {
    "return Ok and the correct view on a GET request" in {
      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules: _*)) { app =>
        val controller = app.injector.instanceOf[TrusteesAlsoDirectorsController]
        val view = app.injector.instanceOf[dataPrefillRadio]
        val result = controller.onPageLoad(establisherIndex = 0)(fakeRequest)

        status(result) mustBe OK
        val form = new DataPrefillRadioFormProvider().apply(
          requiredError = ""
        )

        contentAsString(result) mustBe
          view(form, Some(schemeName), pageHeading, titleMessage, DataPrefillRadio.radios(seqTrustee), postCall)(fakeRequest, messages).toString
      }
    }
  }

  "onSubmit" must {
    "behave correctly when item other than None is chosen" in {
      val emptyUA = UserAnswers()

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copyAllTrusteesToDirectors(any(), ArgumentMatchers.eq(Seq(1)), any())).thenReturn(emptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules: _*)) { app =>
        val controller = app.injector.instanceOf[TrusteesAlsoDirectorsController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value" -> "1"
        )
        val result = controller.onSubmit(establisherIndex = 0)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        verify(mockDataPrefillService, atLeastOnce).copyAllTrusteesToDirectors(any(), any(), any())
      }
    }

    "behave correctly when item None is chosen" in {
      val emptyUA = UserAnswers()

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copyAllTrusteesToDirectors(any(), any(), any())).thenReturn(emptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules: _*)) { app =>
        val controller = app.injector.instanceOf[TrusteesAlsoDirectorsController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value" -> "-1"
        )
        val result = controller.onSubmit(establisherIndex = 0)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        verify(mockDataPrefillService, never).copyAllTrusteesToDirectors(any(), any(), any())
      }
    }

    "return bad request when error displayed" in {
      val emptyUA = UserAnswers()

      when(mockUserAnswersService.upsert(any(), any(), any())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copyAllTrusteesToDirectors(any(), ArgumentMatchers.eq(Seq(1)), any())).thenReturn(emptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules: _*)) { app =>
        val controller = app.injector.instanceOf[TrusteesAlsoDirectorsController]
        val result = controller.onSubmit(establisherIndex = 0)(fakeRequest)
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
