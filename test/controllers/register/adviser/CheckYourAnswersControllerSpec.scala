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

package controllers.register.adviser

import connectors._
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.TypedIdentifier
import models.CheckMode
import models.register.SchemeSubmissionResponse
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ScalaFutures {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getMandatoryAdviser).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "send an email when valid data is submitted" in {
      reset(mockEmailConnector)

      when(mockEmailConnector.sendEmail(eqTo("email@test.com"), eqTo("pods_scheme_register"), any(), any())(any(), any()))
        .thenReturn(Future.successful(EmailSent))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      whenReady(controller(emailConnector = mockEmailConnector).onSubmit(postRequest)) {
        _ =>
          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("email@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890")),
            eqTo(psaId)
          )(any(), any())
      }
    }

    "not send an email if there is no records for user email" in {
      reset(mockEmailConnector)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      whenReady(controller(emailConnector = mockEmailConnector, psaName = Json.obj("psaName" -> "Test")).onSubmit(postRequest)) {
        _ =>
          verify(mockEmailConnector, times(0)).sendEmail(any(), any(), any(), any())(any(), any())
      }
    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with MockitoSugar {

  val schemeName = "Test Scheme Name"

  val psaId = PsaId("A0000000")

  lazy val adviserDetailsRoute: Option[String] = Some(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
  lazy val adviserSection = AnswerSection(None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq("name"), answerIsMessageKey = false, adviserDetailsRoute),
      AnswerRow("messages__adviserDetails__email", Seq("email"), answerIsMessageKey = false, adviserDetailsRoute),
      AnswerRow("messages__adviserDetails__phone", Seq("phone"), answerIsMessageKey = false, adviserDetailsRoute)
    )
  )

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("S1234567890")

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
      Future.successful(validSchemeSubmissionResponse)
    }
  }

  private val mockEmailConnector = mock[EmailConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  private val fakeEmailConnector = new EmailConnector {
    override def sendEmail
    (emailAddress: String, templateName: String, params: Map[String, String] = Map.empty, psaId: PsaId)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus] = {
      Future.successful(EmailSent)
    }
  }

  case class FakePsaNameCacheConnector(psaName: JsValue) extends PSANameCacheConnector(
    frontendAppConfig,
    mock[WSClient]
  ) with FakeUserAnswersCacheConnector {
    override def fetch(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] = Future.successful(Some(psaName))

    override def upsert(cacheId: String, value: JsValue)
                       (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = Future.successful(value)

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                                (implicit
                                                 ec: ExecutionContext,
                                                 hc: HeaderCarrier
                                                ): Future[JsValue] = ???
  }


  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 emailConnector: EmailConnector = fakeEmailConnector,
                 psaName: JsValue = Json.obj("psaName" -> "Test", "psaEmail" -> "email@test.com")
                ): CheckYourAnswersController =

    new CheckYourAnswersController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      new FakeNavigator(onwardRoute),
      new FakeCountryOptions,
      fakePensionsSchemeConnector,
      emailConnector,
      FakePsaNameCacheConnector(psaName),
      applicationCrypto
    )

  lazy val viewAsString: String = check_your_answers(
    frontendAppConfig,
    Seq(adviserSection),
    Some(Message("messages__adviser__secondary_heading")),
    postUrl
  )(fakeRequest, messages).toString

}
