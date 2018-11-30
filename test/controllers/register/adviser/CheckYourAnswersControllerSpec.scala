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
import identifiers.register.IsWorkingKnowledgeCompleteId
import models.CheckMode
import models.register.SchemeSubmissionResponse
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeCountryOptions, FakeNavigator, FakeSectionComplete, SectionComplete, UserAnswers}
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

    "send an email when valid data is submitted" which {
      "fetches name and email from Get PSA Minimal Details" in {

        val mockPsaNameCacheConnector = mock[PSANameCacheConnector]

        lazy val app = new GuiceApplicationBuilder().configure(
            "features.is-hub-enabled" -> false
          )
          .overrides(bind[EmailConnector].toInstance(mockEmailConnector))
          .overrides(bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector))
          .overrides(bind[AuthAction].toInstance(FakeAuthAction))
          .overrides(bind[DataRetrievalAction].toInstance(getEmptyData))
          .overrides(bind[PSANameCacheConnector].toInstance(mockPsaNameCacheConnector))
          .overrides(bind[PensionsSchemeConnector].toInstance(fakePensionsSchemeConnector))
          .overrides(bind[PensionAdministratorConnector].toInstance(fakePensionAdminstratorConnector))
          .build()

        reset(mockEmailConnector)

        when(mockEmailConnector.sendEmail(eqTo("email@test.com"), eqTo("pods_scheme_register"), any(), any())(any(), any()))
          .thenReturn(Future.successful(EmailSent))

        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

        whenReady(app.injector.instanceOf[CheckYourAnswersController].onSubmit(postRequest)) { _ =>

          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("email@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890")),
            eqTo(psaId)
          )(any(), any())

          verifyZeroInteractions(mockPsaNameCacheConnector)

        }
      }

    }

    "redirect to the next page on a POST request" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page on a POST request when hubEnabled" in {
        lazy val app = new GuiceApplicationBuilder().configure(
          "features.is-hub-enabled" -> true
        )
        .overrides(bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector))
        .overrides(bind[AuthAction].toInstance(FakeAuthAction))
        .overrides(bind[DataRetrievalAction].toInstance(getEmptyData))
        .overrides(bind[PensionsSchemeConnector].toInstance(fakePensionsSchemeConnector))
        .overrides(bind[PensionAdministratorConnector].toInstance(fakePensionAdminstratorConnector))
        .overrides(bind[SectionComplete].toInstance(FakeSectionComplete)).build()

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      whenReady(app.injector.instanceOf[CheckYourAnswersController].onSubmit(postRequest)) { _=>

        FakeSectionComplete.verify(IsWorkingKnowledgeCompleteId, true)
      }

    }


    "redirect to Service Unavailable page for a POST if InvalidPayloadException thrown" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(pensionsSchemeConnector = fakePensionsSchemeConnectorWithInvalidPayloadException).onSubmit(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.ServiceUnavailableController.onPageLoad().url)
    }

  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with MockitoSugar {

  val schemeName = "Test Scheme Name"
  val adviserName = "name"

  val psaId = PsaId("A0000000")

  val psaName = Json.obj("psaName" -> "Test", "psaEmail" -> "email@test.com")

  lazy val adviserDetailsRoute: Option[String] = Some(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
  lazy val adviserSection = AnswerSection(None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq(adviserName), answerIsMessageKey = false, adviserDetailsRoute, Message("messages__visuallyhidden__common__name", adviserName)),
      AnswerRow("messages__adviserDetails__email", Seq("email"), answerIsMessageKey = false, adviserDetailsRoute, "messages__visuallyhidden__adviser__email_address"),
      AnswerRow("messages__adviserDetails__phone", Seq("phone"), answerIsMessageKey = false, adviserDetailsRoute, "messages__visuallyhidden__adviser__phone_number")
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

  private val fakePensionsSchemeConnectorWithInvalidPayloadException = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
      Future.failed(new InvalidPayloadException)
    }
  }


  private val mockEmailConnector = mock[EmailConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  private val fakePensionAdminstratorConnector = new PensionAdministratorConnector {
    override def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("email@test.com")

    override def getPSAName(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("PSA Name")
  }

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
                 psaName: JsValue = psaName,
                 pensionsSchemeConnector: PensionsSchemeConnector = fakePensionsSchemeConnector
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
      pensionsSchemeConnector,
      emailConnector,
      FakePsaNameCacheConnector(psaName),
      applicationCrypto,
      fakePensionAdminstratorConnector,
      FakeSectionComplete
    )

  lazy val viewAsString: String = check_your_answers(
    frontendAppConfig,
    Seq(adviserSection),
    Some(Message("messages__adviser__secondary_heading")),
    postUrl
  )(fakeRequest, messages).toString

}
