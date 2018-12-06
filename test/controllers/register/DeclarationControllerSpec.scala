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

package controllers.register

import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.DeclarationDutiesControllerSpec.psaId
import forms.register.DeclarationFormProvider
import identifiers.TypedIdentifier
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import identifiers.register.establishers.individual.EstablisherDetailsId
import identifiers.register.establishers.partnership.{IsPartnershipDormantId, PartnershipDetailsId}
import identifiers.register.{DeclarationDormantId, DeclarationDutiesId, SchemeDetailsId}
import models.person.PersonDetails
import models.register.{DeclarationDormant, SchemeDetails, SchemeSubmissionResponse, SchemeType}
import models.{CompanyDetails, PartnershipDetails}
import org.joda.time.LocalDate
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures {

  import DeclarationControllerSpec._

  "Declaration Controller" must {

    "return OK and don't save the DeclarationDormant " when {

      "the establisher is an individual" in {
        val result = controller(individual).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false)
        FakeUserAnswersCacheConnector.verifyNot(DeclarationDormantId)
      }
    }

    "return OK, the correct view and save the DeclarationDormant if isHubEnabled toggle is on" when {

      "the establisher is a dormant company" in {
        val result = controller(dormantCompanyAndNonDormantPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }

      "the establisher is non dormant company and partnership estabslihsre" in {
        val result = controller(nonDormantCompanyAndPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values.head)
      }

      "dormant company establisher and non dormant partnership" in {
        val result = controller(dormantCompanyAndNonDormantPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }

      "dormant partnership establisher and non dormant company" in {
        val result = controller(dormantPartnershipAndNonDormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }
    }

    "return OK and the correct view " when {
      "master trust" in {

        val data = new FakeDataRetrievalAction(Some(UserAnswers()
          .set(DeclarationDutiesId)(false)
          .asOpt
          .value
          .set(SchemeDetailsId)(SchemeDetails("Test Scheme Name", SchemeType.MasterTrust))
          .asOpt
          .value
          .json
        ))

        val result = controller(data).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }

    "return OK and the correct view if isHubEnabled toggle is off" when {

      "non-dormant company establisher" in {
        val result = controller(nonDormantCompany, isHubEnabled = false).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false, isHubEnabled = false)
      }

      "dormant company establisher" in {
        val result = controller(dormantCompany, isHubEnabled = false).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true, isHubEnabled = false)
      }
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")

      val result = controller(nonDormantCompanyAndPartnership).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "send an email when valid data is submitted when hub enabled" which {
      "fetches from Get PSA Minimal Details" in {

        reset(mockEmailConnector)

        when(mockEmailConnector.sendEmail(eqTo("email@test.com"), eqTo("pods_scheme_register"), any(), any())(any(), any()))
          .thenReturn(Future.successful(EmailSent))

        val postRequest = fakeRequest.withFormUrlEncodedBody(("agree" -> "agreed"))

        whenReady(controller(nonDormantCompany, fakeEmailConnector = mockEmailConnector,
          fakePsaNameCacheConnector = mockPSANameCacheConnector).onSubmit(postRequest)) { _ =>

          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("email@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890")),
            eqTo(psaId)
          )(any(), any())

          verifyZeroInteractions(mockPSANameCacheConnector)

        }
      }
    }

    "return a Bad Request and errors" when {
      "invalid data is submitted in individual journey" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller(individual).onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm, isCompany = false, isDormant = false)
      }
      "invalid data is submitted in company journey" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller(nonDormantCompanyAndPartnership).onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm, isCompany = true, isDormant = false)
      }
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
          val result = controller(dontGetAnyData).onSubmit()(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}

object DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  private val schemeName = "Test Scheme Name"

  private def controller(dataRetrievalAction: DataRetrievalAction, isHubEnabled: Boolean = true,
                         fakeEmailConnector: EmailConnector = fakeEmailConnector,
                         fakePsaNameCacheConnector: PSANameCacheConnector = fakePsaNameCacheConnector): DeclarationController =
    new DeclarationController(
      appConfig(isHubEnabled),
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      fakePensionsSchemeConnector,
      fakeEmailConnector,
      fakePsaNameCacheConnector,
      applicationCrypto,
      fakePensionAdminstratorConnector
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false,
                           isHubEnabled: Boolean = true): String =
    declaration(
      appConfig(isHubEnabled),
      form,
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge
    )(fakeRequest, messages).toString

  private val individual =
    UserAnswers()
      .schemeDetails()
      .individualEstablisher()
      .set(DeclarationDutiesId)(false)
      .asOpt
      .value
      .asDataRetrievalAction()

  private val nonDormantCompany =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher(0)
      .dormant(false)
      .asDataRetrievalAction()

  private val dormantCompany =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher(0)
      .dormant(true)
      .asDataRetrievalAction()

  private val nonDormantCompanyAndPartnership =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher(0)
      .set(DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .partnershipEstablisher(1)
      .dormantPartnership(false, 1)
      .asDataRetrievalAction()

  private val dormantCompanyAndNonDormantPartnership =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher(0)
      .set(DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .companyEstablisher(1)
      .dormantCompany(true, 1)
      .partnershipEstablisher(2)
      .dormantPartnership(false, 2)
      .asDataRetrievalAction()

  private val dormantPartnershipAndNonDormantCompany =
    UserAnswers()
      .schemeDetails()
      .companyEstablisher(0)
      .set(DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .companyEstablisher(1)
      .dormantCompany(false, 1)
      .partnershipEstablisher(2)
      .dormantPartnership(true, 2)
      .partnershipEstablisher(3)
      .dormantPartnership(false, 3)
      .asDataRetrievalAction()

  private implicit class UserAnswersOps(answers: UserAnswers) {

    def schemeDetails(): UserAnswers = {
      answers.set(SchemeDetailsId)(SchemeDetails("Test Scheme Name", SchemeType.SingleTrust)).asOpt.value
    }

    def dormant(dormant: Boolean): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(DeclarationDormantId)(declarationDormant).asOpt.value
    }

    def companyEstablisher(index: Int): UserAnswers = {
      answers.set(CompanyDetailsId(index))(CompanyDetails("test-company-name", None, None)).asOpt.value
    }

    def partnershipEstablisher(index: Int): UserAnswers = {
      answers.set(PartnershipDetailsId(index))(PartnershipDetails("test-company-name")).asOpt.value
    }

    def individualEstablisher(): UserAnswers = {
      answers.set(EstablisherDetailsId(0))(PersonDetails("test-first-name", None, "test-last-name", LocalDate.now())).asOpt.value
    }

    def dormantCompany(dormant: Boolean, index: Int): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(IsCompanyDormantId(index))(declarationDormant).asOpt.value
    }

    def dormantPartnership(dormant: Boolean, index: Int): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(IsPartnershipDormantId(index))(declarationDormant).asOpt.value
    }

    def asDataRetrievalAction(): DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }
  }

  private val mockPSANameCacheConnector = mock[PSANameCacheConnector]
  private val mockEmailConnector = mock[EmailConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]

  object fakePsaNameCacheConnector extends PSANameCacheConnector(
    frontendAppConfig,
    mock[WSClient]
  ) with FakeUserAnswersCacheConnector {

    override def fetch(cacheId: String)(implicit
                                        ec: ExecutionContext,
                                        hc: HeaderCarrier): Future[Option[JsValue]] = Future.successful(Some(Json.obj("psaName" -> "Test",
      "psaEmail" -> "email@test.com")))

    override def upsert(cacheId: String, value: JsValue)
                       (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = Future.successful(value)

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                                (implicit
                                                 ec: ExecutionContext,
                                                 hc: HeaderCarrier
                                                ): Future[JsValue] = ???
  }

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

  private val fakeEmailConnector = new EmailConnector {
    override def sendEmail
    (emailAddress: String, templateName: String, params: Map[String, String] = Map.empty, psaId: PsaId)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[EmailStatus] = {
      Future.successful(EmailSent)
    }
  }

  private val fakePensionAdminstratorConnector = new PensionAdministratorConnector {
    override def getPSAEmail(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("email@test.com")

    override def getPSAName(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = Future.successful("PSA Name")
  }

}