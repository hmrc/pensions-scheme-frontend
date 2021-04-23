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
import forms.register.DeclarationFormProvider
import helpers.DataCompletionHelper
import identifiers._
import models.register.{DeclarationDormant, SchemeSubmissionResponse, SchemeType}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.{Call, RequestHeader}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.{FakeNavigator, UserAnswers}
import views.html.racdac.declaration

import scala.concurrent.{ExecutionContext, Future}

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
    "return OK and the correct view " when {
      "master trust and all the answers is complete" in {

        val result = controller(dataWithMasterTrust).onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(isCompany = false, isDormant = false, showMasterTrustDeclaration = true)
      }
    }
  }

  "onClickAgree" must {

    "redirect to the next page on clicking agree and continue" in {
      val result = controller(nonDormantCompany).onClickAgree()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
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
  private val mockPensionAdministratorConnector = mock[PensionAdministratorConnector]
  private val psaName = "A PSA"
  private val view = injector.instanceOf[declaration]

  private def uaWithBasicData: UserAnswers =
    setCompleteBeforeYouStart(
      isComplete = true,
      setCompleteMembers(
        isComplete = true,
        setCompleteBank(
          isComplete = true,
          setCompleteBenefits(
            isComplete = true,
            setCompleteEstIndividual(0, UserAnswers())
          )
        )
      )
    )
      .set(HaveAnyTrusteesId)(false).asOpt.value

  private def controller(dataRetrievalAction: DataRetrievalAction): DeclarationController =
    new DeclarationController(
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakePensionsSchemeConnector,
      mockPensionAdministratorConnector,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[_] = form, isCompany: Boolean, isDormant: Boolean,
                           showMasterTrustDeclaration: Boolean = false, hasWorkingKnowledge: Boolean = false): String =
    view(
      psaName,
      href
    )(fakeRequest, messages).toString

  private def dataWithMasterTrust: DataRetrievalAction = {
    setCompleteWorkingKnowledge(isComplete = true, uaWithBasicData)
      .set(identifiers.DeclarationDutiesId)(false).asOpt
      .value.schemeType(SchemeType.MasterTrust).set(HaveAnyTrusteesId)(false).asOpt.value
      .dataRetrievalAction
  }

  private val nonDormantCompany: DataRetrievalAction =
    setCompleteWorkingKnowledge(
      isComplete = true,
      ua = setCompleteEstCompany(1, uaWithBasicData)
    )
      .set(identifiers.DeclarationDutiesId)(false)
      .asOpt
      .value
      .establisherCompanyDormant(1, DeclarationDormant.No)
      .dataRetrievalAction

  private val validSchemeSubmissionResponse = SchemeSubmissionResponse("S1234567890")

  private val fakePensionsSchemeConnector = new PensionsSchemeConnector {
    override def registerScheme
    (answers: UserAnswers, psaId: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, SchemeSubmissionResponse]] = {
      Future.successful(Right(validSchemeSubmissionResponse))
    }

    override def updateSchemeDetails(psaId: String, pstr: String, answers: UserAnswers)(
      implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[HttpResponse, Unit]] = ???

    override def checkForAssociation(psaId: String, srn: String)
                                    (implicit headerCarrier: HeaderCarrier,
                                     ec: ExecutionContext, request: RequestHeader): Future[Either[HttpResponse, Boolean]] = ???
  }
}
