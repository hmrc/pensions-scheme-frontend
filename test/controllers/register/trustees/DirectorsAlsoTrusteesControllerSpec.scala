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

package controllers.register.trustees

import controllers.ControllerSpecBase
import controllers.actions.*
import forms.dataPrefill.DataPrefillRadioFormProvider
import models.prefill.IndividualDetails as DataPrefillIndividualDetails
import models.{DataPrefillRadioOptions, EmptyOptionalSchemeReferenceNumber, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.{JsNull, JsValue, Json}
import play.api.test.Helpers.*
import services.DataPrefillService.DirectorIdentifier
import services.{DataPrefillService, UserAnswersService}
import utils.UserAnswers
import views.html.dataPrefillRadio

import scala.concurrent.Future

class DirectorsAlsoTrusteesControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar {

  private val mockDataPrefillService = mock[DataPrefillService]
  private val mockUserAnswersService = mock[UserAnswersService]

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(UserAnswers(Json.obj()).json))

  private val pageHeading = Messages("messages__trustees__prefill__title")
  private val titleMessage = Messages("messages__trustees__prefill__heading")
  private val postCall = controllers.register.trustees.routes.DirectorsAlsoTrusteesController.onSubmit(0)

  private val seqOneEstablisherDirector = Seq(
    DataPrefillIndividualDetails(
      firstName = "John", lastName = "Smith", index = 3, isDeleted = false, nino = None, dob = None, isComplete = true, mainIndex = Some(1)
    )
  )

  private val seqThreeEstablisherDirectors = Seq(
    DataPrefillIndividualDetails(
      firstName = "John", lastName = "Smith", index = 3, isDeleted = false, nino = None, dob = None, isComplete = true, mainIndex = Some(1)
    ),
    DataPrefillIndividualDetails(
      firstName = "Jane", lastName = "Anderson", index = 4, isDeleted = false, nino = None, dob = None, isComplete = true, mainIndex = Some(2)
    ),
    DataPrefillIndividualDetails(
      firstName = "Jane", lastName = "Anderson", index = 5, isDeleted = false, nino = None, dob = None, isComplete = true, mainIndex = Some(2)
    )
  )

  private val extraModules: Seq[GuiceableModule] = Seq(
    bind[DataPrefillService].toInstance(mockDataPrefillService),
    bind[UserAnswersService].toInstance(mockUserAnswersService)
  )

  private val index = 0

  override def beforeEach(): Unit = {
    reset(mockDataPrefillService)
    reset(mockUserAnswersService)
  }

  "onPageLoad when only one establisher" must {
    "return Ok and the correct view on a GET request" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqOneEstablisherDirector)
      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val view = app.injector.instanceOf[dataPrefillRadio]
        val result = controller.onPageLoad(index)(fakeRequest)

        status(result) mustBe OK
        val form = new DataPrefillRadioFormProvider().apply(
          requiredError = ""
        )

        contentAsString(result) mustBe
          view(form, pageHeading, titleMessage, DataPrefillRadioOptions(seqOneEstablisherDirector), postCall)(fakeRequest, messages).toString
      }
    }

    "redirect to trustee name page when there are no establishers to copy" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(Nil)
      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val result = controller.onPageLoad(index)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          Some(controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(NormalMode, 0, EmptyOptionalSchemeReferenceNumber).url)
      }
    }
  }

  "onSubmit" must {
    "behave correctly when two directors out of three ticked and None not ticked" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqThreeEstablisherDirectors)
      val nonEmptyUA = UserAnswers(Json.obj("test" -> "test"))

      val jsonCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

      when(mockUserAnswersService.upsert(any(), any(), jsonCaptor.capture())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copySelectedDirectorsToTrustees(any(), any())).thenReturn(nonEmptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value[0]" -> "0",
          "value[1]" -> "2"
        )
        val result = controller.onSubmit(index)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber).url)
        val expectedDirectors = Seq(
          DirectorIdentifier(establisherIndex = 1, directorIndex = 3),
          DirectorIdentifier(establisherIndex = 2, directorIndex = 5)
        )
        verify(mockDataPrefillService, atLeastOnce).copySelectedDirectorsToTrustees(any(), ArgumentMatchers.eq(expectedDirectors))
        (jsonCaptor.getValue \ "test").asOpt[String] mustBe Some("test")
      }
    }

    "behave correctly when three directors and item None is chosen" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqThreeEstablisherDirectors)
      val nonEmptyUA = UserAnswers(Json.obj("test" -> "test"))

      val jsonCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

      when(mockUserAnswersService.upsert(any(), any(), jsonCaptor.capture())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copySelectedDirectorsToTrustees(any(), any())).thenReturn(nonEmptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value[0]" -> "-1"
        )
        val result = controller.onSubmit(index)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(NormalMode, 0, EmptyOptionalSchemeReferenceNumber).url)

        verify(mockDataPrefillService, never).copySelectedDirectorsToTrustees(any(), any())
        (jsonCaptor.getValue \ "test").asOpt[String] mustBe None
      }
    }

    "behave correctly when one director out of only one chosen and None not chosen" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqOneEstablisherDirector)
      val nonEmptyUA = UserAnswers(Json.obj("test" -> "test"))

      val jsonCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

      when(mockUserAnswersService.upsert(any(), any(), jsonCaptor.capture())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copySelectedDirectorsToTrustees(any(), any())).thenReturn(nonEmptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value" -> "0"
        )
        val result = controller.onSubmit(index)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.trustees.routes.AddTrusteeController.onPageLoad(NormalMode, EmptyOptionalSchemeReferenceNumber).url)
        val expectedDirectors = Seq(
          DirectorIdentifier(establisherIndex = 1, directorIndex = 3)
        )
        verify(mockDataPrefillService, atLeastOnce).copySelectedDirectorsToTrustees(any(), ArgumentMatchers.eq(expectedDirectors))
        (jsonCaptor.getValue \ "test").asOpt[String] mustBe Some("test")
      }
    }

    "behave correctly when one director and None chosen" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqOneEstablisherDirector)
      val nonEmptyUA = UserAnswers(Json.obj("test" -> "test"))

      val jsonCaptor: ArgumentCaptor[JsValue] = ArgumentCaptor.forClass(classOf[JsValue])

      when(mockUserAnswersService.upsert(any(), any(), jsonCaptor.capture())(any(), any(), any())).thenReturn(Future.successful(JsNull))
      when(mockDataPrefillService.copySelectedDirectorsToTrustees(any(), any())).thenReturn(nonEmptyUA)

      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val request = fakeRequest.withFormUrlEncodedBody(
          "value" -> "-1"
        )
        val result = controller.onSubmit(index)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.trustees.individual.routes.TrusteeNameController.onPageLoad(NormalMode, 0, EmptyOptionalSchemeReferenceNumber).url)
        verify(mockDataPrefillService, never).copySelectedDirectorsToTrustees(any(), any())
        (jsonCaptor.getValue \ "test").asOpt[String] mustBe None
      }
    }

    "return bad request when field not filled in" in {
      when(mockDataPrefillService.getListOfDirectorsToBeCopied(any()))
        .thenReturn(seqThreeEstablisherDirectors)
      val allModules = modules(dataRetrievalAction) ++ extraModules
      running(_.overrides(allModules*)) { app =>
        val controller = app.injector.instanceOf[DirectorsAlsoTrusteesController]
        val result = controller.onSubmit(index)(fakeRequest)
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
