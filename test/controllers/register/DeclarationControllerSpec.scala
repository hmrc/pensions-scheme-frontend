/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.MinimalPsaConnector.MinimalPSA
import connectors.{FakeUserAnswersCacheConnector, _}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationFormProvider
import helpers.DataCompletionHelper
import identifiers.HaveAnyTrusteesId
import identifiers.register.DeclarationDormantId
import models.NormalMode
import models.register.{DeclarationDormant, SchemeSubmissionResponse, SchemeType}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.{Call, RequestHeader}
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.hstasklisthelper.HsTaskListHelperRegistration
import utils.{FakeNavigator, UserAnswers}
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with BeforeAndAfterEach {

  import DeclarationControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockHsTaskListHelperRegistration)
    when(mockHsTaskListHelperRegistration.declarationEnabled(any())).thenReturn(true)
  }

  "Declaration Controller" must {

    "redirect to task list page when user answers are not complete" in {
      when(mockHsTaskListHelperRegistration.declarationEnabled(any())).thenReturn(false)
      val result = controller(UserAnswers().schemeName("Test Scheme").dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe controllers.routes.SchemeTaskListController.onPageLoad(NormalMode, None).url
    }

    "return OK and don't save the DeclarationDormant " when {

      "the establisher is an individual" in {
        val result = controller(individualEst).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false)
        FakeUserAnswersCacheConnector.verifyNot(DeclarationDormantId)
      }
    }

    "return OK, the correct view and save the DeclarationDormant" when {

      "the establisher is a dormant company" in {
        val result = controller(dormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = true)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values.head)
      }

      "the establisher is non dormant company" in {
        val result = controller(nonDormantCompany).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = true, isDormant = false)
        FakeUserAnswersCacheConnector.verify(DeclarationDormantId, DeclarationDormant.values(1))
      }
    }

    "return OK and the correct view " when {
      "master trust and all the answers is complete" in {

        val result = controller(dataWithMasterTrust).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }

    "redirect to the next page on clicking agree and continue" in {
      val result = controller(nonDormantCompany).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "send an email on clicking agree and continue" which {
      "fetches from Get PSA Minimal Details" in {

        reset(mockEmailConnector)

        when(mockEmailConnector.sendEmail(eqTo("test@test.com"), eqTo("pods_scheme_register"), any(), any())(any(), any()))
          .thenReturn(Future.successful(EmailSent))

        whenReady(controller(nonDormantCompany, fakeEmailConnector = mockEmailConnector).onClickAgree()(fakeRequest)) { _ =>

          verify(mockEmailConnector, times(1)).sendEmail(
            eqTo("test@test.com"),
            eqTo("pods_scheme_register"),
            eqTo(Map("srn" -> "S12345 67890", "psaName" -> "psa name")),
            eqTo(psaId)
          )(any(), any())

        }
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
          val result = controller(dontGetAnyData).onClickAgree()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}

object DeclarationControllerSpec extends ControllerSpecBase with MockitoSugar with DataCompletionHelper {
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new DeclarationFormProvider()
  private val form = formProvider()
  private val href = controllers.register.routes.DeclarationController.onClickAgree()
  val psaId = PsaId("A0000000")

  private val mockHsTaskListHelperRegistration = mock[HsTaskListHelperRegistration]

  private val view = injector.instanceOf[declaration]

  private def uaWithBasicData: UserAnswers = setCompleteBeforeYouStart(isComplete = true,
    setCompleteMembers(isComplete = true,
      setCompleteBank(isComplete = true,
        setCompleteBenefits(isComplete = true,
          setCompleteEstIndividual(0, UserAnswers())))))
    .set(HaveAnyTrusteesId)(false).asOpt.value

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
      fakePensionsSchemeConnector,
      fakeEmailConnector,
      applicationCrypto,
      fakeMinimalPsaConnector,
      stubMessagesControllerComponents(),
      mockHsTaskListHelperRegistration,
      view
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    view(
      isCompany,
      isDormant,
      showMasterTrustDeclaration,
      hasWorkingKnowledge,
      Some("Test Scheme"),
      href
    )(fakeRequest, messages).toString

  private def individualEst: DataRetrievalAction = {
    setCompleteWorkingKnowledge(isComplete = true, uaWithBasicData)
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value
      .dataRetrievalAction
  }

  private def dataWithMasterTrust: DataRetrievalAction = {
    setCompleteWorkingKnowledge(isComplete = true, uaWithBasicData)
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value.schemeType(SchemeType.MasterTrust).set(HaveAnyTrusteesId)(false).asOpt.value
      .dataRetrievalAction
  }

  private val nonDormantCompany =
    setCompleteWorkingKnowledge(
      isComplete = true, setCompleteEstCompany(1, uaWithBasicData))
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value.establisherCompanyDormant(1, DeclarationDormant.No).dataRetrievalAction

  private val dormantCompany: DataRetrievalAction = {
    setCompleteWorkingKnowledge(
    isComplete = true, setCompleteEstCompany(1, uaWithBasicData))
    .set(identifiers.DeclarationDutiesId)(false).asOpt
    .value.establisherCompanyDormant(1, DeclarationDormant.Yes).dataRetrievalAction
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

  private val fakeMinimalPsaConnector = new MinimalPsaConnector {
    override def isPsaSuspended(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = Future.successful(true)

    override def getMinimalPsaDetails(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[MinimalPsaConnector.MinimalPSA] =
      Future.successful(MinimalPSA("test@test.com", Some("psa name"), None))
  }
}