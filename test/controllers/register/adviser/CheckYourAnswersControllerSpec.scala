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
import identifiers.register.adviser.{AdviserAddressId, AdviserEmailId, AdviserNameId}
import identifiers.register.{DeclarationDutiesId, IsWorkingKnowledgeCompleteId}
import models.CheckMode
import models.address.Address
import models.register.SchemeSubmissionResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeCountryOptions, FakeNavigator, FakeSectionComplete, UserAnswers}
import viewmodels.{AnswerRow, AnswerSection, Message}
import views.html.check_your_answers

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersControllerSpec extends ControllerSpecBase with ScalaFutures {

  import CheckYourAnswersControllerSpec._

  "CheckYourAnswers Controller" must {

    "return OK and the correct view for a GET" in {

      val adviserName = "Xyx"
      val adviserEmail = "x@x.c"
      val adviserPhone = "0000"
      val adviserAddress = Address("addr1", "addr2", Some("addr3"), Some("addr4"), Some("xxx"), "GB")

      val getMandatoryAdviser = new FakeDataRetrievalAction(Some(UserAnswers()
        .set(DeclarationDutiesId)(false)
        .asOpt
        .value
        .set(AdviserNameId)(adviserName)
        .asOpt
        .value
        .set(AdviserEmailId)(adviserEmail)
        .asOpt
        .value
        .set(AdviserAddressId)(adviserAddress)
        .asOpt
        .value
        .adviserPhone(adviserPhone)
        .json
      ))

      val result = controller(dataRetrievalAction = getMandatoryAdviser).onPageLoad(fakeRequest)

      lazy val adviserSection = AnswerSection(None,
        Seq(
          AnswerRow("declarationDuties.checkYourAnswersLabel", Seq("site.no"), answerIsMessageKey = true,
            Some(controllers.routes.WorkingKnowledgeController.onPageLoad(CheckMode).url), "messages__visuallyhidden__declarationDuties"),
          AnswerRow("adviserName.checkYourAnswersLabel", Seq(adviserName), answerIsMessageKey = false,
            Some(routes.AdviserNameController.onPageLoad(CheckMode).url), "messages__visuallyhidden__adviserName"),
          AnswerRow(Messages("adviserEmail.checkYourAnswersLabel", adviserName), Seq(adviserEmail), answerIsMessageKey = false,
            Some(routes.AdviserEmailAddressController.onPageLoad(CheckMode).url), "messages__visuallyhidden__adviserEmail"),
          AnswerRow(Messages("adviserPhone.checkYourAnswersLabel", adviserName), Seq(adviserPhone), answerIsMessageKey = false,
            Some(routes.AdviserPhoneController.onPageLoad(CheckMode).url), "messages__visuallyhidden__adviserPhone"),
          AnswerRow(Messages("adviserAddress.checkYourAnswersLabel", adviserName),
            Seq(
              adviserAddress.addressLine1,
              adviserAddress.addressLine2,
              adviserAddress.addressLine3.get,
              adviserAddress.addressLine4.get,
              adviserAddress.postcode.get,
              "Country of GB"),
            answerIsMessageKey = false,
            Some(routes.AdviserAddressController.onPageLoad(CheckMode).url), "Change address")
        )
      )

      val viewAsString: String = check_your_answers(
        frontendAppConfig,
        Seq(adviserSection),
        postUrl
      )(fakeRequest, messages).toString

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST request" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeSectionComplete.verify(IsWorkingKnowledgeCompleteId, true)
    }
  }
}

object CheckYourAnswersControllerSpec extends ControllerSpecBase with MockitoSugar {
  val schemeName = "Test Scheme Name"
  val adviserName = "name"

  val psaId = PsaId("A0000000")

  private val psaName = Json.obj("psaName" -> "Test", "psaEmail" -> "email@test.com")

  lazy val adviserDetailsRoute: Option[String] = Some(routes.AdviserDetailsController.onPageLoad(CheckMode).url)
  lazy val postUrl: Call = routes.CheckYourAnswersController.onSubmit()
  lazy val adviserSection = AnswerSection(None,
    Seq(
      AnswerRow("messages__common__cya__name", Seq(adviserName), answerIsMessageKey = false,
        adviserDetailsRoute, Message("messages__visuallyhidden__common__name", adviserName)),
      AnswerRow("messages__adviserDetails__email", Seq("email"), answerIsMessageKey = false,
        adviserDetailsRoute, "messages__visuallyhidden__adviser__email_address"),
      AnswerRow("messages__adviserDetails__phone", Seq("phone"), answerIsMessageKey = false,
        adviserDetailsRoute, "messages__visuallyhidden__adviser__phone_number")
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
    postUrl
  )(fakeRequest, messages).toString

}
