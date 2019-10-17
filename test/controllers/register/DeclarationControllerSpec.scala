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

package controllers.register

import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.SchemeTypeId
import identifiers.register.DeclarationDormantId
import identifiers.register.establishers.company.{CompanyDetailsId, IsCompanyDormantId}
import identifiers.register.establishers.individual.EstablisherNameId
import identifiers.register.establishers.partnership.PartnershipDetailsId
import models.person.PersonName
import models.register.{DeclarationDormant, SchemeSubmissionResponse, SchemeType}
import models.{CompanyDetails, PartnershipDetails}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.{Call, RequestHeader}
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

    "return OK, the correct view and save the DeclarationDormant" when {

      "the establisher is a dormant company" in {
        val result = controller(dormantCompanyAndNonDormantPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values.head)
      }

      "the establisher is non dormant company and partnership estabslihsre" in {
        val result = controller(nonDormantCompanyAndPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }

      "dormant company establisher and non dormant partnership" in {
        val result = controller(dormantCompanyAndNonDormantPartnership).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values.head)
      }
    }

    "return OK and the correct view " when {
      "master trust" in {

        val data = new FakeDataRetrievalAction(Some(UserAnswers()
          .set(identifiers.DeclarationDutiesId)(false)
          .asOpt
          .value
          .set(SchemeTypeId)(SchemeType.MasterTrust)
          .asOpt
          .value
          .json
        ))

        val result = controller(data).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")

      val result = controller(nonDormantCompanyAndPartnership).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "send an email when valid data is submitted" which {
      "fetches from Get PSA Minimal Details" in {

        reset(mockEmailConnector)

        when(mockEmailConnector.sendEmail(eqTo("email@test.com"), eqTo("pods_scheme_register"), any(), any())(any(), any()))
          .thenReturn(Future.successful(EmailSent))

        val postRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")

        whenReady(controller(nonDormantCompany, fakeEmailConnector = mockEmailConnector).onSubmit(postRequest)) { _ =>

          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("email@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890")),
            eqTo(psaId)
          )(any(), any())

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
  val psaId = PsaId("A0000000")

  private def controller(dataRetrievalAction: DataRetrievalAction,
                         fakeEmailConnector: EmailConnector = fakeEmailConnector
                        ): DeclarationController =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      fakePensionsSchemeConnector,
      fakeEmailConnector,
      applicationCrypto,
      fakePensionAdminstratorConnector
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    declaration(
      frontendAppConfig,
      form,
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge,
      None
    )(fakeRequest, messages).toString

  private val individual =
    UserAnswers()
      .individualEstablisher()
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value
      .asDataRetrievalAction()

  private val nonDormantCompany =
    UserAnswers()
      .companyEstablisher(0)
      .dormant(false)
      .asDataRetrievalAction()

  private val nonDormantCompanyAndPartnership =
    UserAnswers()
      .companyEstablisher(0)
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .partnershipEstablisher(1)
      .asDataRetrievalAction()

  private val dormantCompanyAndNonDormantPartnership =
    UserAnswers()
      .companyEstablisher(0)
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .companyEstablisher(1)
      .dormantCompany(true, 1)
      .partnershipEstablisher(2)
      .asDataRetrievalAction()

  private val dormantPartnershipAndNonDormantCompany =
    UserAnswers()
      .companyEstablisher(0)
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .dormantCompany(false, 0)
      .companyEstablisher(1)
      .dormantCompany(false, 1)
      .partnershipEstablisher(2)
      .partnershipEstablisher(3)
      .asDataRetrievalAction()

  private implicit class UserAnswersOps(answers: UserAnswers) {

    def dormant(dormant: Boolean): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(DeclarationDormantId)(declarationDormant).asOpt.value
    }

    def companyEstablisher(index: Int): UserAnswers = {
      answers.set(CompanyDetailsId(index))(CompanyDetails("test-company-name")).asOpt.value
    }

    def partnershipEstablisher(index: Int): UserAnswers = {
      answers.set(PartnershipDetailsId(index))(PartnershipDetails("test-company-name")).asOpt.value
    }

    def individualEstablisher(): UserAnswers = {
      answers.set(EstablisherNameId(0))(PersonName("test-first-name", "test-last-name")).asOpt.value
    }

    def dormantCompany(dormant: Boolean, index: Int): UserAnswers = {
      val declarationDormant = if (dormant) DeclarationDormant.Yes else DeclarationDormant.No
      answers.set(IsCompanyDormantId(index))(declarationDormant).asOpt.value
    }

    def asDataRetrievalAction(): DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }
  }

  private val mockEmailConnector = mock[EmailConnector]
  private val applicationCrypto = injector.instanceOf[ApplicationCrypto]


  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("S1234567890")

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[SchemeSubmissionResponse] = {
      Future.successful(validSchemeSubmissionResponse)
    }

    override def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = ???

    override def checkForAssociation(psaId: String, srn: String)(implicit headerCarrier: HeaderCarrier,
                                                                 ec: ExecutionContext, request: RequestHeader): Future[Boolean] = ???
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