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

package controllers.racdac

import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import helpers.DataCompletionHelper
import identifiers.racdac.{DeclarationId, RACDACNameId}
import identifiers.register.SubmissionReferenceNumberId
import models.MinimalPSA
import models.register.SchemeSubmissionResponse
import org.mockito.{ArgumentCaptor, Matchers}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.racdac.declaration

import scala.concurrent.Future

class DeclarationControllerSpec
  extends ControllerSpecBase
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterEach {

  import DeclarationControllerSpec._

  override protected def beforeEach(): Unit = {
    when(mockPensionAdministratorConnector.getPSAName(any(), any())).thenReturn(Future.successful(psaName))
  }

  "onPageLoad" must {
    "return OK and the correct view " in {
        val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
    }
  }

  "onClickAgree" must {
    "redirect to the next page on clicking agree and continue" in {
      val uaCaptorForRegisterScheme = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockPensionsSchemeConnector.registerScheme(uaCaptorForRegisterScheme.capture(), any())(any(), any()))
        .thenReturn(Future.successful(Right(schemeSubmissionResponse)))
      when(mockEmailConnector.sendEmail(any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(EmailSent))
      when(mockMinimalPsaConnector.getMinimalPsaDetails(any())(any(), any())).thenReturn(Future.successful(minimalPsa))

      val result = controller(dataRetrievalAction).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(mockPensionsSchemeConnector, times(1)).registerScheme(any(), any())(any(), any())
      uaCaptorForRegisterScheme.getValue.get(DeclarationId) mustBe Some(true)
      FakeUserAnswersCacheConnector.verifyUpsert(DeclarationId, true)
      FakeUserAnswersCacheConnector.verifyUpsert(SubmissionReferenceNumberId, schemeSubmissionResponse)
      verify(mockEmailConnector, times(1))
        .sendEmail(Matchers.eq(minimalPsa.email), Matchers.eq("pods_racdac_scheme_register"),
          Matchers.eq(emailParams), any())(any(), any())

    }
  }
}

object DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with DataCompletionHelper {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val schemeName = "scheme"
  private val minimalPsa = MinimalPSA(email = "a@a.c", isPsaSuspended = false, organisationName = Some("org"), individualDetails = None)
  private val emailParams = Map("psaName" -> minimalPsa.name, "schemeName" -> schemeName)
  private val href = controllers.racdac.routes.DeclarationController.onClickAgree()
  private val mockPensionAdministratorConnector = mock[PensionAdministratorConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val mockMinimalPsaConnector = mock[MinimalPsaConnector]
  private val mockPensionsSchemeConnector = mock[PensionsSchemeConnector]
  private val psaName = "A PSA"
  //private val psaId = "A0000000"
  private val view = injector.instanceOf[declaration]

  private val schemeSubmissionResponse = SchemeSubmissionResponse(schemeReferenceNumber = "srn")

  private def controller(dataRetrievalAction: DataRetrievalAction): DeclarationController =
    new DeclarationController(
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeAllowAccessProvider(),
      mockPensionAdministratorConnector,
      mockPensionsSchemeConnector,
      mockEmailConnector,
      mockMinimalPsaConnector,
      controllerComponents,
      view
    )

  private def viewAsString(): String =
    view(
      psaName,
      href
    )(fakeRequest, messages).toString

  private def dataRetrievalAction: DataRetrievalAction = {
    UserAnswers()
      .set(RACDACNameId)(schemeName).asOpt.get
      .dataRetrievalAction
  }
}
